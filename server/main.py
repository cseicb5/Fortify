import datetime
import json
import logging
import os
import random
import sqlite3
import subprocess
import sys
import threading
import time

import jwt
from colorama import Fore, Style, init
from flask import Flask, jsonify, request

init()
sys.dont_write_bytecode = True

JADX_EXECUTABLE_PATH = "/usr/local/bin/jadx"
SECRET_KEY = "tNzMya1CCZvH8UQwZfH4vdjhgmZGifFV0E8p20fz"

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_DIR = os.path.join(BASE_DIR, "sandbox/output")
UPLOADS_FOLDER = os.path.join(BASE_DIR, "sandbox/input")
MALWARE_FOLDER = os.path.join(UPLOADS_FOLDER, "malware-box")
PHISHING_FOLDER = os.path.join(UPLOADS_FOLDER, "message-box")
DB_PATH = os.path.join(BASE_DIR, "sandbox/jobs-database.db")

MALWARE_WORKER_DIR = os.path.join(BASE_DIR, "api/malware-api")
PHISHING_WORKER_DIR = os.path.join(BASE_DIR, "api/phishing-api")
MALWARE_WORKER_PATH = os.path.join(MALWARE_WORKER_DIR, "malware-worker.py")
PHISHING_WORKER_PATH = os.path.join(PHISHING_WORKER_DIR, "phishing-worker.py")

TEST_WEBSITE_DIR = os.path.join(BASE_DIR, "debug")
TEST_PY_PATH = os.path.join(TEST_WEBSITE_DIR, "debug.py")

os.makedirs(MALWARE_FOLDER, exist_ok=True)
os.makedirs(PHISHING_FOLDER, exist_ok=True)
os.makedirs(OUTPUT_DIR, exist_ok=True)

app = Flask(__name__)
app.config["SECRET_KEY"] = SECRET_KEY

werkzeug_logger = logging.getLogger("werkzeug")
werkzeug_logger.setLevel(logging.ERROR)


def log(code, message):
    color = Fore.WHITE
    if code.startswith("ERR"):
        color = Fore.RED
    elif code.startswith("SYS"):
        color = Fore.CYAN
    elif code.startswith("JOB"):
        color = Fore.YELLOW
    elif code.startswith("API"):
        color = Fore.GREEN

    timestamp = datetime.datetime.now().strftime("%H:%M:%S")
    print(
        f"{Style.DIM}{timestamp}{Style.RESET_ALL} {color}[{code}]{Style.RESET_ALL} {message}"
    )
    sys.stdout.flush()


def get_db_connection():
    try:
        conn = sqlite3.connect(DB_PATH, check_same_thread=False)
        conn.row_factory = sqlite3.Row
        return conn
    except sqlite3.Error as e:
        log("ERR-DB-01", f"Connection failure: {e}")
        return None


def init_db():
    conn = get_db_connection()
    if conn:
        try:
            cursor = conn.cursor()
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS data(
                    JOB_ID INTEGER NOT NULL,
                    JWT_TOKEN TEXT NOT NULL,
                    TYPE TEXT CHECK(TYPE IN ("Malware","Phishing")) NOT NULL,
                    INPUT_FILE_PATH TEXT,
                    STATUS TEXT CHECK(STATUS IN ("Pending","Done")) NOT NULL,
                    OUTPUT_FILE_PATH TEXT,
                    CONFIDENCE INTEGER NULL,
                    DETECTION TEXT NULL
                )""")
            conn.commit()
        finally:
            conn.close()


@app.route("/getToken", methods=["POST"])
def get_token():
    data = request.get_json()
    if not data or "username" not in data:
        return jsonify({"error": "Username is required"}), 400

    username = data["username"]
    token = jwt.encode(
        {
            "user": username,
            "exp": datetime.datetime.now(datetime.timezone.utc)
            + datetime.timedelta(hours=24),
        },
        app.config["SECRET_KEY"],
        algorithm="HS256",
    )

    log("API-100", f"Token issued for: {username}")
    return jsonify({"token": token})


@app.route("/scanApk", methods=["POST"])
def addApp():
    auth_header = request.headers.get("Authorization")
    if not (auth_header and auth_header.startswith("Bearer ")):
        return jsonify({"error": "Missing Token"}), 401
    jwt_token = auth_header.split(" ")[1]

    file = request.files.get("file")
    if not file or not file.filename:
        return jsonify({"error": "No file"}), 400

    filepath = os.path.join(MALWARE_FOLDER, file.filename)
    file.save(filepath)

    conn = get_db_connection()
    if not conn:
        return jsonify({"error": "DB Error"}), 500

    try:
        job_id = random.randint(100000, 999999)
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO data (JOB_ID, JWT_TOKEN, TYPE, INPUT_FILE_PATH, STATUS) VALUES (?, ?, ?, ?, ?)",
            (job_id, jwt_token, "Malware", filepath, "Pending"),
        )
        conn.commit()
        log("API-101", f"Job {job_id} (Malware) Queued")
        return jsonify({"jobID": job_id}), 201
    finally:
        conn.close()


@app.route("/scanMessage", methods=["POST"])
def addMessage():
    auth_header = request.headers.get("Authorization")
    if not (auth_header and auth_header.startswith("Bearer ")):
        return jsonify({"error": "Missing Token"}), 401
    jwt_token = auth_header.split(" ")[1]

    message = request.form.get("message")
    if not message:
        return jsonify({"error": "No message"}), 400

    job_id = random.randint(100000, 999999)
    filepath = os.path.join(PHISHING_FOLDER, f"message_{job_id}.txt")
    with open(filepath, "w") as f:
        f.write(message)

    conn = get_db_connection()
    if not conn:
        return jsonify({"error": "DB Error"}), 500

    try:
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO data (JOB_ID, JWT_TOKEN, TYPE, INPUT_FILE_PATH, STATUS) VALUES (?, ?, ?, ?, ?)",
            (job_id, jwt_token, "Phishing", filepath, "Pending"),
        )
        conn.commit()
        log("API-102", f"Job {job_id} (Phishing) Queued")
        return jsonify({"jobID": job_id}), 201
    finally:
        conn.close()


@app.route("/scanStatus")
def getJob():
    job_id = request.args.get("jobID")
    if not job_id:
        return jsonify({"error": "jobID required"}), 400

    auth_header = request.headers.get("Authorization")
    if not (auth_header and auth_header.startswith("Bearer ")):
        return jsonify({"error": "Missing Token"}), 401
    user_jwt = auth_header.split(" ")[1]

    conn = get_db_connection()
    if not conn:
        return jsonify({"error": "DB Error"}), 500

    try:
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM data WHERE JOB_ID = ?", (job_id,))
        row = cursor.fetchone()
        if not row:
            return jsonify({"error": "Job not found"}), 404

        if user_jwt != row["JWT_TOKEN"]:
            return jsonify({"error": "Unauthorized"}), 403

        return jsonify({"details": dict(row)}), 200
    finally:
        conn.close()


@app.route("/seeDbs", methods=["GET"])
def seeDbs():
    conn = get_db_connection()
    if not conn:
        return jsonify({"error": "DB Error"}), 500

    try:
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM data ORDER BY JOB_ID DESC")
        rows = cursor.fetchall()
        return jsonify([dict(row) for row in rows]), 200
    finally:
        conn.close()


@app.route("/getScanDetails", methods=["GET"])
def get_scan_details():
    job_id = request.args.get("jobID")
    if not job_id:
        return jsonify({"error": "jobID required"}), 400

    report_path = os.path.join(OUTPUT_DIR, f"{job_id}.json")

    if not os.path.exists(report_path):
        return jsonify({"error": "Report not found"}), 404

    try:
        with open(report_path, "r", encoding="utf-8") as f:
            return jsonify(json.load(f))
    except Exception as e:
        log("ERR-FILE-01", f"Read error job {job_id}: {e}")
        return jsonify({"error": "File Error"}), 500


def get_oldest_pending_job():
    conn = get_db_connection()
    if not conn:
        return None
    try:
        cursor = conn.cursor()
        cursor.execute(
            "SELECT JOB_ID, TYPE, INPUT_FILE_PATH FROM data WHERE STATUS = 'Pending' ORDER BY JOB_ID LIMIT 1"
        )
        return cursor.fetchone()
    except sqlite3.Error:
        return None
    finally:
        conn.close()


def update_job_db(job_id, output_path, detection_result):
    conn = get_db_connection()
    if not conn:
        return
    try:
        cursor = conn.cursor()
        cursor.execute(
            "UPDATE data SET STATUS = ?, OUTPUT_FILE_PATH = ?, DETECTION = ? WHERE JOB_ID = ?",
            ("Done", output_path, detection_result, job_id),
        )
        conn.commit()
    except sqlite3.Error as e:
        log("ERR-DB-02", f"Update failed {job_id}: {e}")
    finally:
        conn.close()


def dispatcher():
    while True:
        try:
            job = get_oldest_pending_job()

            if job:
                job_id, job_type, input_path = job
                command = []
                worker_cwd = None

                log("JOB-001", f"Processing {job_id} [{job_type}]")

                if job_type == "Malware":
                    command = [
                        sys.executable,
                        MALWARE_WORKER_PATH,
                        "--src",
                        input_path,
                        "--out",
                        OUTPUT_DIR,
                        "--jadx_path",
                        JADX_EXECUTABLE_PATH,
                        "--job_id",
                        str(job_id),
                    ]
                    worker_cwd = MALWARE_WORKER_DIR
                elif job_type == "Phishing":
                    command = [
                        sys.executable,
                        PHISHING_WORKER_PATH,
                        "--src",
                        input_path,
                        "--out",
                        OUTPUT_DIR,
                        "--job_id",
                        str(job_id),
                    ]
                    worker_cwd = PHISHING_WORKER_DIR

                if command:
                    try:
                        subprocess.run(
                            command,
                            check=True,
                            capture_output=True,
                            text=True,
                            cwd=worker_cwd,
                        )

                        output_json = os.path.join(OUTPUT_DIR, f"{job_id}.json")
                        result_txt = "Scan Error"

                        if os.path.exists(output_json):
                            with open(output_json, "r") as f:
                                data = json.load(f)
                                if job_type == "Phishing":
                                    result_txt = data.get("detection", "N/A")
                                elif job_type == "Malware":
                                    result_txt = f"{data.get('summary', {}).get('total_detections', 0)} Detections"

                        update_job_db(job_id, output_json, result_txt)
                        log("JOB-002", f"Finished {job_id} -> {result_txt}")

                    except subprocess.CalledProcessError as e:
                        log("ERR-JOB-01", f"Worker failed {job_id}")
                        if e.stderr:
                            log("ERR-JOB-02", f"Stderr: {e.stderr.strip()}")

            else:
                time.sleep(3)

        except Exception as e:
            log("ERR-SYS-00", f"Dispatcher Loop Error: {e}")
            time.sleep(5)


def start_flask():
    app.run(host="0.0.0.0", port=5000, use_reloader=False)


if __name__ == "__main__":
    test_process = None
    init_db()

    log("SYS-001", "System Initialized")

    try:
        server_thread = threading.Thread(target=start_flask)
        server_thread.daemon = True
        server_thread.start()
        time.sleep(1)
        log("SYS-002", "API Server Online (Port 5000)")

        test_process = subprocess.Popen(
            [sys.executable, TEST_PY_PATH], cwd=TEST_WEBSITE_DIR
        )
        log("SYS-003", "Frontend Service Online (Port 5001)")

        dispatcher()

    except KeyboardInterrupt:
        log("SYS-004", "Shutdown Signal")
    except Exception as e:
        log("ERR-FATAL", f"Critical: {e}")
    finally:
        if test_process:
            test_process.terminate()
            test_process.wait(timeout=5)
        log("SYS-005", "System Offline")

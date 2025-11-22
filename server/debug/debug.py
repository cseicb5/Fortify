import os
import sys
import logging
import requests
import datetime
from flask import Flask, render_template, request, jsonify
from colorama import Fore, Style, init

init()

app = Flask(__name__)
BACKEND_URL = "http://127.0.0.1:5000"

werkzeug_logger = logging.getLogger('werkzeug')
werkzeug_logger.setLevel(logging.ERROR)

def log(code, message):
    color = Fore.WHITE
    if code.startswith("ERR"):
        color = Fore.RED
    elif code.startswith("SYS"):
        color = Fore.CYAN
    elif code.startswith("WEB"):
        color = Fore.GREEN
    
    timestamp = datetime.datetime.now().strftime("%H:%M:%S")
    print(f"{Style.DIM}{timestamp}{Style.RESET_ALL} {color}[{code}]{Style.RESET_ALL} {message}")
    sys.stdout.flush()

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/get_db_data', methods=['GET'])
def get_db_data():
    log("WEB-001", "Fetching DB data from backend")
    try:
        backend_response = requests.get(f'{BACKEND_URL}/seeDbs')
        return jsonify(backend_response.json()), backend_response.status_code
    except requests.exceptions.ConnectionError:
        log("ERR-NET-01", f"Backend unreachable at {BACKEND_URL}")
        return jsonify({"error": "Backend Offline"}), 503

@app.route('/submit_scan', methods=['POST'])
def submit_scan():
    jwt_token = request.form.get('jwt_token')
    scan_type = request.form.get('scan_type')

    if not jwt_token:
        return jsonify({"error": "Token required"}), 400

    headers = {'Authorization': f'Bearer {jwt_token}'}
    log("WEB-002", f"Forwarding scan request: {scan_type}")

    try:
        if scan_type == 'Malware':
            file = request.files.get('file')
            if not file or not file.filename:
                return jsonify({"error": "No file"}), 400
            files = {'file': (file.filename, file.read(), file.content_type)}
            backend_response = requests.post(f'{BACKEND_URL}/scanApk', headers=headers, files=files)
        
        elif scan_type == 'Phishing':
            message = request.form.get('message_text')
            if not message:
                return jsonify({"error": "No message"}), 400
            backend_response = requests.post(f'{BACKEND_URL}/scanMessage', headers=headers, data={'message': message})
        
        else:
            return jsonify({"error": "Invalid Type"}), 400
            
        return jsonify(backend_response.json()), backend_response.status_code

    except requests.exceptions.ConnectionError:
        log("ERR-NET-02", "Backend connection failed")
        return jsonify({"error": "Backend Offline"}), 503
    except Exception as e:
        log("ERR-WEB-01", f"Proxy Error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/check_status', methods=['POST'])
def check_status():
    jwt_token = request.form.get('jwt_token_status')
    job_id = request.form.get('job_id_status')

    if not jwt_token or not job_id:
        return jsonify({"error": "Missing credentials"}), 400

    headers = {'Authorization': f'Bearer {jwt_token}'}
    log("WEB-003", f"Checking status for Job {job_id}")

    try:
        url = f'{BACKEND_URL}/scanStatus'
        params = {'jobID': job_id}
        backend_response = requests.get(url, headers=headers, params=params)
        return jsonify(backend_response.json()), backend_response.status_code
    except requests.exceptions.ConnectionError:
        log("ERR-NET-03", "Backend connection failed")
        return jsonify({"error": "Backend Offline"}), 503
    except Exception as e:
        log("ERR-WEB-02", f"Status Check Error: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    if not os.path.exists('templates'):
        os.makedirs('templates')
    
    log("SYS-INIT", "Frontend Server Starting (Port 5001)")
    app.run(host='0.0.0.0', port=5001)
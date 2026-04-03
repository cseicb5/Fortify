from flask import Flask, request, render_template
from transformers import pipeline
from phishing_api.worker import predict_message_type, load_model
from collections import deque
import threading
import uuid
import signal
import sys

app = Flask(__name__)
job_lock = threading.Lock()
job_condition = threading.Condition(lock=job_lock)
queue = deque()
jobLookup = {}
tokenizer, model = load_model("phishing_api/model")
classifier = pipeline("text-classification", model=model, tokenizer=tokenizer)


class Job:
    def __init__(self, jobID: str, message: str) -> None:
        self.jobID = jobID
        self.message = message
        self.status = 0
        self.confidence = None
        self.detection = None

# -- mother function
def queryJobsDB(queryType: str, param):
    with job_condition:
        if queryType == "createJob":
            queue.append(param)
            job_condition.notify()  # wake up worker
        elif queryType == "getJobDetails":
            return jobLookup[param].status
        elif queryType == "getLatestJob":
            if queue:
                return queue[0]
            else:
                return None
        elif queryType == "updateJobDetails":
            queue.popleft()
            jobLookup[param.jobID] = param
        else:
            print(" -- An unknown query type call was made, returning None")
            return None

# -- routes
@app.route("/scanMessage", methods=["POST"])
def scanMessage():
    print(" -- Got a request for message scan")
    try:
        data = request.get_json()
        message = data.get('message')
        jobID = str(uuid.uuid4())
        if not message:
            print(" -- The message scan request didn't have a 'message' field in it")
            return {"error": "Message field is missing"}, 400
        j = Job(jobID, message)
        queryJobsDB("createJob", j)
        jobLookup[jobID] = queue[-1]
        return {"jobID": jobID}, 200
    except Exception as e:
        print(f" -- An error occurred in the job creation process: {e}")
        return {"error": "Failed to create job, refer to system logs for errors"}, 500

@app.route("/scanStatus", methods=["POST"])
def scanStatus():
    print(" -- Got a request for status check on message")
    try:
        data = request.get_json()
        jobID = data.get('jobID')
        if not jobID:
            print(" -- The status check request didn't have a 'jobID' field in it")
            return {"error": "The jobID field is missing"}, 400
        if jobID not in jobLookup:
            print(" -- The status check was performed on a job that doesn't exist")
            return {"error": "Job not found"}, 404
        status = queryJobsDB("getJobDetails", jobID)
        return {"status": status}, 200
    except Exception as e:
        print(f" -- An error occurred in checking job status: {e}")
        return {"error": "Failed to check job status, refer to system logs for errors"}, 500

@app.route("/getScanDetails", methods=["POST"])
def get_scan_details():
    data = request.get_json()
    jobID = data.get('jobID')
    if not jobID:
        return {"error": "jobID required"}, 400
    try:
        return {"detection": jobLookup[jobID].detection, "confidence": jobLookup[jobID].confidence}, 200
    except Exception as e:
        print(f" -- An error occurred: {e}")
        return {"error": "An exception occurred"}, 500

def message_worker():
    while True:
        with job_condition:
            while not queue:
                job_condition.wait()  # wait efficiently for a new job
            j = queue[0]
        try:
            out = predict_message_type(j.message, classifier)
            j.status = 1
            j.confidence = out[0]['score']
            j.detection = "Safe" if out[0]['label'] == "LABEL_0" else "Phishing"
            queryJobsDB("updateJobDetails", j)
        except Exception as e:
            print(f" -- An error occurred in message_worker: {e}")
            j.status = -1
            j.detection = "Error"
            j.confidence = 0
            queryJobsDB("updateJobDetails", j)

def shutdown_signal_handler(sig, frame):
    print(" -- Shutdown signal received, exiting gracefully...")
    sys.exit(0)

if __name__ == '__main__':
    signal.signal(signal.SIGINT, shutdown_signal_handler)
    signal.signal(signal.SIGTERM, shutdown_signal_handler)

    t1 = threading.Thread(target=message_worker, daemon=True)
    t1.start()
    print(" -- Flask app starting on 0.0.0.0:8000")
    app.run(host='0.0.0.0', port=8000, debug=False)

from flask import Flask , request , render_template
from phishing_api.worker import predict_message_type, load_model
from collections import deque
import threading
import uuid
import time

app = Flask(__name__)
job_lock = threading.Lock()
queue = deque()
jobLookup = {}
tokenizer,model = load_model("phishing_api/model")


class Job:
    def __init__(self,jobID: str, message: str)-> None:
        self.jobID = jobID
        self.message = message
        self.status = 0
        self.confidence = None
        self.detection = None

# -- mother function
def queryJobsDB(queryType: str , param):
    with job_lock:
        if queryType == "createJob": queue.append(param)
        elif queryType == "getJobDetails": return jobLookup[param].status
        elif queryType == "getLatestJob": 
            if queue:
                return queue[0] 
            else: 
                return None 
        elif queryType == "updateJobDetails":
            queue.popleft()
            jobLookup[param.jobID] = param
        else:
            print("An unknown query type call was made returning Null")
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
            print(" -- The message scan request didnt have message field in them")
            return {"error":"Message field is missing"},400
        j = Job(jobID,message)
        queryJobsDB("createJob",j)
        jobLookup[jobID] = queue[-1]
        return {"jobID": jobID}, 200
    except Exception as e:
        print(f" -- An error occured in the job creation process {e}")
        return {"error":"Failed to create job , refer system logs for errors"},500
    

@app.route("/scanStatus",methods=["POST"])
def scanStatus():
    print(" -- Got a request for status check on message")
    try:
        data = request.get_json()
        jobID = data.get('jobID')
        if not Job:
            print(" -- The status check request didnt have job id field in them")
            return {"error":"The job id field is missing"},400
        if jobID not in jobLookup:
            print(" -- The status check was performed on a job that doesnt exists")
            return {"error": "Job not found"}, 404
        status = queryJobsDB("getJobDetails",jobID)
        return {"status":status},200
    except Exception as e:
        print(f" -- An error occured in checking job status {e}")
        return {"error":"Failed to check job statuss , refer system logs for errors"},500

@app.route("/getScanDetails", methods=["POST"])
def get_scan_details():
    data = request.get_json()
    jobID = data.get('jobID')
    if not jobID:
        return {"error": "jobID required"}, 400
    try:
        return {"detection":jobLookup[jobID].detection,"confidence":jobLookup[jobID].confidence},200
    except Exception as e:
        print(f"-- An error {e}")
        return {"error":"An exception occured"},500

def message_worker():
    while True:
        j = queryJobsDB("getLatestJob",None)
        if j is not None:
            out = predict_message_type(j.message,model,tokenizer)
            j.status = 1
            j.confidence = out[0]['score']
            j.detection = "Safe" if out[0]['label']=="LABEL_0" else "Phishing"
            queryJobsDB("updateJobDetails",j) 
        time.sleep(2)

if __name__ == '__main__':
    t1 = threading.Thread(target=message_worker)
    t1.start()
    app.run(host='0.0.0.0',port=8001,debug=True)
    t1.join()
    
    



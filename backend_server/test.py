
import requests
import time

url = "http://localhost:8001"

print(" -- start of test")
print(" -- remember to put the worker sleep for 10s")

jobID = ""

def createJob() -> bool:
    global jobID
    print(" -- creating a new job")
    response = requests.post(url+"/scanMessage",json={"message":"Hey, just checking in — are we still on for the meeting tomorrow at 3 PM? Let me know if you need to reschedule."})
    if(response.status_code != 200):
        print(" -- job creation test failed")
        print(f" -- got status code : {response.status_code}")
        print(f" -- got content : {response.content}")
        exit(1)

    response = response.json()
    jobID = response.get('jobID')
    print(response)
    print(f"-- Got job id as : {jobID}")
    return True

def checkStatus() -> bool:
    print(" -- checking job status")
    response = requests.post(url+"/scanStatus",json={"jobID":jobID})
    if(response.status_code != 200):
        print(" -- job status check test failed")
        print(f" -- got status code : {response.status_code}")
        print(f" -- got content : {response.content}")
        exit(1)
    response = response.json()
    status = response.get('status')
    print(response)
    if status is 0:
        return False
    return True
    
def getReport() -> bool:
    print(" -- getting job report")
    response = requests.post(url+"/getScanDetails",json={"jobID":jobID})
    if(response.status_code != 200):
        print(" -- job status check test failed")
        print(f" -- got status code : {response.status_code}")
        print(f" -- got content : {response.content}")
        exit(1)
    response = response.json()
    print(response)
    return True


createJob()
while checkStatus() is False:
    time.sleep(1)
    checkStatus()
getReport()

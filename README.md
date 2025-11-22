# **Fortify**

**Automated Threat Detection Platform for Android APKs and Phishing Messages.**

Fortify is a containerized security tool designed to analyze Android applications for malware signatures and detect phishing attempts in text messages using machine learning.

## **Quick Start**

You can download the pre-built Server Image from the Releases page.  
The Mobile Application and Browser Extension are available in the build/ folder.

### **1\. Load the Server**

Download fortify-server.tar and load it into Docker:

docker load \-i fortify-server.tar

### **2\. Run the Container**

Start the platform by mapping the ports:

docker run \-p 5000:5000 \-p 5001:5001 fortify-server

### **3\. Access the Dashboard**

* **Frontend:** http://localhost:5001  
* **Backend API:** http://localhost:5000

## **Screenshots**

### **Main Dashboard**

\
<img src="https://github.com/user-attachments/assets/58ae6cab-89b6-46b3-8108-54fe45747c4b" width="100%" alt="Main Dashboard"/>

### **Server Action Logs**

\
<img src="https://github.com/user-attachments/assets/9103e18f-66c4-44ff-9027-0fceaf6a4f9e" width="100%" alt="Server Action"/>

### **Mobile Application**

<p align="center">
  <img src="https://github.com/user-attachments/assets/98f9f7bb-b67e-4a7e-ae19-1eed46af98ae" width="45%" style="display:inline-block; margin-right:10px;" alt="Mobile Interface">
  <img src="https://github.com/user-attachments/assets/2986cf98-5eab-4cc2-a524-43fe75101302" width="45%" style="display:inline-block;" alt="Mobile Scanner">
</p>

### **Browser Extension Preview**

\
<img src="https://github.com/user-attachments/assets/1a93b7c0-0ccb-4d0d-8372-c7b3245cc508" width="100%" alt="Extension Preview 1">

\
<img src="https://github.com/user-attachments/assets/53f60619-f703-4181-8ab2-9e862e48053b" width="100%" alt="Extension Preview 2">

## **Features**

* **Malware Scanner:** Decompiles APKs (using JADX) to find malicious patterns.  
* **Phishing Guard:** AI-driven analysis of SMS content.  
* **Dockerized:** Zero-dependency setup using the provided image.  
* **Secure:** JWT-based authentication for all API endpoints.

## **Tech Stack**

* **Core:** Python 3.11, Flask  
* **Engine:** JADX 1.5.3, Headless Java 17  
* **Container:** Docker (Debian Slim)

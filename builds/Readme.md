# Fortify: Mobile Security Suite (Android Client)

Protect. Prevent. Preserve. A unified mobile security solution combating malware and phishing attacks through advanced static analysis and machine learning.

---

## 📋 Overview

Fortify is a next‑generation Android security application that combines static malware analysis and AI‑powered phishing detection. It acts as the client interface for the Fortify Security Suite, communicating with a powerful backend engine that performs deep inspection on APK files and analyzes text messages for malicious intent.

Unlike traditional antivirus apps, Fortify uses a hybrid client‑server architecture to deliver high‑accuracy results without draining device performance.

---

## ✨ Key Features

### 🛡️ Malware Scanning (Static Analysis)

* **Deep APK Inspection:** Upload `.apk` files to the server for a full “digital autopsy.”
* **Decompilation:** Backend uses JADX to decompile and analyze code.
* **Pattern Detection:** Identifies suspicious behaviors using Regex‑based heuristics.
* **Detailed Reports:** Displays file paths, code traces, and specific threat descriptions.

### 🎣 Phishing Detection (NLP Model)

* **Manual Scan:** Paste any suspicious SMS or message for instant analysis.
* **AI‑Powered Detection:** Uses TF‑IDF + machine learning classifiers.
* **Confidence Score:** Clear verdict (“Phishing” / “Clean”) with percentage accuracy.

### ⚡ Silent Guardian (Background Protection)

* **Real‑time SMS Scanning:** Optional feature that monitors incoming SMS.
* **Instant Alerts:** Notifications only if a threat is detected.
* **Privacy‑Focused:** Background scan runs only when the user enables it.

### 🎨 Modern UI/UX

* Dark, professional theme.
* Color‑coded threat results (Green → Safe, Red → Threat).
* Smooth navigation with a card‑based dashboard.

---

## 🏗️ Architecture

* **Language:** Kotlin
* **Networking:** OkHttp
* **Security:** JWT‑based authentication
* **Background Tasks:** `BroadcastReceiver` + `Service`
* **Persistence:** SharedPreferences

---

## 🚀 Getting Started

### **Prerequisites**

* Android device/emulator (API 26+ recommended)
* Fortify Server running locally (refer to server README)

### **Installation & Usage**

#### 1. Clone the Repository

```bash
git clone https://github.com/NorzOman/Fortify.git
```

#### 2. Install the App

* Navigate to `/builds/` in the cloned repo
* Locate `app-debug.apk`
* Install via:

  * **Physical Device:** Transfer + enable "Install from unknown sources"
  * **Emulator:** Drag and drop the APK into the emulator

#### 3. Run the Backend Server

* Start `core.py`
* Ensure PC and Android device are on the **same Wi‑Fi network**

#### 4. Connect the App

* Open Fortify → Login screen
* Enter your PC's local IP + port (e.g., `http://192.168.1.5:5000`)
* Enter any username → You're in!

---

## 🤝 Team

* **Arshad Shaikh** — Lead Android Developer & UI/UX
* **Tushar** — System Architecture & Backend Logic
* **Raahim** — Machine Learning & AI Model Development
* **Neal** — Research & Documentation

---

## 📄 License

This project is licensed under the **MIT License**. Please refer to the LICENSE file for more details.

---

Built with ❤️ for a safer digital future.


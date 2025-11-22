# Use Python 3.11 Slim as the base image (approx 130MB)
FROM python:3.11-slim

# 1. Install System Dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    default-jre-headless \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# 2. Install JADX (Decompiler used by malware-worker)
RUN wget https://github.com/skylot/jadx/releases/download/v1.5.3/jadx-1.5.3.zip -O jadx.zip \
    && unzip jadx.zip -d /usr/local/jadx \
    && ln -s /usr/local/jadx/bin/jadx /usr/local/bin/jadx \
    && chmod +x /usr/local/jadx/bin/jadx \
    && rm jadx.zip

# 3. Set Working Directory
WORKDIR /app

# 4. Copy Requirements and Install Python Dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. Copy the rest of the application files
COPY . .

# 6. Expose Ports
# 5000 for Backend (main.py)
# 5001 for Frontnd (debug.py)
EXPOSE 5000
EXPOSE 5001

# 7. Start the application
CMD ["python", "main.py"]

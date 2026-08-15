FROM python:3.10-slim

WORKDIR /app

# Install system dependencies required for OpenCV and PyTorch
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libgl1-mesa-glx \
    libglib2.0-0 \
    libgomp1 \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Pre-download InsightFace models to cache directory if needed
COPY download_models.py .
RUN python download_models.py || true

# Copy full application code
COPY . .

EXPOSE 5000

ENV PYTHONUNBUFFERED=1

CMD ["python", "app_server.py"]

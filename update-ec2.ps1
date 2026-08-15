# ==============================================================================
# Script cap nhat Server EC2 thu cong 1-Click (Fintech + FaceID)
# Vi tri: C:\bank\fintech\update-ec2.ps1
# ==============================================================================

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " 🚀 DANG CAP NHAT CODE VA DEPLOY EC2... " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. Pull code moi tu Git
Write-Host "`n[1/4] Pulling code moi tu Git..." -ForegroundColor Yellow
git pull origin sonnh

# 2. Build & Push Docker Images
Write-Host "`n[2/4] Building & Pushing Docker Images (:latest) len Docker Hub..." -ForegroundColor Yellow
docker build -t nigasante/fintech-service:latest ./fintech
docker build -t nigasante/paymentgateway:latest ./paymentgateway
docker build -t nigasante/coreservice:latest ./coreservice
docker build -t nigasante/notificationservice:latest ./notificationservice
docker build -t nigasante/faceid-service:latest ./faceid

docker push nigasante/fintech-service:latest
docker push nigasante/paymentgateway:latest
docker push nigasante/coreservice:latest
docker push nigasante/notificationservice:latest
docker push nigasante/faceid-service:latest

# 3. Dong bo docker-compose.prod.yml, nginx, prometheus sang EC2
Write-Host "`n[3/4] Uploading config files sang EC2..." -ForegroundColor Yellow
scp -i "fintech-key.pem" docker-compose.prod.yml ubuntu@13.213.32.9:~/
scp -i "fintech-key.pem" nginx/nginx.conf ubuntu@13.213.32.9:~/nginx/
scp -i "fintech-key.pem" prometheus/prometheus.yml ubuntu@13.213.32.9:~/prometheus/

# 4. Remote va cap nhat EC2
Write-Host "`n[4/4] Dang goi EC2 de pull & up Image :latest..." -ForegroundColor Yellow
ssh -i "fintech-key.pem" ubuntu@13.213.32.9 "docker compose -f docker-compose.prod.yml pull && docker compose -f docker-compose.prod.yml up -d && docker image prune -f"

Write-Host "`n=========================================" -ForegroundColor Green
Write-Host " 🎉 HOAN THANH! SERVER DA CAP NHAT CODE MOI! " -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

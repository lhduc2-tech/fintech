#!/bin/bash
# =============================================================
# deploy.sh - Script deploy toan bo fintech microservices len K8s
# =============================================================
# 
# CACH DUNG:
#   chmod +x k8s/deploy.sh
#   ./k8s/deploy.sh <dockerhub-username> <version-tag>
#
# Vi du:
#   ./k8s/deploy.sh lhduc2 v1.0
# =============================================================

set -e  # Dung neu co loi

DOCKERHUB_USERNAME=${1:-"YOUR_DOCKERHUB_USERNAME"}
VERSION=${2:-"v1.0"}
NAMESPACE="fintech"

echo "=============================================="
echo " Fintech Microservices - K8s Deploy Script"
echo " DockerHub: $DOCKERHUB_USERNAME"
echo " Version:   $VERSION"
echo " Namespace: $NAMESPACE"
echo "=============================================="

# ── BUOC 1: Build & Push Docker Images ─────────────────────
echo ""
echo ">>> [1/5] Building Docker Images..."

docker build -t $DOCKERHUB_USERNAME/fintech-service:$VERSION ./fintech
docker build -t $DOCKERHUB_USERNAME/paymentgateway:$VERSION ./paymentgateway
docker build -t $DOCKERHUB_USERNAME/coreservice:$VERSION ./coreservice
docker build -t $DOCKERHUB_USERNAME/notificationservice:$VERSION ./notificationservice

echo ""
echo ">>> [2/5] Pushing Images to Docker Hub..."

docker push $DOCKERHUB_USERNAME/fintech-service:$VERSION
docker push $DOCKERHUB_USERNAME/paymentgateway:$VERSION
docker push $DOCKERHUB_USERNAME/coreservice:$VERSION
docker push $DOCKERHUB_USERNAME/notificationservice:$VERSION

# ── BUOC 2: Tao Namespace ───────────────────────────────────
echo ""
echo ">>> [3/5] Setting up Kubernetes Namespace..."

kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# ── BUOC 3: Tao Secrets ─────────────────────────────────────
echo ""
echo ">>> [4/5] Creating Kubernetes Secrets..."
echo "    (doc tu file .env cua tung service)"

# Tao fintech-secrets tu .env file
kubectl create secret generic fintech-secrets \
  --namespace=$NAMESPACE \
  --from-env-file=./fintech/.env \
  --dry-run=client -o yaml | kubectl apply -f -

# Tao Kafka CA cert secret
kubectl create secret generic kafka-ca-cert \
  --namespace=$NAMESPACE \
  --from-file=ca.pem=./fintech/ca.pem \
  --dry-run=client -o yaml | kubectl apply -f -

# ── BUOC 4: Cap nhat image tags trong YAML va Deploy ────────
echo ""
echo ">>> [5/5] Deploying to Kubernetes..."

# Thay the placeholder bang gia tri thuc
sed "s|<YOUR_DOCKERHUB_USERNAME>|$DOCKERHUB_USERNAME|g; s|:v1.0|:$VERSION|g" \
  k8s/coreservice-deployment.yaml | kubectl apply -f -

sed "s|<YOUR_DOCKERHUB_USERNAME>|$DOCKERHUB_USERNAME|g; s|:v1.0|:$VERSION|g" \
  k8s/fintech-deployment.yaml | kubectl apply -f -

sed "s|<YOUR_DOCKERHUB_USERNAME>|$DOCKERHUB_USERNAME|g; s|:v1.0|:$VERSION|g" \
  k8s/paymentgateway-deployment.yaml | kubectl apply -f -

sed "s|<YOUR_DOCKERHUB_USERNAME>|$DOCKERHUB_USERNAME|g; s|:v1.0|:$VERSION|g" \
  k8s/notificationservice-deployment.yaml | kubectl apply -f -

kubectl apply -f k8s/ingress.yaml -n $NAMESPACE

# ── BUOC 5: Kiem tra ────────────────────────────────────────
echo ""
echo ">>> Dang doi pods khoi dong..."
kubectl rollout status deployment/coreservice-deployment -n $NAMESPACE
kubectl rollout status deployment/fintech-deployment -n $NAMESPACE
kubectl rollout status deployment/paymentgateway-deployment -n $NAMESPACE
kubectl rollout status deployment/notificationservice-deployment -n $NAMESPACE

echo ""
echo "=============================================="
echo " DEPLOY THANH CONG!"
echo "=============================================="
echo ""
echo "Trang thai pods:"
kubectl get pods -n $NAMESPACE
echo ""
echo "Services:"
kubectl get services -n $NAMESPACE
echo ""
echo "De test local, chay:"
echo "  kubectl port-forward svc/fintech-service 8082:8082 -n $NAMESPACE"
echo "  kubectl port-forward svc/paymentgateway-service 8080:8080 -n $NAMESPACE"

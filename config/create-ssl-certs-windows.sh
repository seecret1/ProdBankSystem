#!/bin/bash

# ============================================
# SSL Certificate Generator for Kafka
# For Windows (Git Bash / WSL)
# ============================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
PASSWORD="changeit"
DAYS=3650
CN="localhost"
OUTPUT_DIR="./certificates"

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}   Kafka SSL Certificates Generator        ${NC}"
echo -e "${GREEN}   For Windows (Git Bash / WSL)            ${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""

# Check if openssl is installed
if ! command -v openssl &> /dev/null; then
    echo -e "${RED}ERROR: openssl not found!${NC}"
    echo "Please install OpenSSL:"
    echo "  - Download from: https://slproweb.com/products/Win32OpenSSL.html"
    echo "  - Or install via: choco install openssl (if you have Chocolatey)"
    echo "  - Or use WSL with: sudo apt-get install openssl"
    exit 1
fi

# Check if keytool is available (should be in PATH if Java is installed)
if ! command -v keytool &> /dev/null; then
    echo -e "${RED}ERROR: keytool not found!${NC}"
    echo "Please make sure Java is installed and in PATH"
    echo "  - Download: https://adoptium.net/"
    exit 1
fi

echo -e "${YELLOW}Using password: ${PASSWORD}${NC}"
echo -e "${YELLOW}Valid for: ${DAYS} days${NC}"
echo -e "${YELLOW}Output directory: ${OUTPUT_DIR}${NC}"
echo ""

# Create output directory
mkdir -p "$OUTPUT_DIR"
cd "$OUTPUT_DIR" || exit 1

echo -e "${GREEN}[1/9] Creating CA certificate...${NC}"
openssl req -new -x509 -days "${DAYS}" -keyout ca.key -out ca.crt \
    -subj "/CN=Kafka-CA" \
    -passout pass:"${PASSWORD}" 2>/dev/null

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to create CA certificate${NC}"
    exit 1
fi
echo -e "${GREEN}✓ CA certificate created${NC}"

echo -e "${GREEN}[2/9] Creating server keystore...${NC}"
keytool -keystore kafka.keystore.jks \
    -alias localhost \
    -validity "${DAYS}" \
    -genkey -keyalg RSA \
    -dname "CN=${CN}, OU=Development, O=Kafka, L=City, S=State, C=RU" \
    -storepass "${PASSWORD}" \
    -keypass "${PASSWORD}" 2>/dev/null

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to create keystore${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Keystore created${NC}"

echo -e "${GREEN}[3/9] Creating CSR (Certificate Signing Request)...${NC}"
keytool -keystore kafka.keystore.jks \
    -alias localhost \
    -certreq -file kafka.csr \
    -storepass "${PASSWORD}" \
    -keypass "${PASSWORD}" 2>/dev/null

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to create CSR${NC}"
    exit 1
fi
echo -e "${GREEN}✓ CSR created${NC}"

cat > kafka-san.cnf <<EOF
[ v3_req ]
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = localhost
IP.1 = 127.0.0.1
EOF

echo -e "${GREEN}[4/9] Signing certificate with CA...${NC}"
openssl x509 -req -CA ca.crt -CAkey ca.key \
    -in kafka.csr -out kafka-signed.crt \
    -days "${DAYS}" -CAcreateserial \
    -passin pass:"${PASSWORD}" \
    -extfile kafka-san.cnf -extensions v3_req 2>/dev/null

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to sign certificate${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Certificate signed${NC}"

echo -e "${GREEN}[5/9] Importing CA into keystore...${NC}"
keytool -keystore kafka.keystore.jks \
    -alias CARoot \
    -import -file ca.crt \
    -storepass "${PASSWORD}" \
    -noprompt 2>/dev/null

echo -e "${GREEN}[6/9] Importing signed certificate into keystore...${NC}"
keytool -keystore kafka.keystore.jks \
    -alias localhost \
    -import -file kafka-signed.crt \
    -storepass "${PASSWORD}" \
    -noprompt 2>/dev/null

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to import signed certificate${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Certificates imported to keystore${NC}"

echo -e "${GREEN}[7/9] Creating truststore...${NC}"
keytool -keystore kafka.truststore.jks \
    -alias CARoot \
    -import -file ca.crt \
    -storepass "${PASSWORD}" \
    -noprompt 2>/dev/null

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to create truststore${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Truststore created${NC}"

echo -e "${GREEN}[8/9] Creating client keystore (optional)...${NC}"
# Create client certificate (for microservices)
openssl req -new -x509 -days "${DAYS}" -keyout client.key -out client.crt \
    -subj "/CN=client-service" \
    -passout pass:"${PASSWORD}" 2>/dev/null

keytool -keystore client.keystore.jks \
    -alias client \
    -validity "${DAYS}" \
    -genkey -keyalg RSA \
    -dname "CN=client-service, OU=Development, O=Kafka, L=City, S=State, C=RU" \
    -storepass "${PASSWORD}" \
    -keypass "${PASSWORD}" 2>/dev/null

keytool -keystore client.keystore.jks \
    -alias client \
    -certreq -file client.csr \
    -storepass "${PASSWORD}" \
    -keypass "${PASSWORD}" 2>/dev/null

openssl x509 -req -CA ca.crt -CAkey ca.key \
    -in client.csr -out client-signed.crt \
    -days "${DAYS}" -CAcreateserial \
    -passin pass:"${PASSWORD}" 2>/dev/null

keytool -keystore client.keystore.jks \
    -alias CARoot \
    -import -file ca.crt \
    -storepass "${PASSWORD}" \
    -noprompt 2>/dev/null

keytool -keystore client.keystore.jks \
    -alias client \
    -import -file client-signed.crt \
    -storepass "${PASSWORD}" \
    -noprompt 2>/dev/null

echo -e "${GREEN}✓ Client certificates created${NC}"

echo -e "${GREEN}[9/9] Verifying certificates...${NC}"
echo ""
echo -e "${YELLOW}Keystore contents:${NC}"
keytool -list -keystore kafka.keystore.jks -storepass "${PASSWORD}" 2>/dev/null | grep -E "alias|Trusted"

echo ""
echo -e "${YELLOW}Truststore contents:${NC}"
keytool -list -keystore kafka.truststore.jks -storepass "${PASSWORD}" 2>/dev/null | grep -E "alias|Trusted"

echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}✅ All certificates created successfully!${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo -e "Files created in: ${YELLOW}${OUTPUT_DIR}${NC}"
echo ""
echo -e "${YELLOW}Main files:${NC}"
echo -e "  ${GREEN}✓${NC} kafka.keystore.jks  - Server keystore (contains private key)"
echo -e "  ${GREEN}✓${NC} kafka.truststore.jks - Truststore (contains CA certificate)"
echo ""
echo -e "${YELLOW}Additional files (for microservices):${NC}"
echo -e "  ${GREEN}✓${NC} client.keystore.jks - Client keystore (for order-service, card-service)"
echo -e "  ${GREEN}✓${NC} ca.crt               - CA certificate"
echo ""
echo -e "${YELLOW}Password: ${PASSWORD}${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo -e "  1. Copy certificates to your service directories:"
echo -e "     ${GREEN}cp ${OUTPUT_DIR}/client.keystore.jks ../order-service/src/main/resources/kafka.keystore.jks${NC}"
echo -e "     ${GREEN}cp ${OUTPUT_DIR}/kafka.truststore.jks ../order-service/src/main/resources/kafka.truststore.jks${NC}"
echo -e "     ${GREEN}cp ${OUTPUT_DIR}/client.keystore.jks ../card-service/src/main/resources/kafka.keystore.jks${NC}"
echo -e "     ${GREEN}cp ${OUTPUT_DIR}/kafka.truststore.jks ../card-service/src/main/resources/kafka.truststore.jks${NC}"
echo ""
echo -e "  2. Update application.yml with:"
echo -e "     ${YELLOW}spring.kafka.ssl.enabled: true${NC}"
echo -e "     ${YELLOW}spring.kafka.ssl.trust-store-location: classpath:kafka.truststore.jks${NC}"
echo -e "     ${YELLOW}spring.kafka.ssl.key-store-location: classpath:kafka.keystore.jks${NC}"
echo ""

# Ask if user wants to copy files automatically
read -p "Do you want to copy certificates to services automatically? (y/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # Get project root (assuming script is in project root/certificates/)
    PROJECT_ROOT=$(cd ../ && pwd)

    echo -e "${GREEN}Copying certificates...${NC}"

    # Copy to order-service
    if [ -d "$PROJECT_ROOT/order-service/src/main/resources" ]; then
        cp client.keystore.jks "$PROJECT_ROOT/order-service/src/main/resources/kafka.keystore.jks"
        cp kafka.truststore.jks "$PROJECT_ROOT/order-service/src/main/resources/"
        echo -e "  ${GREEN}✓${NC} Copied to order-service/src/main/resources/"
    fi

    # Copy to card-service
    if [ -d "$PROJECT_ROOT/card-service/src/main/resources" ]; then
        cp kafka.truststore.jks "$PROJECT_ROOT/card-service/src/main/resources/"
        cp client.keystore.jks "$PROJECT_ROOT/card-service/src/main/resources/kafka.keystore.jks"
        echo -e "  ${GREEN}✓${NC} Copied to card-service/src/main/resources/"
    fi

    echo -e "${GREEN}✅ Certificates copied successfully!${NC}"
fi

echo ""
echo -e "${GREEN}Done!${NC}"
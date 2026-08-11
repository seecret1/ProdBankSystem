#!/bin/bash

# Переменные
PASSWORD=changeit
DAYS=3650
CN="localhost"

echo "=== Creating SSL certificates for Kafka ==="

# 1. CA Certificate
echo "1. Creating CA certificate..."
openssl req -new -x509 -days ${DAYS} -keyout ca.key -out ca.crt -subj "/CN=Kafka-CA"

# 2. Server Keystore
echo "2. Creating server keystore..."
keytool -keystore kafka.keystore.jks \
    -alias localhost \
    -validity ${DAYS} \
    -genkey -keyalg RSA \
    -dname "CN=${CN}, OU=Development, O=Kafka, L=City, S=State, C=RU" \
    -storepass ${PASSWORD} \
    -keypass ${PASSWORD}

# 3. Create CSR
echo "3. Creating CSR..."
keytool -keystore kafka.keystore.jks \
    -alias localhost \
    -certreq -file kafka.csr \
    -storepass ${PASSWORD} \
    -keypass ${PASSWORD}

cat > kafka-san.cnf <<EOF
[ v3_req ]
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = localhost
IP.1 = 127.0.0.1
EOF

# 4. Sign certificate
echo "4. Signing certificate..."
openssl x509 -req -CA ca.crt -CAkey ca.key -in kafka.csr -out kafka-signed.crt -days ${DAYS} -CAcreateserial -passin pass:${PASSWORD} -extfile kafka-san.cnf -extensions v3_req

# 5. Import CA and signed cert into keystore
echo "5. Importing certificates into keystore..."
keytool -keystore kafka.keystore.jks -alias CARoot -import -file ca.crt -storepass ${PASSWORD} -noprompt
keytool -keystore kafka.keystore.jks -alias localhost -import -file kafka-signed.crt -storepass ${PASSWORD}

# 6. Create truststore
echo "6. Creating truststore..."
keytool -keystore kafka.truststore.jks -alias CARoot -import -file ca.crt -storepass ${PASSWORD} -noprompt

echo "=== Certificates created successfully ==="
echo "Files:"
echo "  - kafka.keystore.jks (contains client private key)"
echo "  - kafka.truststore.jks (contains CA certificate)"
echo "Password: ${PASSWORD}"
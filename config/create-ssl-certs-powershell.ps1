# PowerShell SSL Certificate Generator for Kafka
# This script generates SSL certificates for Kafka brokers and clients

param(
    [string]$OutputDir = ".\certificates",
    [string]$Password = "changeit",
    [int]$Days = 3650,
    [string]$CN = "localhost"
)

# Colors
$Green = "Green"
$Red = "Red"
$Yellow = "Yellow"

function Write-Success {
    Write-Host "✓ $args" -ForegroundColor $Green
}

function Write-Error-Custom {
    Write-Host "✗ $args" -ForegroundColor $Red
}

function Write-Info {
    Write-Host "ℹ $args" -ForegroundColor $Yellow
}

Write-Host "============================================" -ForegroundColor $Green
Write-Host "   Kafka SSL Certificates Generator        " -ForegroundColor $Green
Write-Host "   PowerShell Version                       " -ForegroundColor $Green
Write-Host "============================================" -ForegroundColor $Green
Write-Host ""

# Create output directory
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
    Write-Success "Created output directory: $OutputDir"
}

$OutputDir = (Get-Item $OutputDir).FullName
Push-Location $OutputDir

try {
    # Generate CA certificate
    Write-Info "[1/5] Generating CA certificate..."
    $CAKey = "ca.key"
    $CACert = "ca.crt"

    # Using OpenSSL via WSL if available, otherwise use Docker
    $wslAvailable = $true
    try {
        $wslVersion = wsl -e bash -c "echo 'WSL available'" 2>&1
    } catch {
        $wslAvailable = $false
    }

    if ($wslAvailable) {
        Write-Info "Using WSL for certificate generation..."

        # Copy the corrected bash script to WSL and run it
        wsl -e bash -c @"
            #!/bin/bash
            set -e
            cd /tmp/kafka-certs
            mkdir -p /tmp/kafka-certs

            PASSWORD="$Password"
            DAYS=$Days
            CN="$CN"

            # Create CA
            openssl req -new -x509 -days \$DAYS -keyout ca.key -out ca.crt \
                -subj '/CN=Kafka-CA' -passout pass:\$PASSWORD

            # Create server keystore
            keytool -keystore kafka.keystore.jks \
                -alias localhost \
                -validity \$DAYS \
                -genkey -keyalg RSA \
                -dname "CN=\$CN,OU=Development,O=Kafka,L=City,S=State,C=RU" \
                -storepass \$PASSWORD \
                -keypass \$PASSWORD 2>/dev/null || true

            # Create CSR
            keytool -keystore kafka.keystore.jks \
                -alias localhost \
                -certreq -file kafka.csr \
                -storepass \$PASSWORD \
                -keypass \$PASSWORD 2>/dev/null || true

            cat > kafka-san.cnf <<'EOF'
[ v3_req ]
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = localhost
IP.1 = 127.0.0.1
EOF

            # Sign certificate with CA
            openssl x509 -req -CA ca.crt -CAkey ca.key \
                -in kafka.csr -out kafka-signed.crt \
                -days \$DAYS -CAcreateserial \
                -passin pass:\$PASSWORD \
                -extfile kafka-san.cnf -extensions v3_req 2>/dev/null || true

            # Import CA and signed cert
            keytool -keystore kafka.keystore.jks \
                -alias CARoot \
                -import -file ca.crt \
                -storepass \$PASSWORD \
                -noprompt 2>/dev/null || true

            keytool -keystore kafka.keystore.jks \
                -alias localhost \
                -import -file kafka-signed.crt \
                -storepass \$PASSWORD \
                -noprompt 2>/dev/null || true

            # Create truststore
            keytool -keystore kafka.truststore.jks \
                -alias CARoot \
                -import -file ca.crt \
                -storepass \$PASSWORD \
                -noprompt 2>/dev/null || true

            # Copy to Windows
            cp ca.* kafka.* /mnt/c/Users/u3aka/IdeaProjects/ProdBankSystem/certificates/ 2>/dev/null || true
"@

        Write-Success "Certificates generated via WSL"
    } else {
        Write-Error-Custom "WSL not available. Please install WSL2 or OpenSSL on Windows."
        Write-Info "Alternative: Use Docker to generate certificates:"
        Write-Info "  docker run --rm -v $(pwd):/certs alpine/openssl ..."
        exit 1
    }

    Write-Host ""
    Write-Info "Generated files:"
    Get-ChildItem -Filter "*.jks" -o @{$true=@{$false=$_.Mode}} | ForEach-Object {
        Write-Info "  - $($_.Name)"
    }
    Get-ChildItem -Filter "*.crt" -o @{$true=@{$false=$_.Mode}} | ForEach-Object {
        Write-Info "  - $($_.Name)"
    }

    Write-Host ""
    Write-Success "All certificates created successfully!"
    Write-Info "Password: $Password"

} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Next steps:" -ForegroundColor $Yellow
Write-Host "1. Update docker-compose.yml with SSL configuration"
Write-Host "2. Copy certificates to Docker volumes"
Write-Host "3. Update application properties for Kafka SSL"
Write-Host ""


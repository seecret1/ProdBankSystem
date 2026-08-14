package com.github.seecret1.userservice.utils;

import com.github.seecret1.userservice.config.EncryptionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class EncryptionUtils {

    private final EncryptionProperties encryptionProperties;

    /**
     * Шифрует строку
     */
    public String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    encryptionProperties.getSecretKey().getBytes(StandardCharsets.UTF_8),
                    encryptionProperties.getAlgorithm()
            );

            Cipher cipher = Cipher.getInstance(encryptionProperties.getTransformation());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt", e);
        }
    }

    /**
     * Дешифрует строку
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    encryptionProperties.getSecretKey().getBytes(StandardCharsets.UTF_8),
                    encryptionProperties.getAlgorithm()
            );

            Cipher cipher = Cipher.getInstance(encryptionProperties.getTransformation());
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt", e);
        }
    }
}
package com.github.seecret1.userservice.utils;

import lombok.experimental.UtilityClass;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@UtilityClass
public class EncryptionUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    // Ключ должен быть 16, 24 или 32 байта для AES-128/192/256
    private static final String SECRET_KEY = "0123456789abcdef";

    /**
     * Шифрует строку
     */
    public String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM
            );

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
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
                    SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM
            );

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt", e);
        }
    }
}
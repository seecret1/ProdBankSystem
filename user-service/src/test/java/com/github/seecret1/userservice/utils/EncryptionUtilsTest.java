package com.github.seecret1.userservice.utils;

import com.github.seecret1.userservice.config.EncryptionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EncryptionUtils Tests")
class EncryptionUtilsTest {

    @Mock
    private EncryptionProperties encryptionProperties;

    @InjectMocks
    private EncryptionUtils encryptionUtils;

    private static final String TEST_ALGORITHM = "AES";
    private static final String TEST_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String TEST_SECRET_KEY = "1234567890123456";
    private static final String TEST_PLAIN_TEXT = "Hello, World!";

    @BeforeEach
    void setUp() {
        lenient().when(encryptionProperties.getAlgorithm()).thenReturn(TEST_ALGORITHM);
        lenient().when(encryptionProperties.getTransformation()).thenReturn(TEST_TRANSFORMATION);
        lenient().when(encryptionProperties.getSecretKey()).thenReturn(TEST_SECRET_KEY);
    }

    @Test
    @DisplayName("Should encrypt plain text successfully")
    void shouldEncryptPlainTextSuccessfully() {
        String data = TEST_PLAIN_TEXT;

        String result = encryptionUtils.encrypt(data);

        assertThat(result).isNotNull();
        assertThat(result).isNotEqualTo(data);

        String decrypted = encryptionUtils.decrypt(result);
        assertThat(decrypted).isEqualTo(data);
    }

    @Test
    @DisplayName("Should return null when encrypting null")
    void shouldReturnNullWhenEncryptingNull() {
        String result = encryptionUtils.encrypt(null);

        assertThat(result).isNull();
        verify(encryptionProperties, never()).getAlgorithm();
        verify(encryptionProperties, never()).getTransformation();
        verify(encryptionProperties, never()).getSecretKey();
    }

    @Test
    @DisplayName("Should return empty string when encrypting empty string")
    void shouldReturnEmptyStringWhenEncryptingEmptyString() {
        String result = encryptionUtils.encrypt("");

        assertThat(result).isEmpty();
        verify(encryptionProperties, never()).getAlgorithm();
        verify(encryptionProperties, never()).getTransformation();
        verify(encryptionProperties, never()).getSecretKey();
    }

    @Test
    @DisplayName("Should throw RuntimeException when encryption fails")
    void shouldThrowRuntimeExceptionWhenEncryptionFails() {
        when(encryptionProperties.getAlgorithm()).thenReturn("INVALID_ALGORITHM");

        assertThatThrownBy(() -> encryptionUtils.encrypt(TEST_PLAIN_TEXT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to encrypt");
    }

    @Test
    @DisplayName("Should encrypt and produce valid Base64 output")
    void shouldEncryptAndProduceValidBase64Output() {
        String data = TEST_PLAIN_TEXT;
        String result = encryptionUtils.encrypt(data);
        assertThat(result).matches("^[A-Za-z0-9+/]+=*$");
    }


    @Test
    @DisplayName("Should decrypt encrypted text successfully")
    void shouldDecryptEncryptedTextSuccessfully() {
        String encrypted = encryptionUtils.encrypt(TEST_PLAIN_TEXT);
        String result = encryptionUtils.decrypt(encrypted);
        assertThat(result).isEqualTo(TEST_PLAIN_TEXT);
    }

    @Test
    @DisplayName("Should return null when decrypting null")
    void shouldReturnNullWhenDecryptingNull() {
        String result = encryptionUtils.decrypt(null);

        assertThat(result).isNull();
        verify(encryptionProperties, never()).getAlgorithm();
        verify(encryptionProperties, never()).getTransformation();
        verify(encryptionProperties, never()).getSecretKey();
    }

    @Test
    @DisplayName("Should return empty string when decrypting empty string")
    void shouldReturnEmptyStringWhenDecryptingEmptyString() {
        String result = encryptionUtils.decrypt("");

        assertThat(result).isEmpty();
        verify(encryptionProperties, never()).getAlgorithm();
        verify(encryptionProperties, never()).getTransformation();
        verify(encryptionProperties, never()).getSecretKey();
    }

    @Test
    @DisplayName("Should throw RuntimeException when decrypting invalid data")
    void shouldThrowRuntimeExceptionWhenDecryptingInvalidData() {
        String invalidEncrypted = "invalidBase64Data!@#$";

        assertThatThrownBy(() -> encryptionUtils.decrypt(invalidEncrypted))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to decrypt");
    }

    @Test
    @DisplayName("Should throw RuntimeException when decrypting with wrong key")
    void shouldThrowRuntimeExceptionWhenDecryptingWithWrongKey() {
        String encrypted = encryptionUtils.encrypt(TEST_PLAIN_TEXT);

        when(encryptionProperties.getSecretKey()).thenReturn("6543210987654321");

        assertThatThrownBy(() -> encryptionUtils.decrypt(encrypted))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to decrypt");
    }

    @Test
    @DisplayName("Should encrypt and decrypt correctly for various inputs")
    void shouldEncryptAndDecryptCorrectlyForVariousInputs() {
        String[] testData = {
                "Hello, World!",
                "1234567890",
                "Special chars: !@#$%^&*()",
                "Long text: " + "A".repeat(1000)
        };

        for (String data : testData) {
            String encrypted = encryptionUtils.encrypt(data);
            String decrypted = encryptionUtils.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(data);
        }
    }

    @Test
    @DisplayName("Should produce same encrypted results for same input in ECB mode")
    void shouldProduceSameEncryptedResultsForSameInput() {
        String data = TEST_PLAIN_TEXT;

        String encrypted1 = encryptionUtils.encrypt(data);
        String encrypted2 = encryptionUtils.encrypt(data);

        assertThat(encrypted1).isEqualTo(encrypted2);
    }

    @Test
    @DisplayName("Should handle very large data")
    void shouldHandleVeryLargeData() {
        String largeData = "X".repeat(10000);

        String encrypted = encryptionUtils.encrypt(largeData);
        String decrypted = encryptionUtils.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(largeData);
    }

    @Test
    @DisplayName("Should handle whitespace strings")
    void shouldHandleWhitespaceStrings() {
        String data = "   ";

        String encrypted = encryptionUtils.encrypt(data);
        String decrypted = encryptionUtils.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(data);
    }

    @Test
    @DisplayName("Should handle single character")
    void shouldHandleSingleCharacter() {
        String data = "A";

        String encrypted = encryptionUtils.encrypt(data);
        String decrypted = encryptionUtils.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(data);
    }

    @Test
    @DisplayName("Should handle null cipher initialization gracefully")
    void shouldHandleNullCipherInitializationGracefully() {
        when(encryptionProperties.getTransformation()).thenReturn(null);

        assertThatThrownBy(() -> encryptionUtils.encrypt(TEST_PLAIN_TEXT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to encrypt");
    }

    @Test
    @DisplayName("Should encrypt and decrypt within acceptable time")
    void shouldEncryptAndDecryptWithinAcceptableTime() {
        String data = "A".repeat(1000);
        long startTime = System.currentTimeMillis();

        String encrypted = encryptionUtils.encrypt(data);
        String decrypted = encryptionUtils.decrypt(encrypted);
        long endTime = System.currentTimeMillis();

        assertThat(decrypted).isEqualTo(data);
        assertThat(endTime - startTime).isLessThan(1000);
    }
}
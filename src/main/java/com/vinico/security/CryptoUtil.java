package com.vinico.security;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utilidades criptográficas para validación de firmas SHA-256 y HMAC.
 * Equivalente Java de las funciones crypto.* del spec TypeScript.
 */
@Component
public class CryptoUtil {

    private static final String SHA256 = "SHA-256";
    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Calcula SHA-256 de un payload y retorna hex string.
     */
    public String sha256Hex(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256);
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    /**
     * Calcula HMAC-SHA256 sobre timestamp.rawBody con la clave secreta.
     * Para verificación de webhooks (prevention de replay attacks).
     */
    public String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error HMAC-SHA256", e);
        }
    }

    /**
     * Comparación de tiempo constante (timing-safe) para firmas HMAC.
     */
    public boolean timingSafeEquals(String a, String b) {
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }

    /**
     * Genera token hex aleatorio de n bytes (igual a crypto.randomBytes).
     */
    public String randomHex(int bytes) {
        byte[] random = new byte[bytes];
        new SecureRandom().nextBytes(random);
        return bytesToHex(random);
    }

    /**
     * Genera PIN numérico de 4 dígitos (1000-9999).
     */
    public String generatePin() {
        int pin = 1000 + new SecureRandom().nextInt(9000);
        return String.valueOf(pin);
    }

    /**
     * Mock de tx hash Polygon (66 chars: "0x" + 64 hex).
     */
    public String mockPolygonTx() {
        return "0x" + randomHex(32);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

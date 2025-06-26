package com.example.gasteiapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

// Classe utilitária para realizar o hashing e verificação de senhas de forma segura.
// Utiliza SHA-256 com salt para proteger as senhas armazenadas.
public class PasswordHasher {

    private static final String ALGORITHM = "SHA-256"; // Algoritmo de hashing
    private static final int SALT_SIZE = 16; // Tamanho do salt em bytes

    // Gera o hash de uma senha com um salt aleatório.
    // Retorna a combinação do salt e do hash da senha em formato hexadecimal.
    public static String hashPassword(String password) {
        try {
            byte[] salt = generateSalt(); // Gera um salt aleatório
            byte[] hashedPassword = hash(password, salt); // Gera o hash da senha com o salt
            byte[] combined = new byte[salt.length + hashedPassword.length];
            // Combina o salt e o hash da senha
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            return bytesToHex(combined); // Converte para hexadecimal e retorna
        } catch (NoSuchAlgorithmException e) {
            // Lança uma exceção em caso de algoritmo de hashing não encontrado
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    // Verifica se uma senha fornecida corresponde a um hash armazenado.
    // Retorna true se a senha for válida, false caso contrário.
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            byte[] combined = hexToBytes(storedHash); // Converte o hash armazenado de hexadecimal para bytes
            byte[] salt = Arrays.copyOfRange(combined, 0, SALT_SIZE); // Extrai o salt
            byte[] storedHashedPassword = Arrays.copyOfRange(combined, SALT_SIZE, combined.length); // Extrai o hash da senha
            byte[] hashedPassword = hash(password, salt); // Gera o hash da senha fornecida com o salt extraído
            return Arrays.equals(storedHashedPassword, hashedPassword); // Compara os hashes
        } catch (NoSuchAlgorithmException e) {
            // Lança uma exceção em caso de algoritmo de hashing não encontrado
            throw new RuntimeException("Failed to verify password", e);
        }
    }

    // Gera um salt criptograficamente seguro.
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);
        return salt;
    }

    // Gera o hash de uma senha usando o algoritmo SHA-256 e um salt.
    private static byte[] hash(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        digest.update(salt);
        return digest.digest(password.getBytes());
    }

    // Converte um array de bytes para uma string hexadecimal.
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Converte uma string hexadecimal para um array de bytes.
    private static byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
}

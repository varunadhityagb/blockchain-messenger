package blockchain;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class Crypto {

    public static KeyPair deserializeKeyPair() {
        try (ObjectInputStream privateInput = new ObjectInputStream(new FileInputStream("private_key.ser"));
             ObjectInputStream publicInput = new ObjectInputStream(new FileInputStream("public_key.ser"))) {

            PrivateKey privateKey = (PrivateKey) privateInput.readObject();
            PublicKey publicKey = (PublicKey) publicInput.readObject();

            return new KeyPair(publicKey, privateKey);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void serializeKeyPair(KeyPair keyPair) {
        try (ObjectOutputStream privateOutput = new ObjectOutputStream(new FileOutputStream("private_key.ser"));
             ObjectOutputStream publicOutput = new ObjectOutputStream(new FileOutputStream("public_key.ser"))) {

            privateOutput.writeObject(keyPair.getPrivate());
            publicOutput.writeObject(keyPair.getPublic());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Key loadKeyFromFile(String fileName) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (Key) ois.readObject();
        }
    }

    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

package blockchain;

import java.io.*;
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
}

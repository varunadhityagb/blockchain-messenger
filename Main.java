import App.MainFrame;
import blockchain.Crypto;

import javax.swing.*;
import java.io.*;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String publicKeyString;
        String userName;

        KeyPair keyPair;
        File privateKeyFile = new File("private_key.ser");
        File publicKeyFile = new File("public_key.ser");
        File userNameFile = new File("userName.ser");

        if (!privateKeyFile.exists() && !publicKeyFile.exists()) {
            keyPair = Crypto.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            Crypto.serializeKeyPair(keyPair);
        } else {
            keyPair = Crypto.deserializeKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            System.out.println(publicKeyString);
        }
        if (!userNameFile.exists()) {
            userName = JOptionPane.showInputDialog(null, "Enter your name: ");
            ObjectOutputStream userNameWrite = new ObjectOutputStream(new FileOutputStream("userName.ser"));
            userNameWrite.writeObject(userName);
        } else {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("userName.ser"));
            userName = (String) ois.readObject();
        }
        MainFrame mainFrame = new MainFrame(userName);
    }
}

import App.MainFrame;
import blockchain.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        String publicKeyString;
        PublicKey publicKey;
        String userName;

        KeyPair keyPair;
        File privateKeyFile = new File("private_key.ser");
        File publicKeyFile = new File("public_key.ser");
        File userNameFile = new File("userName.ser");
        File blockChainBackup = new File("blockchain.ser");

        if (!privateKeyFile.exists() && !publicKeyFile.exists()) {
            keyPair = Crypto.generateKeyPair();
            publicKey = keyPair.getPublic();
            publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            Crypto.serializeKeyPair(keyPair);
        } else {
            keyPair = Crypto.deserializeKeyPair();
            publicKey = keyPair.getPublic();
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

        if (!blockChainBackup.exists()) {
            Block initialiser = new Block("0");
            PublicKey randomPublicKey = (PublicKey) DigitalSignature.decodeKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjKGTupVK4+nBZB9n/JFpELpefQu1NZ8fKZoGTYUqg140v2oVkE+jsMdRN+DwK7YiJSpZD7OjjnkUl0OYxNpX6xS10JvGELHK6YzZhwiSocHB7QScoSoWwhTyq9WOWYbIi7ZZ9nyM9rfhqvIunSz+M0OF2qcUov2OB5IFZxnOz9e5YwECkiHcu/IOPOIHFGBi7VtuXAX2ZzdSZEWXoR+1EC9q69PkTYLilpPYsE15/yy9kQK4WQy3PD5S/g/qPNO7+u070Ex2hE3Nfyw9BavA/X6f0fnrVrqfYyxSL0nWNUOGUaLGIZ36Ah7WrEET054zHnlo36DBBdUeTb+oLGCYowIDAQAB", "RSA", true);
            initialiser.addUserKeyPair(userName, publicKey);
            initialiser.setMessage(new Message("blockZERO", randomPublicKey, randomPublicKey));
            BlockChain blockChain = new BlockChain();
            blockChain.addBlock(initialiser);
            blockChain.serializeBlockChain("blockchain.ser");
        }

        MainFrame mainFrame = new MainFrame(userName);
    }
}

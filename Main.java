import App.MainFrame;
import blockchain.Block;
import blockchain.BlockChain;
import blockchain.Crypto;
import blockchain.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.*;
import java.security.*;
import java.util.Base64;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, InvalidKeyException {
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
            initialiser.setMessage(new Message("blockZERO", publicKey, publicKey));
            BlockChain blockChain = new BlockChain();
            blockChain.addBlock(initialiser);
            blockChain.serializeBlockChain("blockchain.ser");
        }

        MainFrame mainFrame = new MainFrame(userName);
    }
}

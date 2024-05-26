import App.MainFrame;
import App.UserAlreadyExistsException;
import App.Widgets.ChatOption;
import blockchain.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static class userMultiCast extends MultiCast {

        public userMultiCast(String address, int port) throws IOException {
            super(address, port);
        }

        @Override
        public void receiveMessage() {
            while (true) {
                try {
                    byte[] buffer = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    byte[] data = packet.getData();
                    ByteArrayInputStream bais = new ByteArrayInputStream(data);
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Message receivedMessage = (Message) ois.readObject();
                    InetAddress sourceAddress = packet.getAddress();
                    String s = sourceAddress.toString();
                    InetAddress localHost = Inet4Address.getLocalHost();
                    String ipv4Address = "/" + localHost.getHostAddress();
                    if (!s.equals(ipv4Address) && receivedMessage.getContent().startsWith("uSeRaDdEd")) {
                        boolean contains = false;
                        String[] messageParts = receivedMessage.getContent().split("0");
                        String user = messageParts[1];
                        PublicKey userPublicKey = (PublicKey) receivedMessage.getPublicKey();
                        BlockChain blockChain = BlockChain.deserializeBlockChain("blockchain.ser");
                        Block lastBlock = blockChain.getLastBlock();
                        HashMap<String, PublicKey> tempMap = lastBlock.userKeyPairs;
                        for (Map.Entry<String, PublicKey> entry : tempMap.entrySet()) {
                            if (entry.getValue().equals(userPublicKey)) {
                                contains = true;
                                break;
                            }
                        }
                        if (!contains) {
                            lastBlock.addUserKeyPair(user, userPublicKey);
                            blockChain.addBlock(lastBlock);
                            blockChain.serializeBlockChain("blockchain.ser");
                        }

                    }
                } catch (NoSuchPaddingException | IllegalBlockSizeException | IOException |
                         NoSuchAlgorithmException |
                         BadPaddingException | InvalidKeyException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        String publicKeyString;
        PublicKey publicKey;
        String userName;

        KeyPair keyPair;
        File privateKeyFile = new File("private_key.ser");
        File publicKeyFile = new File("public_key.ser");
        File userNameFile = new File("userName.ser");
        File blockChainBackup = new File("blockchain.ser");

        if (!userNameFile.exists()) {
            userName = JOptionPane.showInputDialog(null, "Enter your name: ");
            ObjectOutputStream userNameWrite = new ObjectOutputStream(new FileOutputStream("userName.ser"));
            userNameWrite.writeObject(userName);
        } else {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("userName.ser"));
            userName = (String) ois.readObject();
        }

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

        if (!blockChainBackup.exists()) {
            Block initialiser = new Block("0");
            PublicKey randomPublicKey = (PublicKey) DigitalSignature.decodeKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjKGTupVK4+nBZB9n/JFpELpefQu1NZ8fKZoGTYUqg140v2oVkE+jsMdRN+DwK7YiJSpZD7OjjnkUl0OYxNpX6xS10JvGELHK6YzZhwiSocHB7QScoSoWwhTyq9WOWYbIi7ZZ9nyM9rfhqvIunSz+M0OF2qcUov2OB5IFZxnOz9e5YwECkiHcu/IOPOIHFGBi7VtuXAX2ZzdSZEWXoR+1EC9q69PkTYLilpPYsE15/yy9kQK4WQy3PD5S/g/qPNO7+u070Ex2hE3Nfyw9BavA/X6f0fnrVrqfYyxSL0nWNUOGUaLGIZ36Ah7WrEET054zHnlo36DBBdUeTb+oLGCYowIDAQAB", "RSA", true);
            initialiser.addUserKeyPair(userName, publicKey);
            initialiser.setMessage(new Message("blockZERO", randomPublicKey, randomPublicKey));
            BlockChain blockChain = new BlockChain();
            blockChain.addBlock(initialiser);
            blockChain.serializeBlockChain("blockchain.ser");
        }

        Message myKeyMessage = new Message("uSeRaDdEd" + "0" + userName, publicKey, publicKey);

        userMultiCast userMultiCast = new userMultiCast("239.255.255.250", 3333);

        Thread senderThread = new Thread(() -> {
            while (true) {
                try {
                    userMultiCast.sendMessage(myKeyMessage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        senderThread.start();
        new Thread(userMultiCast::receiveMessage).start();

        MainFrame mainFrame = new MainFrame(userName);
    }
}

package App;

import App.Widgets.ChatOption;
import blockchain.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Map;

public class MainFrame {

    private PublicKey myPublicKey;
    private PrivateKey myPrivateKey;
    private JPanel userPanel;
    private String userName;
    BlockChain blockChain;

    static class userSender extends MultiCast {
        public userSender(String address, int port) throws IOException {
            super(address, port);
        }

        @Override
        public void receiveMessage() {

        }
    }

    public MainFrame(String userName) throws IOException, ClassNotFoundException {
        this.myPublicKey = (PublicKey) Crypto.loadKeyFromFile("public_key.ser");
        this.myPrivateKey = (PrivateKey) Crypto.loadKeyFromFile("private_key.ser");
        this.blockChain = BlockChain.deserializeBlockChain("blockchain.ser");
        this.userName = userName;

        userSender userSender = new userSender("239.255.255.250",3333);

        SkeletonFrame frame = new SkeletonFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(331,720);
        frame.setResizable(false);
        JPanel topPanel = new JPanel();
        topPanel.setPreferredSize(new Dimension(100,50));
        topPanel.setBackground(new Color(39, 48, 67));
        topPanel.setLayout(new BorderLayout(40,0));
        frame.add(topPanel, BorderLayout.NORTH);

        ImageIcon settingsIcon = new ImageIcon("App/images/info.png");
        ImageIcon newIcon = new ImageIcon("App/images/plus.png");

        JButton addChat = new JButton(newIcon);
        JLabel title = new JLabel("Messenger");
        JButton settings = new JButton(settingsIcon);

        title.setForeground(Color.lightGray);
        title.setFont(new Font("", Font.BOLD,25));

        topPanel.add(addChat, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);
        topPanel.add(settings, BorderLayout.EAST);

        topPanel.revalidate();

        userPanel = new JPanel();
        userPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10,15));
        userPanel.setBackground(new Color(39, 48, 67));

        Block lastBlock = blockChain.getLastBlock();
        new Thread(() -> {
            Component[] components;
            ArrayList<String> arrayOfUsers = new ArrayList<String>();
            while (true) {
                components = userPanel.getComponents();
                for (Component component : components) {
                    arrayOfUsers.add(((ChatOption) component).getText());
                }

                BlockChain b = BlockChain.deserializeBlockChain("blockchain.ser");
                for (Map.Entry<String, PublicKey> entry : b.getLastBlock().userKeyPairs.entrySet()) {
                    if (arrayOfUsers.contains(entry.getKey()))
                        continue;
                    if (entry.getValue().equals(myPublicKey))
                        continue;
                    String key = entry.getKey();
                    ChatOption chatOption = new ChatOption(key);
                    userPanel.add(chatOption);
                    userPanel.revalidate();
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted.");
                }
            }
        }).start();

        addChat.addActionListener(e -> {
            blockChain = BlockChain.deserializeBlockChain("blockchain.ser");
            String name = null;
            PublicKey publicKey = null;
            String publicKeyString;

            do {
                name = JOptionPane.showInputDialog(null, "Enter the name of the user you want to chat with: ");
                try {
                    if (name.equals(userName))
                        throw new UserAlreadyExistsException("User already exists");
                    else {
                        for (Map.Entry<String, PublicKey> entry : lastBlock.userKeyPairs.entrySet()) {
                            if (entry.getKey().equals(name))
                                throw new UserAlreadyExistsException("User already exists");
                        }
                    }
                    do {
                        publicKeyString = JOptionPane.showInputDialog(null, "Enter the public key of the user you want to chat with: ");

                        try {
                            publicKey = (PublicKey) DigitalSignature.decodeKey(publicKeyString, "RSA", true);
                            BlockChain blockChain = BlockChain.deserializeBlockChain("blockchain.ser");
                            for (Map.Entry<String, PublicKey> entry : blockChain.getLastBlock().userKeyPairs.entrySet()) {
                                if (entry.getValue().equals(publicKey))
                                    throw new UserAlreadyExistsException("User already exists");
                                else if (publicKey.equals(myPublicKey))
                                    throw new UserAlreadyExistsException("User already exists");
                            }
                            ChatOption chatOption = new ChatOption(name);
                            userPanel.add(chatOption);
                            userPanel.revalidate();
                            blockChain.getLastBlock().userKeyPairs.put(name, publicKey);
                            Message message = new Message(("uSeRaDdEd" + "0" + name), publicKey, publicKey);

                            userSender.sendMessage(message);
                            blockChain.serializeBlockChain("blockchain.ser");

                        } catch (InvalidKeySpecException | IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(null, "Invalid public key", "Error", JOptionPane.ERROR_MESSAGE);
                        } catch (NullPointerException ex) {
                            break;
                        } catch (NoSuchPaddingException | IllegalBlockSizeException | IOException | NoSuchAlgorithmException |
                                 BadPaddingException | InvalidKeyException | ClassNotFoundException | SignatureException ex) {
                            throw new RuntimeException(ex);
                        } catch (UserAlreadyExistsException ex) {
                            JOptionPane.showMessageDialog(null, "User already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                            break;
                        }

                    } while (publicKey == null);
                } catch (UserAlreadyExistsException ex) {
                    name = null;
                    JOptionPane.showMessageDialog(null, "User already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NullPointerException ex) {
                    break;
                }
            } while (name == null);
        });

        settings.addActionListener(e -> {
            InfoPage infoPage = new InfoPage(userName);
        });

        frame.add(userPanel);
        userPanel.revalidate();
    }


}
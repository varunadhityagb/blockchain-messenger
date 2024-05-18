package App;

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
import java.util.Map;

public class MainFrame {
    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private PublicKey myPublicKey;
    private PrivateKey myPrivateKey;
    private JPanel userPanel;
    BlockChain blockChain;

    public MainFrame(String userName) throws IOException, ClassNotFoundException {
        this.group = InetAddress.getByName("239.255.255.250");
        this.port = 5555;
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(new InetSocketAddress(group, port), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
        this.myPublicKey = (PublicKey) Crypto.loadKeyFromFile("public_key.ser");
        this.myPrivateKey = (PrivateKey) Crypto.loadKeyFromFile("private_key.ser");
        this.blockChain = BlockChain.deserializeBlockChain("blockchain.ser");

        SkeletonFrame frame = new SkeletonFrame();
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
        for (Map.Entry<String, PublicKey> entry : lastBlock.userKeyPairs.entrySet()) {
            String key = entry.getKey();
            ChatOption chatOption = new ChatOption(key);
            userPanel.add(chatOption);
        }

        addChat.addActionListener(e -> {
            String name;
            PublicKey publicKey = null;
            String publicKeyString;

            do {
                name = JOptionPane.showInputDialog(null, "Enter the name of the user you want to chat with: ");
                if (name == null) break;
                publicKeyString = JOptionPane.showInputDialog(null, "Enter the public key of the user you want to chat with: ");

                try {
                    publicKey = (PublicKey) DigitalSignature.decodeKey(publicKeyString, "RSA", true);
                    ChatOption chatOption = new ChatOption(name);
                    userPanel.add(chatOption);
                    userPanel.revalidate();
                    Block newBlock = new Block(lastBlock.hash);
                    newBlock.userKeyPairs = lastBlock.userKeyPairs;
                    newBlock.addUserKeyPair(name, publicKey);
                    Message message = new Message(("uSeRaDdEd" + "0" + name), publicKey, publicKey);
                    newBlock.setMessage(message);
                    sendMessage(message);


                } catch (InvalidKeySpecException | IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid public key", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NullPointerException ex) {
                    break;
                } catch (NoSuchPaddingException | IllegalBlockSizeException | IOException | NoSuchAlgorithmException |
                         BadPaddingException | InvalidKeyException | ClassNotFoundException | SignatureException ex) {
                    throw new RuntimeException(ex);
                }

            } while (publicKey == null);
        });

        frame.add(userPanel);
        userPanel.revalidate();

        new Thread(this::receiveMessage).start();
    }

    public void sendMessage(Message message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        oos.flush();
        byte[] buffer = baos.toByteArray();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
    }

    public void receiveMessage() {
        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (true) {
            try {
                socket.receive(packet);
                byte[] data = packet.getData();
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais);
                Message receivedMessage = (Message) ois.readObject();
                InetAddress sourceAddress = packet.getAddress();
                String s = sourceAddress.toString();
                InetAddress localHost = Inet4Address.getLocalHost();
                String ipv4Address = "/" + localHost.getHostAddress();
                if (!s.equals(ipv4Address)) {
                    BlockChain blockChain = BlockChain.deserializeBlockChain("blockchain.ser");
                    Block lastBlock = blockChain.getLastBlock();
                    String newUser = receivedMessage.getContent().split("0")[1];
                    PublicKey newKey = receivedMessage.getPublicKey();
                    Block newBlock = new Block(lastBlock.hash);
                    newBlock.userKeyPairs = lastBlock.userKeyPairs;
                    newBlock.addUserKeyPair(newUser, newKey);
                    newBlock.setMessage(receivedMessage);
                    blockChain.addBlock(newBlock);
                    blockChain.serializeBlockChain("blockchain.ser");
                    System.out.println("updated blockchain");
                    userPanel.add(new ChatOption(newUser));
                    userPanel.revalidate();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}



//new Color(39, 48, 67)
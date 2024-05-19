package App;

import App.Widgets.MessageBubble;
import blockchain.BlockChain;
import blockchain.Crypto;
import blockchain.DigitalSignature;
import blockchain.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Date;

public class ChatFrame {
    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private JPanel chatArea;
    private PublicKey myPublicKey;
    private PublicKey toPublicKey;
    private PrivateKey myPrivateKey;

    public ChatFrame(String name) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, IOException, InvalidKeyException, ClassNotFoundException {
        this.group = InetAddress.getByName("239.255.255.250");
        this.port = 8888;
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(new InetSocketAddress(group, port), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
        this.myPublicKey = (PublicKey) Crypto.loadKeyFromFile("public_key.ser");
        this.myPrivateKey = (PrivateKey) Crypto.loadKeyFromFile("private_key.ser");

        BlockChain blockChain = BlockChain.deserializeBlockChain("blockchain.ser");
        this.toPublicKey = blockChain.getLastBlock().userKeyPairs.get(name);

        SkeletonFrame frame = new SkeletonFrame();

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(39, 48, 67));
        titlePanel.setPreferredSize(new Dimension(1000,50));
        JLabel title = new JLabel(name);
        ImageIcon spy = new ImageIcon("App/images/spy.png");
        title.setForeground(Color.lightGray);
        title.setFont(new Font("", Font.BOLD,25));
        title.setIcon(spy);
        titlePanel.add(title);
        frame.add(titlePanel, BorderLayout.NORTH);

        chatArea = new JPanel();
        chatArea.setLayout(new GridBagLayout());
        chatArea.setPreferredSize(new Dimension(1000, 500));
        chatArea.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        chatArea.add(new JLabel(), gbc); // Filler to keep components at the top


        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel sendPanel = getjPanel(chatArea);

        frame.add(sendPanel, BorderLayout.SOUTH);

        addMessageBubble(chatArea, "Hello", true);

        Thread messageListener = new Thread(this::receiveMessage);
        messageListener.start();


    }

    private JPanel getjPanel(JPanel chatArea) {
        JPanel sendPanel = new JPanel();
        sendPanel.setPreferredSize(new Dimension(1000,50));
        JTextField messesgeField = new JTextField();
        messesgeField.setPreferredSize(new Dimension(400,50));
        sendPanel.add(messesgeField, BorderLayout.CENTER);

        ImageIcon sendIcon = new ImageIcon("App/images/send.png");

        JButton sendButton = new JButton(sendIcon);
        sendButton.addActionListener(e -> {
            try {
                String message = messesgeField.getText();
                if (!message.isEmpty()) {
                    sendMessage(new Message(message, toPublicKey, myPublicKey));
                    addMessageBubble(chatArea, message, false);
                    messesgeField.setText("");
                }
            } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                     BadPaddingException | InvalidKeyException | ClassNotFoundException | SignatureException ex) {
                throw new RuntimeException(ex);
            }
        });
        sendPanel.add(sendButton, BorderLayout.EAST);
        return sendPanel;
    }

    private void addMessageBubble(JPanel chatArea, String text, boolean isSender) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        MessageBubble bubble = new MessageBubble(text, isSender);
        chatArea.add(bubble, gbc);
        chatArea.revalidate();
        chatArea.repaint();
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
                if (!s.equals(ipv4Address) && DigitalSignature.verify(receivedMessage.getContent(), receivedMessage.getSignature(), this.toPublicKey)) {
                   addMessageBubble(chatArea, receivedMessage.getContent(), true);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


/*
    *  Thanks to https://github.com/DJ-Raven/java-jpanel-round-border
*/
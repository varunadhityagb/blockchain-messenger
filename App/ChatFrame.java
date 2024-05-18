package App;

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
    private PublicKey myPublicKey;
    private PublicKey toPublicKey;
    private PrivateKey myPrivateKey;

    ChatFrame(String key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, IOException, InvalidKeyException, ClassNotFoundException {
        this.group = InetAddress.getByName("239.255.255.250");
        this.port = 8888;
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(new InetSocketAddress(group, port), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
        this.myPublicKey = (PublicKey) Crypto.loadKeyFromFile("public_key.ser");
        this.myPrivateKey = (PrivateKey) Crypto.loadKeyFromFile("private_key.ser");
        this.toPublicKey = this.myPublicKey; //(PublicKey) DigitalSignature.decodeKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAocPRtL2WoxQrrlExD1Aqku+kaBX7GGI16EbpO6qxNukvrIsSjyrdtIM9EPUnrvYCyn6HhHh/Qd9xxMr8k2ems3WxjDr9Zi1TL1wKUY91LfgiZDlNRtnJ1wKxIjGhCQc7y3cxVDyzzvsOXPU3IX5h9jtqF+HCFdRRNXKPO5fBFuWR4i1h1MeneTflLXHYosdZjlSfZD8SdpXayLKBcGP3/+2yUwonZK0QsubEiuKbPEI7CNgLQz/f5f2SysC0uwUPi4R680X1UPKdKcSxeLHScibb4dbX/ANas5/ZR01bsdlQbtjWwykIZ9LSXwnvYfRhiIz/GNMPI6zn30bI8cuDmQIDAQAB", "RSA", true);

        SkeletonFrame frame = new SkeletonFrame();

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(39, 48, 67));
        titlePanel.setPreferredSize(new Dimension(1000,50));
        frame.add(titlePanel, BorderLayout.NORTH);

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(119, 136, 153));
        chatArea.setFont(new Font("", Font.PLAIN, 20));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel sendPanel = getjPanel(chatArea);

        frame.add(sendPanel, BorderLayout.SOUTH);

        new Thread(this::receiveMessage).start();


    }

    private JPanel getjPanel(JTextArea chatArea) {
        JPanel sendPanel = new JPanel();
        sendPanel.setPreferredSize(new Dimension(1000,50));
        JTextField messesgeField = new JTextField();
        messesgeField.setPreferredSize(new Dimension(400,50));
        messesgeField.addActionListener(e -> {
            try {
                String message = messesgeField.getText();
                sendMessage(new Message(message, toPublicKey, myPublicKey));
                chatArea.append(message + "\n");
                messesgeField.setText("");
            } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                     BadPaddingException | InvalidKeyException | ClassNotFoundException | SignatureException ex) {
                throw new RuntimeException(ex);
            }
        });
        sendPanel.add(messesgeField, BorderLayout.CENTER);

        ImageIcon sendIcon = new ImageIcon("App/images/send.png");

        JButton sendButton = new JButton(sendIcon);
        sendButton.addActionListener(e -> {
            try {
                sendMessage(new Message(messesgeField.getText(), toPublicKey, myPublicKey));
            } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                     BadPaddingException | InvalidKeyException | ClassNotFoundException | SignatureException ex) {
                throw new RuntimeException(ex);
            }
        });
        sendPanel.add(sendButton, BorderLayout.EAST);
        return sendPanel;
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
                    System.out.println("Received message: " + receivedMessage.getContent() + ", Timestamp: " + new Date(receivedMessage.getTimeStamp()));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}

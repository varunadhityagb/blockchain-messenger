package App;

import App.Widgets.MessageBubble;
import blockchain.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.concurrent.atomic.AtomicReference;

public class ChatFrame {
    private JPanel chatArea;
    private PublicKey myPublicKey;
    private PublicKey toPublicKey;
    private PrivateKey myPrivateKey;


    public ChatFrame(String name) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, IOException, InvalidKeyException, ClassNotFoundException {
        this.myPublicKey = (PublicKey) Crypto.loadKeyFromFile("public_key.ser");
        this.myPrivateKey = (PrivateKey) Crypto.loadKeyFromFile("private_key.ser");
        AtomicReference<BlockChain> blockChain = new AtomicReference<>(BlockChain.deserializeBlockChain("blockchain.ser"));
        this.toPublicKey = blockChain.get().getLastBlock().userKeyPairs.get(name);

        class ChatMultiCast extends MultiCast {

            public ChatMultiCast(String address, int port, JPanel panel, PublicKey toPublicKey) throws IOException {
                super(address, port, panel, toPublicKey);
            }

            @Override
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
                            BlockChain blockChain = BlockChain.deserializeBlockChain("blockchain.ser");
                            Block lastBlock = blockChain.getLastBlock();
                            Block newBlock = new Block(lastBlock.hash);
                            newBlock.userKeyPairs = lastBlock.userKeyPairs;
                            newBlock.setMessage(receivedMessage);
                            blockChain.addBlock(newBlock);
                            blockChain.serializeBlockChain("blockchain.ser");
                            addMessageBubble(chatArea, receivedMessage.getContent(), true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        ChatMultiCast chatMultiCast = new ChatMultiCast("239.255.255.250", 8888, chatArea, this.toPublicKey);

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

        JPanel sendPanel = new JPanel();
        sendPanel.setPreferredSize(new Dimension(1000,50));
        JTextField messesgeField = new JTextField();
        messesgeField.setPreferredSize(new Dimension(400,50));
        sendPanel.add(messesgeField, BorderLayout.CENTER);

        ImageIcon sendIcon = new ImageIcon("App/images/send.png");

        JButton sendButton = new JButton(sendIcon);
        sendButton.addActionListener(e -> {
            try {
                blockChain.set(BlockChain.deserializeBlockChain("blockchain.ser"));
                Block newBlock = new Block(blockChain.get().getLastBlock().hash);
                String message = messesgeField.getText();
                Message newMessage = new Message(message, toPublicKey, myPublicKey);
                newBlock.userKeyPairs = blockChain.get().getLastBlock().userKeyPairs;
                newBlock.setMessage(newMessage);
                blockChain.get().addBlock(newBlock);
                blockChain.get().serializeBlockChain("blockchain.ser");
                if (!message.isEmpty()) {
                    chatMultiCast.sendMessage(newMessage);
                    addMessageBubble(chatArea, message, false);
                    messesgeField.setText("");
                }
            } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                     BadPaddingException | InvalidKeyException | ClassNotFoundException | SignatureException ex) {
                throw new RuntimeException(ex);
            }
        });
        sendPanel.add(sendButton, BorderLayout.EAST);

        frame.add(sendPanel, BorderLayout.SOUTH);

        for (int i = 0; i < blockChain.get().size(); i++) {
            if (blockChain.get().getBlock(i).getMessage().getContent().equals("blockZERO")) {
                continue;
            } else if (blockChain.get().getBlock(i).getMessage().getContent().startsWith("uSeRaDdEd")) {
                continue;
            }
            try {
                Message temp = blockChain.get().getBlock(i).getMessage();
                if (temp.getSenderKey().equals(myPublicKey) && temp.getPublicKey().equals(toPublicKey) ) {
                    addMessageBubble(chatArea, blockChain.get().getBlock(i).getMessage().getContent(), false);
                } else if (temp.getPublicKey().equals(myPublicKey) && temp.getSenderKey().equals(toPublicKey))
                   addMessageBubble(chatArea, blockChain.get().getBlock(i).getMessage().getContent(), true);
            } catch(Exception e) {
                throw new RuntimeException();
            }
            chatArea.revalidate();
            chatArea.repaint();
        }


        Thread messageListener = new Thread(chatMultiCast::receiveMessage);
        messageListener.start();


    }

    private static void addMessageBubble(JPanel chatArea, String text, boolean isSender) {
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
}


/*
    *  Thanks to https://github.com/DJ-Raven/java-jpanel-round-border
*/
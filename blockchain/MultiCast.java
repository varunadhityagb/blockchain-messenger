package blockchain;

import App.Widgets.ChatOption;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.security.PublicKey;
import java.util.Map;

public class MultiCast {
    public MulticastSocket socket;
    public InetAddress group;
    public int port;
    public String userName;
    public PublicKey myPublicKey;
    public JPanel panel;

    public MultiCast(String address, int port) throws IOException {
        this.port = port;
        this.group = InetAddress.getByName(address);
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(new InetSocketAddress(group, port), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
    }

    public MultiCast(String address, int port, String userName, PublicKey myPublicKey, JPanel panel) throws IOException {
        this(address, port);
        this.myPublicKey = myPublicKey;
        this.userName = userName;
        this.panel = panel;
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
                    Block newBlock;
                    if (panel != null && !lastBlock.userKeyPairs.containsKey(newUser)) {
                        for (Map.Entry<String, PublicKey> entry : lastBlock.userKeyPairs.entrySet()) {
                            if (!entry.getValue().equals(newKey)) {
                                newBlock = new Block(lastBlock.hash);
                                newBlock.userKeyPairs = lastBlock.userKeyPairs;
                                newBlock.addUserKeyPair(newUser, newKey);
                                newBlock.setMessage(receivedMessage);
                                blockChain.addBlock(newBlock);
                                blockChain.serializeBlockChain("blockchain.ser");
                                System.out.println("updated blockchain -- user " + newUser + " added");
                                if (!newUser.equals(userName) && !entry.getValue().equals(myPublicKey)) {
                                    panel.add(new ChatOption(newUser));
                                }
                                panel.revalidate();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

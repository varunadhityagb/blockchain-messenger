package blockchain;


import javax.swing.*;
import java.io.*;
import java.net.*;
import java.security.PublicKey;

abstract public class MultiCast {
    public MulticastSocket socket;
    public InetAddress group;
    public int port;
    public String userName;
    public PublicKey toPublicKey;
    public JPanel panel;

    public MultiCast(String address, int port) throws IOException {
        this.port = port;
        this.group = InetAddress.getByName(address);
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(new InetSocketAddress(group, port), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
    }

    public MultiCast(String address, int port, JPanel panel, PublicKey toPublicKey) throws IOException {
        this(address, port);
        this.toPublicKey = toPublicKey;
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

    abstract public void receiveMessage();
}

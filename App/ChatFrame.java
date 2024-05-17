package App;

import blockchain.DigitalSignature;
import blockchain.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Date;

public class ChatFrame {

    ChatFrame(String text) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, IOException, InvalidKeyException, ClassNotFoundException {
        SkeletonFrame frame = new SkeletonFrame();
        JPanel chatArea = new JPanel();
        chatArea.setBackground(Color.BLACK);

        JPanel messageArea = new JPanel();
        sendMessage(text);


    }

    public void sendMessage(String text) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, SignatureException, IOException, InvalidKeyException, ClassNotFoundException {
        MulticastSocket socket = new MulticastSocket(8888);
        InetAddress group = InetAddress.getByName("239.255.255.250");
        socket.joinGroup(new InetSocketAddress(group, 8888), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));

        PublicKey pk = (PublicKey) DigitalSignature.decodeKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArjuF1MYVmT4cj0/MA6JhfvnF6h/J6xKajmrh2OFdpv1YPA+jWlHoLtR1oi4G8vMGcZRk/dftSIvCVyyMWYeMPScenfI9UAhNwcTMzQQ4C+RfmaQ3gWWoUSf0D8DXetvaB5cFMydIBhCgLfKiw9+KVqbphJzQMO6h5d4xMm1eZ/B8NY8Kk3bCA0iR7QCiAzC7RyCxhrqmyVK43avzzH5YWTyzNdFutLEg5paEQY0jm9gYs5lcSeJoPmN4ON7+5oxg3l10Gh9MrORvS3bvbH4o+x2e4kCb4DjH8dnqkZpZLzr4/S27sh4twH1XN7igrzFLiLzAOHDGIzrqIqAqYeyrbQIDAQAB", "RSA", true);
        Message message = new Message(text, pk, pk);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        oos.flush();
        byte[] buffer = baos.toByteArray();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 8888);
        socket.send(packet);

    }

    public void recieveMessage() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] buffer =  new byte[4096];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        MulticastSocket socket = new MulticastSocket(8888);
        InetAddress group = InetAddress.getByName("239.255.255.250");
        socket.joinGroup(new InetSocketAddress(group, 8888), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));

        while (true) {
            try {
                socket.receive(packet);
                byte[] data = packet.getData();
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais);
                blockchain.Message receivedMessage = (Message) ois.readObject();
                InetAddress sourceAddress = packet.getAddress();
                String s = sourceAddress.toString();
                InetAddress localHost = Inet4Address.getLocalHost();
                String ipv4Address = "/" + localHost.getHostAddress();
                PublicKey pk = (PublicKey) DigitalSignature.decodeKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArjuF1MYVmT4cj0/MA6JhfvnF6h/J6xKajmrh2OFdpv1YPA+jWlHoLtR1oi4G8vMGcZRk/dftSIvCVyyMWYeMPScenfI9UAhNwcTMzQQ4C+RfmaQ3gWWoUSf0D8DXetvaB5cFMydIBhCgLfKiw9+KVqbphJzQMO6h5d4xMm1eZ/B8NY8Kk3bCA0iR7QCiAzC7RyCxhrqmyVK43avzzH5YWTyzNdFutLEg5paEQY0jm9gYs5lcSeJoPmN4ON7+5oxg3l10Gh9MrORvS3bvbH4o+x2e4kCb4DjH8dnqkZpZLzr4/S27sh4twH1XN7igrzFLiLzAOHDGIzrqIqAqYeyrbQIDAQAB", "RSA", true);
                System.out.println(s);
                System.out.println("here");
                System.out.println(ipv4Address);
                //if (!s.equals(ipv4Address) && DigitalSignature.verify(receivedMessage.getContent(), receivedMessage.getSignature(), pk)){
                //    System.out.println("Received message: " + receivedMessage.getContent() + ", Timestamp: " + new Date(receivedMessage.getTimestamp()));
                //}
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                packet.setLength(buffer.length);
            }
        }
    }
}

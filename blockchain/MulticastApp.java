package blockchain;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.Base64;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.security.*;

public class MulticastApp extends Thread{
    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private volatile boolean running = true;
    private PublicKey receiverKey;
    private Block initialiser;

    public MulticastApp(String multicastAddress, int port) throws IOException {
        this.group = InetAddress.getByName(multicastAddress);
        this.port = port;
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(new InetSocketAddress(group, port), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
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
      

    public void run() {
        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (running) {
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
                if (!s.equals(ipv4Address)&& DigitalSignature.verify(receivedMessage.getContent(), receivedMessage.getSignature(), this.receiverKey)){
                    System.out.println("Received message: " + receivedMessage.getContent() + ", Timestamp: " + new Date(receivedMessage.getTimestamp()));  
                    BlockChain blockChain = BlockChain.deserializeBlockChain("blockchain.ser"); 
                    blockChain.addBlock(new Block("0"));
                    blockChain.getLastBlock().setMessage(receivedMessage);
                    blockChain.serializeBlockChain("blockchain.ser");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                //System.out.println("IOException: " + e.getMessage());
            } finally {
                packet.setLength(buffer.length);
            }
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException {
    	MulticastApp m = new MulticastApp("239.255.255.250", 8888);
        java.util.Scanner sc = new java.util.Scanner(System.in);
        KeyPair keyPair = null;

        // Check if key pair files exist
        File privateKeyFile = new File("private_key.ser");
        File publicKeyFile = new File("public_key.ser");
        File blockChainFile = new File("blockchain.ser");

        if (!privateKeyFile.exists() && !publicKeyFile.exists()) {
            keyPair = Crypto.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());   
            System.out.print("Enter your name: ");
            System.out.println("Name: "+sc.nextLine());
            System.out.println("PublicKey: "+publicKeyString);
            System.out.println();
            Crypto.serializeKeyPair(keyPair);
        } else {
            // Deserialize the key pair
            keyPair = Crypto.deserializeKeyPair();
            // publicKeyString is taken from local file
            PublicKey publicKey = keyPair.getPublic();
            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            System.out.println("Public Key: " + publicKeyString);
        }       

        
        m.initialiser = new Block("0");
        if(blockChainFile.exists()){
            BlockChain blockChain = BlockChain.deserializeBlockChain("blockchain.ser");
            blockChain.addBlock(m.initialiser);
        } else {
            BlockChain blockChain = new BlockChain();
            blockChain.addBlock(m.initialiser);
            blockChain.serializeBlockChain("blockchain.ser");
        }

        
        System.out.println("Enter receiver's Public Key: ");
        String receiverPublicKeyString = sc.nextLine();
        System.out.println("Enter receiver's name: ");
        String receiverName = sc.nextLine();
        m.receiverKey = (PublicKey) DigitalSignature.decodeKey(receiverPublicKeyString, "RSA", true);
        m.initialiser.addUserKeyPair(receiverName, (PublicKey) DigitalSignature.decodeKey(receiverPublicKeyString, "RSA", true));
        m.start();
        while (true) {
            System.out.print("Enter message: ");
            String text = sc.nextLine();
            if (text.equalsIgnoreCase("exit")) {
                System.exit(0);
            }
            Message message = new Message(text, m.initialiser.getPublicKey(receiverName));
            m.sendMessage(message);
            // add a new block with the message in it
            BlockChain blockChain = BlockChain.deserializeBlockChain("blockchain.ser");
            blockChain.addBlock(new Block("0"));
            blockChain.getLastBlock().setMessage(message);
            blockChain.serializeBlockChain("blockchain.ser");
        }
    }
}

// varun's:
// MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnYj4q57u6ZaIY89IYK2NYk0W1I1l9Yd7+HBbAwGq5hnM8glGvgRfLfQqJM4ElmmU8Wha+pSdtW8PkM8iEgbeCXtD7at9i63HQlZ0sfcT40UbQObTnlT1aOWFNeTEb74P8hpjGMxE0oc9GGS2tmXBc8y6zQUBcx4JYNFnE4Hnfzq/n6vn+Rj/QRrrHo56HoiJF9+aIp/ux68JK6rFpoOD6FuNK/C4hkOHyyNuEIGQS8U21WTNqGclZO+SrVIQSJqihWyNPYVF/hNoKmbFZDKmSQV0r/oRKs8k2BgUpYAEnB/j5ZJeavEqYuCJWKlf/pgT5D3JhNLekEih2B4Ycs3yHQIDAQAB

// praanesh's
// MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2GshLpEjyhW5dO3T2qMlTnikF431HwO/lhtTGEDglvCp3OPBbJzCD9RPld7xU9c1dNoRsSPp/VOOS7S/H1VJezuFbj0Uhwv+G5Crq0x/eZ1FIufRTVPlrq0zujWrKBqGuwH3QSKlA8RgqwhkOiZpz5d1+JLBWIP0SNIK1ZdlNmbhf4ay8YwzR9xN0WrKRbpDfvyPD7yqZGFlQSrTie0GhRMHLm9jDP0DFni6TLuArzI44EpwQj8BgKrwwMC8cEOTNdwkSxU0E1e7zehCKYM2O7cVRMXnxUKIAcnHe3kAL9KUjS//2oQglUg2WZTiwx7CyxIrPDamLdt4eBmuNvHQCwIDAQAB

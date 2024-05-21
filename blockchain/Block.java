package blockchain;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.TimeZone;

public class Block implements Serializable {
    public HashMap<String, PublicKey> userKeyPairs = new HashMap<>();
    private Message message;
    public String previousHash;
    public String hash;

    public Block(String previousHash) {
        this.previousHash = previousHash;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ClassNotFoundException {
        this.message = message;
        mineBlock();
    }

    public void addUserKeyPair(String name, PublicKey publicKey) throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ClassNotFoundException {
        userKeyPairs.put(name, publicKey);
    }

    public void addUserKeyPair(String name, String keyFile) {
        try {
            userKeyPairs.put(name, (PublicKey) Crypto.loadKeyFromFile(keyFile));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public PublicKey getUserKey(String name) {
        return userKeyPairs.get(name);
    }

    public void mineBlock() throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ClassNotFoundException {
        TimeZone.setDefault(TimeZone.getTimeZone("IST"));
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getTimeStamp()), ZoneId.systemDefault());
        if (previousHash == "0") {
            this.hash = Crypto.applySha256(message.getContent() + dateTime.getHour() + dateTime.getMinute()/10 + "saltySalt");
        } else {
            this.hash = Crypto.applySha256(previousHash + message.getContent() + dateTime.getHour() + dateTime.getMinute()/10 + "saltySalt");
        }
    }


    public String toString() {
        try {
            return "Block: " + hash + "\nPrevious: " + previousHash + "\nMessage: " + message.getContent();
        } catch (IOException | ClassNotFoundException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
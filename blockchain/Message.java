package blockchain;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;
import java.util.ArrayList;

public class Message implements Serializable {

    private String content;
    private long timeStamp;
    private PublicKey publicKey;
    private PublicKey senderKey;
    byte[] signature;

    public Message(String content, PublicKey publicKey, PublicKey senderKey) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, ClassNotFoundException, SignatureException {
        this.publicKey = publicKey;
        this.timeStamp = System.currentTimeMillis();
        if (!content.equals("blockZERO") && !content.startsWith("uSeRaDdEd"))
            new MessageStore().addMessage(publicKey, content, timeStamp);

        this.content = content.equals("blockZERO") || content.startsWith("uSeRaDdEd") ? content : DigitalSignature.encrypt(content, publicKey);
        this.signature = DigitalSignature.sign(content, (PrivateKey) Crypto.loadKeyFromFile("private_key.ser"));
        this.senderKey = senderKey;
    }

    public String getContent() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            if (this.content.equals("blockZERO") || this.content.startsWith("uSeRaDdEd")) {
                return this.content;
            } else{
                return DigitalSignature.decrypt(content, (PrivateKey) Crypto.loadKeyFromFile("private_key.ser"));
            }
        } catch (javax.crypto.BadPaddingException e) {
            ArrayList<MessageRecord> messages = new MessageStore().getMessages(publicKey);
            for (MessageRecord message : messages) {
                if (message.getTimeStamp() == timeStamp) {
                    return message.getMessage();
                }
            }
        }
        return null;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PublicKey getSenderKey() {
        return senderKey;
    }

    public byte[] getSignature() {
        return signature;
    }
}
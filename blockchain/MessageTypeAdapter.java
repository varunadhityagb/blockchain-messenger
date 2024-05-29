package blockchain;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;

public class MessageTypeAdapter extends TypeAdapter<Message> {

    private final TypeAdapter<PublicKey> publicKeyAdapter = new PublicKeyTypeAdapter();

    @Override
    public void write(JsonWriter out, Message message) throws IOException {
        out.beginObject();
        try {
            out.name("content").value(Base64.getEncoder().encodeToString(message.getContent().getBytes()));
        } catch (ClassNotFoundException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        out.name("timeStamp").value(message.getTimeStamp());
        out.name("publicKey");
        publicKeyAdapter.write(out, message.getPublicKey());
        out.name("senderKey");
        publicKeyAdapter.write(out, message.getSenderKey());
        out.name("signature").value(Base64.getEncoder().encodeToString(message.getSignature()));
        out.endObject();
    }

    @Override
    public Message read(JsonReader in) throws IOException {
        String content = null;
        long timeStamp = 0;
        PublicKey publicKey = null;
        PublicKey senderKey = null;
        byte[] signature = null;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "content":
                    String encodedContent = in.nextString();
                    byte[] decodedContent = Base64.getDecoder().decode(encodedContent);
                    content = new String(decodedContent, StandardCharsets.UTF_8);
                    break;
                case "timeStamp":
                    timeStamp = in.nextLong();
                    break;
                case "publicKey":
                    publicKey = publicKeyAdapter.read(in);
                    break;
                case "senderKey":
                    senderKey = publicKeyAdapter.read(in);
                    break;
                case "signature":
                    String encodedSignature = in.nextString();
                    signature = Base64.getDecoder().decode(encodedSignature);
                    break;
            }
        }
        in.endObject();

        return new Message(content, publicKey, senderKey, timeStamp, signature);
    }

}

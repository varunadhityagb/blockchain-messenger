import blockchain.Crypto;
import blockchain.Message;
import blockchain.MessageTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.PublicKey;

public class Test {
    public static void main(String[] args) throws Exception {
        PublicKey p1 = (PublicKey) Crypto.loadKeyFromFile("public_key.ser");
        Message m1 = new Message("Hello, World!", p1, p1);
        Message m2;
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(PublicKey.class, new MessageTypeAdapter())
            .registerTypeAdapter(Message.class, new MessageTypeAdapter())
            .create();
        String json = gson.toJson(m1, Message.class);
        m2 = gson.fromJson(json, Message.class);
        System.out.println(m1.getContent());
        System.out.println(m2.getContent());
    }
}

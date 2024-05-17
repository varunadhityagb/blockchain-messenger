package blockchain;

import java.io.*;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class MessageStore {
    private static final String FILE_NAME = "messages.backup";
    private Map<String, ArrayList<MessageRecord>> messageMap;

    @SuppressWarnings("unchecked")
    public MessageStore() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            messageMap = (Map<String, ArrayList<MessageRecord>>) ois.readObject();
        } catch (FileNotFoundException e) {
            messageMap = new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(PublicKey publicKey, String message, long timeStamp) {
        String key = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        ArrayList<MessageRecord> messages = messageMap.getOrDefault(key, new ArrayList<>());
        messages.add(new MessageRecord(message, timeStamp));
        messageMap.put(key, messages);
        saveMessages();
    }

    private void saveMessages() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(messageMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<MessageRecord> getMessages(PublicKey publicKey) {
        String key = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return messageMap.getOrDefault(key, new ArrayList<>());
    }
}

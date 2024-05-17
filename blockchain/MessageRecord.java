package blockchain;

import java.io.Serializable;

public class MessageRecord implements Serializable {
    private String message;
    private long timeStamp;

    public MessageRecord(String message, long timeStamp) {
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "MessageRecord{" +
                "message='" + message + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}

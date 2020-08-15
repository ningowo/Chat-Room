import java.io.Serializable;

/**
 *
 * [Add your documentation here]
 *
 * @author your name and section
 * @version date
 */
final class ChatMessage implements Serializable {
    // why have this UID?
    private static final long serialVersionUID = 6898543889087L;
    private String message;
    private int type;
    private String recipient;

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public ChatMessage(String message, int type, String recipient) {
        this.message = message;
        this.type = type;
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }
}

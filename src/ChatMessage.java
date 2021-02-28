import java.io.Serializable;

/**
 *
 * A message class to encap information needed, might be removed and use json instead.
 *
 * @author ding.ning
 * @date 2021.2.26
 */
final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;
    private String from;
    private String to;
    private int type; // -1 - logout, 0 - start, 1 - private, 2 - public
    private String content;


    public ChatMessage() {}

    public ChatMessage(String from, int type) {
        this.from = from;
        this.type = type;
    }

    public ChatMessage(String from, int type, String content) {
        this.from = from;
        this.type = type;
        this.content = content;
    }

    public ChatMessage(String from, int type, String content, String to) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.content = content;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", type=" + type +
                ", content='" + content + '\'' +
                '}';
    }
}

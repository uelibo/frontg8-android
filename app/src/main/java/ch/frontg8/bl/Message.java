package ch.frontg8.bl;

public class Message {
    private String messagetext;
    private boolean selected = false;

    public Message(String messagetext) {
        this.messagetext = messagetext;
    }

    public String getMessage() {
        return messagetext;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

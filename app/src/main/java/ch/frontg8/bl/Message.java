package ch.frontg8.bl;

import java.io.Serializable;

public class Message implements Serializable {
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

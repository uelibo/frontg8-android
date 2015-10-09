package ch.frontg8.bl;

import java.util.ArrayList;

public class Contact {
    private String name;
    private ArrayList<Message> messages = new ArrayList<Message>();
    private boolean selected = false;

    public Contact(String name)
    {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getCode() {
        return "0";
    }

    public void addMessage(Message msg) {
        messages.add(msg);
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

}
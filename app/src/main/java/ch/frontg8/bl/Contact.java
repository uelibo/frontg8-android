package ch.frontg8.bl;

import java.util.ArrayList;

public class Contact {
    private String name;
    private ArrayList<Message> messages = new ArrayList<Message>();

    public Contact(String name)
    {
        this.name = name;
    }

    public void addMessage(Message msg) {
        messages.add(msg);
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

}

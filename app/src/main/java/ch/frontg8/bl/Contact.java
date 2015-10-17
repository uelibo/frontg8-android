package ch.frontg8.bl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Contact implements Serializable {
    private UUID contactId;
    private String name;
    private String surname;
    private ArrayList<Message> messages = new ArrayList<Message>();

    public Contact(String name) { this(genUUID(), name, ""); }
    public Contact(String name, String surname) { this(genUUID(), name, surname); }
    public Contact(UUID contactId, String name, String surname)
    {
        this.contactId = contactId;
        this.name = name;
        this.surname = surname;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public UUID getContactId() { return contactId; }

    public void addMessage(Message msg) {
        messages.add(msg);
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    private static UUID genUUID() { return java.util.UUID.randomUUID(); }

}
package ch.frontg8.bl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Contact implements Serializable, Filtertext {
    private UUID contactId;
    private String name;
    private String surname;
    private String publicKeyString;
    private int unreadMessageCounter = 0;
    private boolean hasValidPubKey = false;
    private ArrayList<Message> messages = new ArrayList<>();

    public Contact() { this(genUUID(), "", "", "", 0, false); }
    public Contact(UUID contactId, String name, String surname, String publicKeyString, int unreadMessageCounter, boolean hasValidPubKey)
    {
        this.contactId = contactId;
        this.name = name;
        this.surname = surname;
        this.publicKeyString = publicKeyString;
        this.unreadMessageCounter = unreadMessageCounter;
        this.hasValidPubKey = hasValidPubKey;
    }

    public UUID getContactId() { return contactId; }
    private static UUID genUUID() { return java.util.UUID.randomUUID(); }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getPublicKeyString() { return publicKeyString; }
    public void setPublicKeyString(String publicKeyString) { this.publicKeyString = publicKeyString; }

    public void addMessage(Message msg) { messages.add(msg); }
    public void addMessages(ArrayList<Message> msgs) { messages.addAll(msgs); }
    public ArrayList<Message> getMessages() { return messages; }
    public void delAllMessages() { messages.clear(); }

    public void incrementUnreadMessageCounter() { unreadMessageCounter++; }
    public int getUnreadMessageCounter() { return unreadMessageCounter; }
    public void resetUnreadMessageCounter() { unreadMessageCounter = 0; }

    public boolean hasValidPubKey() { return hasValidPubKey; }
    public void setValidPubkey(boolean valid) { this.hasValidPubKey = valid; }

    public String getFilterValue() { return this.name + " " + this.surname; }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Contact other = (Contact) obj;
        return !((this.contactId == null) ? (other.contactId != null) : !this.contactId.equals(other.contactId));
    }

    @Override
    public String toString() { return name; }

}
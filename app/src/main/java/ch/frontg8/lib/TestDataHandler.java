package ch.frontg8.lib;

import java.util.ArrayList;

import ch.frontg8.bl.Contact;
import ch.frontg8.bl.Message;
import java.util.UUID;

public class TestDataHandler {
    final private static ArrayList<Contact> contactList = new ArrayList<>();

    private static void createContacts() {
        contactList.add(new Contact("Ueli"));
        contactList.add(new Contact("Tobi"));
        contactList.add(new Contact("Flix"));
        contactList.add(new Contact("Paul"));
        contactList.add(new Contact("Benny"));

        contactList.get(0).addMessage(new Message("Foo"));
        contactList.get(0).addMessage(new Message("Bar"));
        contactList.get(0).addMessage(new Message("Baz"));
        contactList.get(1).addMessage(new Message("Ham"));
        contactList.get(1).addMessage(new Message("Spam"));
        contactList.get(1).addMessage(new Message("Chicken"));
        contactList.get(2).addMessage(new Message("Food"));
        contactList.get(2).addMessage(new Message("Drinks"));
        contactList.get(2).addMessage(new Message("Napkin"));
        contactList.get(3).addMessage(new Message("abc"));
        contactList.get(3).addMessage(new Message("def"));
        contactList.get(3).addMessage(new Message("ghi"));
        contactList.get(4).addMessage(new Message("Fooo"));
        contactList.get(4).addMessage(new Message("Foooo"));
        contactList.get(4).addMessage(new Message("Fooooo"));
    }

    public static ArrayList<Contact> getContacts() {
        if (contactList.isEmpty()) {
            createContacts();
        }
        return contactList;
    }

    public static Contact getContactById(UUID id) {
        for (Contact contact:contactList) {
            if (contact.getContactId().equals(id)) {
                return contact;
            }
        }
        return new Contact("dummy");
    }

    public static ArrayList<Message> getMessages() {
        final ArrayList<Message> messageList = new ArrayList<>();
        messageList.add(new Message("? something is wrong here ?"));
        return messageList;
    }

}

package ch.frontg8.lib;

import java.util.ArrayList;

import ch.frontg8.bl.Contact;
import ch.frontg8.bl.Message;

public class TestDataHandler {

    public static ArrayList<Contact> getContacts() {
        final ArrayList<Contact> contactList = new ArrayList<>();
        contactList.add(new Contact("Ueli"));
        contactList.add(new Contact("Tobi"));
        contactList.add(new Contact("Flix"));
        contactList.add(new Contact("Paul"));
        contactList.add(new Contact("Benny"));
        return contactList;
    }

    public static ArrayList<Message> getMessages() {
        final ArrayList<Message> messageList = new ArrayList<>();
        messageList.add(new Message("Foo"));
        messageList.add(new Message("Bar"));
        messageList.add(new Message("Baz"));
        return messageList;
    }

}

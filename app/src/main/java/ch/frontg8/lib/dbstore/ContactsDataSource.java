package ch.frontg8.lib.dbstore;

import java.util.ArrayList;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import ch.frontg8.bl.Contact;
import ch.frontg8.bl.Message;

public class ContactsDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_UUID,
            MySQLiteHelper.COLUMN_NAME,
            MySQLiteHelper.COLUMN_SURNAME
    };

    public ContactsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Contact createContact(Contact contact){
        return this.createContact(contact.getContactId(), contact.getName(), contact.getSurname());
    }

    private Contact createContact(UUID contactId, String name, String surname) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_UUID, contactId.toString());
        values.put(MySQLiteHelper.COLUMN_NAME, name);
        values.put(MySQLiteHelper.COLUMN_SURNAME, surname);
        long insertId = database.insert(MySQLiteHelper.TABLE_CONACTS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONACTS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Contact newContact = cursorToContact(cursor);
        cursor.close();
        return newContact;
    }

    public void updateContact(Contact contact) {
        String[] queryArgs = { contact.getContactId().toString() };
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_UUID, contact.getContactId().toString());
        values.put(MySQLiteHelper.COLUMN_NAME, contact.getName());
        values.put(MySQLiteHelper.COLUMN_SURNAME, contact.getSurname());
        database.update(MySQLiteHelper.TABLE_CONACTS, values, MySQLiteHelper.COLUMN_UUID + "=?", queryArgs);
    }

    public void deleteAllContacts() {
        database.execSQL("delete from " + MySQLiteHelper.TABLE_CONACTS);
    }

    public void deleteContact(Contact contact) {
        String uuid = contact.getContactId().toString();
        System.out.println("Contact deleted with id: " + uuid);
        database.delete(MySQLiteHelper.TABLE_CONACTS, MySQLiteHelper.COLUMN_UUID
                + " = '" + uuid + "'", null);
    }

    public Contact getContactByUUID(UUID contactId) {
        String[] queryArgs = { contactId.toString() };
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONACTS,
                allColumns, MySQLiteHelper.COLUMN_UUID + "=?", queryArgs, null, null, null);
        cursor.moveToFirst();
        return cursorToContact(cursor);
    }

    public ArrayList<Contact> getAllContacts() {
        ArrayList<Contact> contacts = new ArrayList<Contact>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONACTS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Contact contact = cursorToContact(cursor);
            //contact.addMessages(getMessagesByUUID(contact.getContactId()));
            contacts.add(contact);
            cursor.moveToNext();
        }
        cursor.close();
        return contacts;
    }

    private Contact cursorToContact(Cursor cursor) {
        Contact contact = new Contact(UUID.fromString(cursor.getString(1)), cursor.getString(2), cursor.getString(3));
        return contact;
    }

    /* Table Messages */

    public Contact insertMessage(Contact contact, Message message){
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_CONTACTUUID, contact.getContactId().toString());
        values.put(MySQLiteHelper.COLUMN_MESSAGETEXT, message.getMessage());
        long insertId = database.insert(MySQLiteHelper.TABLE_MESSAGES, null, values);
        contact.addMessage(message);
        return contact;
    }

    public ArrayList<Message> getMessagesByUUID(UUID contactId) {
        String[] queryArgs = { contactId.toString() };
        ArrayList<Message> messages = new ArrayList<Message>();
        String[] fields = { MySQLiteHelper.COLUMN_MESSAGETEXT };
        Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGES,
                fields, MySQLiteHelper.COLUMN_CONTACTUUID + "=?", queryArgs, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message message = cursorToMessage(cursor);
            messages.add(message);
            cursor.moveToNext();
        }
        cursor.close();
        return messages;
    }

    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message(cursor.getString(0));
        return message;
    }

}

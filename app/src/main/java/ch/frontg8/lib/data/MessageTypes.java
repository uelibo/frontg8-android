package ch.frontg8.lib.data;

public class MessageTypes {

    // Incoming from GUI

    //- Will add you to a List of active Clients for contact-updates
    public static final int MSG_GET_CONTACTS = 110;
    //- Unregister for Contacts
    public static final int MSG_UNREGISTER_CONTACTS = 111;
    //obj UUID - will respond with a contact
    public static final int MSG_GET_CONTACT_DETAILS = 112;
    //obj Contact.
    public static final int MSG_UPDATE_CONTACT = 113;
    //obj UUID
    public static final int MSG_REMOVE_CONTACT = 114;
    //obj UUID - will respond with boolean
    public static final int MSG_CONTAINS_SK = 115;
    //obj UUID - will respond with all Messages for this UUID,
    // it will set the unread-Counter in the Contact to 0
    // and add you to the list of active clients for message-updates
    public static final int MSG_REGISTER_FOR_MESSAGES = 116;
    // - Will remove you from the list of active clients for message-updates
    public static final int MSG_UNREGISTER_FOR_MESSAGES = 117;
    //obj Tuple<UUID, new Frontg8Client.Data>
    public static final int MSG_SEND_MSG = 118;
    // - Will return array of Frontg8Client.Data
    public static final int MSG_REQUEST_MSG = 119;
    // - Will respond with a Base64 String
    public static final int MSG_GET_KEY = 120;
    //obj String (new Password)
    public static final int MSG_CHANGE_PW = 121;
    // - Will change my key, generate new keys for all Contacts, and delete all Messages
    public static final int MSG_GEN_NEW_KEYS = 122;
    //obj UUID - will delete all messages for this contact
    public static final int MSG_DEL_ALL_MSG = 123;
    // - Will reset keystore, database and contacts.
    public static final int MSG_RESET = 124;
    // obj UUID - Will reset unreadCounter for UUID
    public static final int MSG_RESET_UNREAD = 125;
    //obj String - will export user-key to specified destination in obj
    public static final int MSG_EXPORT_KEY = 126;
    //obj String - will import user-key from specified destination in obj
    public static final int MSG_IMPORT_KEY = 127;
    //obj String - will import ca-cert from specified destination in obj
    public static final int MSG_IMPORT_CACERT = 128;
    // - will connect to server, if not connected
    public static final int MSG_CONNECT = 129;
    //
    public static final int MSG_RECONNECT = 130;


    // Outgoing to GUI

    public static final int MSG_UPDATE = 230;

    public static final int MSG_BULK_UPDATE = 231;

    public static final int MSG_ERROR = 232;

}

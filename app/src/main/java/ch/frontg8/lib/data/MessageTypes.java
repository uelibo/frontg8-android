package ch.frontg8.lib.data;

/**
 * Created by tstauber on 12/4/15.
 */
public class MessageTypes {

            // Incoming

            //- Will add you to a List of active Clients for contact-updates
            public static final int MSG_GET_CONTACTS = 10;
            //- Unregister for Contacts
            public static final int MSG_UNREGISTER_CONTACTS = 11;
            //obj UUID - will respond with a contact
            public static final int MSG_GET_CONTACT_DETAILS = 12;
            //obj Contact.
            public static final int MSG_UPDATE_CONTACT = 13;
            //obj UUID
            public static final int MSG_REMOVE_CONTACT = 14;
            //obj UUID - will respond with boolean
            public static final int MSG_CONTAINS_SK = 15;
            //obj UUID - will respond with all Messages for this UUID,
            // it will set the unread-Counter in the Contact to 0
            // and add you to the list of active clients for message-updates
            public static final int MSG_REGISTER_FOR_MESSAGES = 16;
            // - Will remove you from the list of active clients for message-updates
            public static final int MSG_UNREGISTER_FOR_MESSAGES = 17;
            //obj Tuple<UUID, new Frontg8Client.Data>
            public static final int MSG_SEND_MSG = 18;
            // - Will return array of Frontg8Client.Data
            public static final int MSG_REQUEST_MSG = 19;
            // - Will respond with a Base64 String
            public static final int MSG_GET_KEY = 20;
            //obj String (new Password)
            public static final int MSG_CHANGE_PW = 21;
            // - Will change my key, generate new keys for all Contacts, and delete all Messages
            public static final int MSG_GEN_NEW_KEYS = 22;
            //obj UUID - will delete all messages for this contact
            public static final int MSG_DEL_ALL_MSG = 23;
            // - Will reset keystore, database and contacts.
            public static final int MSG_RESET = 24;

            // Outgoing

            public static final int MSG_UPDATE = 30;

            public static final int MSG_BULK_UPDATE = 31;

            public static final int MSG_ERROR = 32;

}

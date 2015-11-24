package ch.frontg8.lib.dbstore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "frontg8.db";
    private static final int DATABASE_VERSION = 7;
    public static final String COLUMN_ID = "id";

    public static final String TABLE_CONTACTS = "contacts";
    public static final String COLUMN_UUID = "contactId";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SURNAME = "surname";
    public static final String COLUMN_PUBLICKEY = "publickey";
    public static final String COLUMN_UNREADMSG = "unreadMessageCounter";

    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_CONTACTUUID = "contactuuid";
    public static final String COLUMN_MESSAGETEXT = "messagetext";
    public static final String COLUMN_MESSAGEBLOB = "messageblob";

    private static final String DATABASE_CREATE_CONTACTS = "create table "
            + TABLE_CONTACTS + "(" + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_UUID + " text not null, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_SURNAME + " text null, "
            + COLUMN_PUBLICKEY + " text null, "
            + COLUMN_UNREADMSG + " int null); ";

    private static final String DATABASE_CREATE_MESSAGES = "create table "
            + TABLE_MESSAGES + "(" + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_CONTACTUUID + " text not null, "
            + COLUMN_MESSAGETEXT + " text null, "
            + COLUMN_MESSAGEBLOB + " blob null);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_CONTACTS);
        database.execSQL(DATABASE_CREATE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

}
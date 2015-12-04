package ch.frontg8.lib.config;

import android.content.Context;
import android.content.SharedPreferences;

import ch.frontg8.R;

public class LibConfig {
    private static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE);
    }

    public static String getKeystoreFilePath(Context context){
        return getSharedPrefs(context).getString("edittext_preference_keyfilepath", "frontg8keystore.ks");
    }

    public static String getServerName(Context context){
        return getSharedPrefs(context).getString("edittext_preference_hostname", "server.frontg8.ch");
    }

    public static int getServerPort(Context context){
        return Integer.parseInt(getSharedPrefs(context).getString("edittext_preference_port", "40001"));
    }

    public static String getUsername(Context context) {
        return getSharedPrefs(context).getString("edittext_preference_username", "paul");
    }

    public static byte[] getLastMessageHash(Context context) {
        return (getSharedPrefs(context).getString("last_message_hash", "0")).getBytes();
    }

    public static void setLastMessageHash(Context context, byte[] hash) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString("last_message_hash", hash.toString());
        editor.commit();
    }

}

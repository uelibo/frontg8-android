package ch.frontg8.lib.config;

import android.content.Context;
import android.content.SharedPreferences;

import ch.frontg8.R;

public class LibConfig {
    private static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(context.getString(R.string.preferences), context.MODE_PRIVATE);
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

}

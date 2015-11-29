package ch.frontg8.lib.connection;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import ch.frontg8.R;

public class Logger {
    private Activity context=null;
    private String activityName ="noActivity";

    public Logger() {
    }

    public Logger(Activity context, String activityName) {
        this.context = context;
        this.activityName = activityName;
    }

    @NonNull
    public void TRACE(final String log) {
        if ( log == null ) {
            return;
        }
        if (context == null) {
            Log.w(activityName, log);
        } else {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.w(activityName, log);
                    TextView textViewLog = (TextView) context.findViewById(R.id.textViewLog);
                    textViewLog.append(log + "\r\n");
                }
            });
        }
    }

    public void e(String s) {
        Log.e(activityName,s);
    }
}

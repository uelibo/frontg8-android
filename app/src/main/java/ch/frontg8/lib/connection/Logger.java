package ch.frontg8.lib.connection;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import ch.frontg8.R;

public class Logger {
    private Activity context=null;
    private String acitivtyName="noActivity";

    public Logger() {
    }

    public Logger(Activity context, String activityName) {
        this.context = context;
        this.acitivtyName = activityName;
    }

    public void TRACE(final String log) {
        if (context == null) {
            Log.w(acitivtyName, log);
        } else {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.w(acitivtyName, log);
                    TextView textViewLog = (TextView) context.findViewById(R.id.textViewLog);
                    textViewLog.append(log + "\r\n");
                }
            });
        }
    }

}

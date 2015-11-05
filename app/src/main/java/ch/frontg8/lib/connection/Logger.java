package ch.frontg8.lib.connection;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import ch.frontg8.R;

public class Logger {
    private Activity context;
    private String acitivtyName;

    public Logger(Activity context, String activityName) {
        this.context = context;
        this.acitivtyName = activityName;
    }

    public void TRACE(final String log) {
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

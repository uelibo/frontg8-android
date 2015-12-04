package ch.frontg8;

import android.app.Application;
import android.content.Intent;

import ch.frontg8.lib.data.DataService;

public class MyApplication extends Application {
    Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(getApplicationContext(), DataService.class);
        startService(intent);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(intent);
    }
}


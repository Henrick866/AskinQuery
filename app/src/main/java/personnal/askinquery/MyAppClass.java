package personnal.askinquery;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

public class MyAppClass extends Application {
    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel mChannel = new NotificationChannel("AskinQueryChannel_01", getString(R.string.common_google_play_services_notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);
        }
        MyAppClass.context = getApplicationContext();
        // Required initialization logic here!
    }
    public static Context getAppContext() {
        return MyAppClass.context;
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
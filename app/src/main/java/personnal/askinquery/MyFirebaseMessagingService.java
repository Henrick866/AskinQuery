package personnal.askinquery;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService{
        @Override
        public void onNewToken(String s) {
            super.onNewToken(s);
            Log.e("NEW_TOKEN",s);
        }

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            String notificationTitle = null, notificationBody = null;

            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                notificationTitle = remoteMessage.getNotification().getTitle();
                notificationBody = remoteMessage.getNotification().getBody();
            }

            // Also if you intend on generating your own notifications as a result of a received FCM
            // message, here is where that should be initiated. See sendNotification method below.
            sendNotification(notificationTitle, notificationBody);
        }

        private void sendNotification(String notificationTitle, String notificationBody) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);


            String FinalContent = null, FinalTitle = null;
            String[] string = notificationBody.split("[|]");
            if(notificationTitle.equals("NEW_PUBLICATION")){
                intent.putExtra("FragmentName", "PubList");
                intent.putExtra("AuthorID", string[0]);
                FinalTitle = string[2] + " a publié une nouvelle!";
                FinalContent = "Appuyez pour la voir.";
            }
            if(notificationTitle.equals("NEW_SONDAGE")){
                intent.putExtra("FragmentName", "NewPoll");
                intent.putExtra("SondageID", string[1]);
                FinalTitle = string[2] + " a publié un sondage!";
                FinalContent = "Appuyez pour y répondre.";
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)   //Automatically delete the notification
                    .setSmallIcon(R.mipmap.ic_launcher) //Notification icon
                    .setContentIntent(pendingIntent)
                    .setContentTitle(FinalTitle)
                    .setContentText(FinalContent)
                    .setSound(defaultSoundUri);


            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, notificationBuilder.build());
        }
}

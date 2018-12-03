package personnal.askinquery;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
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

            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);


            String FinalContent = null, FinalTitle = null;
            String[] string = notificationBody.split("[|]");
            if(notificationTitle != null) {
                if (notificationTitle.equals("NEW_PUBLICATION")) {
                    intent.putExtra("FragmentName", "PubList");
                    intent.putExtra("AuthorID", string[0]);
                    FinalTitle = string[2] + " a publié une nouvelle!";
                    FinalContent = "Appuyez pour la voir.";
                }
                if (notificationTitle.equals("NEW_SONDAGE")) {
                    intent.putExtra("FragmentName", "NewPoll");
                    intent.putExtra("SondageID", string[1]);
                    FinalTitle = string[2] + " a publié un sondage!";
                    FinalContent = "Appuyez pour y répondre.";
                }
            }
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 1, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            String ChannelID = "AskinQueryChannel_01";
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ChannelID)
                    .setAutoCancel(true)   //Automatically delete the notification
                    .setSmallIcon(R.drawable.ic_notification) //Notification icon
                    .setContentIntent(resultPendingIntent)
                    .setContentTitle(FinalTitle)
                    .setContentText(FinalContent)
                    .setSound(defaultSoundUri);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1, notificationBuilder.build());
        }
}

package personnal.askinquery;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class VideoPopupActivity extends AppCompatActivity {

    private MediaController mediaController;
    private TextView ProgressText;
    private VideoView videoView;
    private ProgressBar Loader, Progress;
    private Context context;
    private FileDownloadTask fileDownloadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_popup);
        context = this;
        this.setFinishOnTouchOutside(false);
        String mParam1;
        boolean mParam2;
        mParam1 = getIntent().getStringExtra("Path");
        mParam2 = getIntent().getBooleanExtra("NotOnServer", false);
        videoView = findViewById(R.id.VideoPlayer);
        mediaController = new MediaController(this);
        Loader = findViewById(R.id.video_dialog_loader);
        ProgressText = findViewById(R.id.video_dialog_textprogress);
        Progress = findViewById(R.id.video_dialog_progress);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM;
        mediaController.setLayoutParams(lp);

        ((ViewGroup) mediaController.getParent()).removeView(mediaController);

        ((FrameLayout) videoView.getParent()).addView(mediaController);
        mediaController.setVisibility(View.GONE);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
                //if we have a position on savedInstanceState, the video playback should start from her

                videoView.seekTo(0);
                Loader.setVisibility(View.GONE);
                Progress.setVisibility(View.GONE);
                ProgressText.setVisibility(View.GONE);
            }
        });
        final Button btnClose = findViewById(R.id.VideoCloseBtn);



        if(mParam2){
            videoView.setVideoURI(Uri.parse(mParam1));
            videoView.setVisibility(View.VISIBLE);
            mediaController.setVisibility(View.VISIBLE);
            videoView.requestFocus();
        }else {
            final StorageReference VideoPath = FirebaseStorage.getInstance().getReference().child(mParam1);
            try {
                final File videoFile = File.createTempFile("Video", mParam1.substring(mParam1.lastIndexOf(".") + 1));

                fileDownloadTask = VideoPath.getFile(videoFile);
                fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        videoView.setVideoURI(Uri.fromFile(videoFile));
                        Loader.setVisibility(View.GONE);
                        Progress.setVisibility(View.GONE);
                        ProgressText.setVisibility(View.GONE);
                        videoView.setVisibility(View.VISIBLE);
                        mediaController.setVisibility(View.VISIBLE);
                        videoView.requestFocus();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Loader.setVisibility(View.GONE);
                        Progress.setVisibility(View.GONE);
                        ProgressText.setTextColor(getResources().getColor(R.color.colorRed));
                        ProgressText.setText(R.string.Video_Dialog_Err);
                        Toast.makeText(getBaseContext(), "Échec du téléchargement", Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        int iProgress = (int)Math.round(progress);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            Progress.setProgress(iProgress, true);
                        }else{
                            Progress.setProgress(iProgress);
                        }

                        ProgressText.setText(context.getString(R.string.Poll_Answ_Percent, iProgress));
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(context, "Téléchargement annullé.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fileDownloadTask != null) {
                    fileDownloadTask.cancel();
                }
                ((Activity)context).finish();
            }
        });
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(fileDownloadTask != null){
            fileDownloadTask.cancel();
        }
    }
}

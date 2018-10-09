package personnal.askinquery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Henrick on 2018-09-21.
 */
/*
* Scénarios :
* 1. créée une question
*
* */

public class CreerOptionAdapter extends ArrayAdapter<Option> {
    private static AdaptListener adaptListener;
    public ArrayList<Option> OptionData;
    Context c;
    int currentEdited;
    int Type;
    int Position;
    int PositionQuestion;
    public interface OptionsToQuestion{

    }
    public CreerOptionAdapter(Context context, ArrayList<Option> options, int type, AdaptListener adaptListener){
        super(context, 0, options);
        c = context;
        Type = type;
        OptionData = options;
        this.adaptListener = adaptListener;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        final Option option = getItem(position);
        Position = position;
        if(OptionData.get(position) == null){
            OptionData.add(option);
        }
        View view = convertView;
        final CreerOptionAdapter.ViewHolder holder;
        if(view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.option_edit_element, parent, false);
            holder = new CreerOptionAdapter.ViewHolder(view, position);
            view.setTag(holder);
            holder.TexteReponse.setTag(option.ID);
        }else{
            holder = (CreerOptionAdapter.ViewHolder)view.getTag();
        }
        option.firstopened = true;
        if(option.Texte != null) {
            if (!option.Texte.isEmpty()) {
                holder.TexteReponse.setText(option.Texte);
            } else {
                holder.TexteReponse.setText("");
                holder.TexteError.setText("Vous devez proposer une réponse");
                holder.TexteError.setVisibility(View.VISIBLE);
            }
        }else{
            holder.TexteReponse.setText("");
            holder.TexteError.setText("Vous devez proposer une réponse");
            holder.TexteError.setVisibility(View.VISIBLE);
        }
        holder.OptionNum.setText("Option #"+(position+1));

        if(Type != Question.TYPE_TEXTE){
            StorageReference OptionMedRef;
            if(Type == Question.TYPE_IMAGE){

                holder.ImagePreview.setVisibility(View.VISIBLE);
                holder.VideoPreview.setVisibility(View.GONE);
                if(option.Chemin_Image != null){
                if(option.Chemin_Image.equals("N")){
                        if(option.Image != null) {
                            holder.MediaError.setVisibility(View.GONE);
                            holder.ImagePreview.setImageURI(option.Image);
                        }else {
                            holder.MediaError.setText("Vous devez téléverser une image");
                            holder.MediaError.setVisibility(View.VISIBLE);
                        }
                    }else{
                        OptionMedRef = FirebaseStorage.getInstance().getReference().child(option.Chemin_Image);
                        OptionMedRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap Image = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                                holder.ImagePreview.setImageBitmap(Image);
                            }
                        });
                        }
                }else{
                    holder.MediaError.setText("Vous devez téléverser une image");
                    holder.MediaError.setVisibility(View.VISIBLE);
                }
            }else if(Type == Question.TYPE_VIDEO){
                holder.ImagePreview.setVisibility(View.GONE);
                holder.VideoPreview.setVisibility(View.VISIBLE);
                if(option.Chemin_Video != null) {
                    if (option.Chemin_Video.equals("N")) {//si nouv ou sans image au départ
                        if (option.Video != null) {
                            holder.MediaError.setVisibility(View.GONE);
                            holder.VideoPreview.setVideoURI(option.Video);
                        } else {
                            holder.MediaError.setText("Vous devez téléverser une vidéo");
                            holder.MediaError.setVisibility(View.VISIBLE);
                        }

                    } else {
                        try {
                            String ext = option.Chemin_Video.substring(option.Chemin_Video.lastIndexOf(".") + 1);
                            final File TempFile = File.createTempFile("video", ext);
                            OptionMedRef = FirebaseStorage.getInstance().getReference().child(option.Chemin_Video);
                            OptionMedRef.getFile(TempFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    holder.VideoPreview.setVideoURI(Uri.fromFile(TempFile));
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    /*OptionMedRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            holder.VideoPreview.setVideoURI(uri);
                        }
                    });*/

                    }
                }else{
                    holder.MediaError.setText("Vous devez téléverser une vidéo");
                    holder.MediaError.setVisibility(View.VISIBLE);
                }
                }
        }else{
            holder.ZoneUpload.setVisibility(View.GONE);
            holder.MediaError.setVisibility(View.GONE);
            holder.ImagePreview.setVisibility(View.GONE);
            holder.VideoPreview.setVisibility(View.GONE);
        }
        final CreerOptionAdapter adapter = this;
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adaptListener.DeleteOption(Position);
            }
        });
        holder.btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                adaptListener.LoadMedia(Position, Type);
            }
        });
        holder.TexteReponse.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                //adaptListener.ToggleDoneBtn(!b);
                if(b == true){
                    currentEdited = Position;
                    /*String newTexte = ((EditText)view).getText().toString();
                    if(newTexte.isEmpty()){
                        holder.TexteError.setText("Vous devez proposer une réponse");
                        holder.TexteError.setVisibility(View.VISIBLE);
                    }else {
                        if(OptionData.get(position).Texte == null) {//si nouv
                            OptionData.get(position).Texte = newTexte;
                        }else if (!OptionData.get(position).Texte.equals(newTexte)) {
                            OptionData.get(position).Texte = newTexte;
                            adaptListener.updateOptions(OptionData);
                        }
                        holder.TexteError.setVisibility(View.INVISIBLE);
                    }*/
                }
            }
        });
        holder.TexteReponse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //if (!option.firstopened) {
                    String newTexte = s.toString();
                    if (newTexte.isEmpty()) {
                        holder.TexteError.setText("Vous devez proposer une réponse");
                        holder.TexteError.setVisibility(View.VISIBLE);
                    } else {
                        if (currentEdited == Position) {
                            if (OptionData.get(Position).Texte == null) {//si nouv
                                OptionData.get(Position).Texte = newTexte;
                            } else if (!OptionData.get(Position).Texte.equals(newTexte)) {
                                OptionData.get(Position).Texte = newTexte;
                                adaptListener.updateOptions(OptionData);
                            }
                            holder.TexteError.setVisibility(View.INVISIBLE);
                        }
                    }
                /*}else{
                    option.firstopened = false;
                }*/
            }
        });


        return view;
    }

    static class ViewHolder{
        TextView OptionNum;
        TextView TexteError;
        TextView MediaError;
        RelativeLayout ZoneUpload;
        ImageButton btnDelete;
        EditText TexteReponse;
        ImageView ImagePreview;
        VideoView VideoPreview;
        Button btnUpload;
        public ViewHolder(View view, int position){
            OptionNum = (TextView)view.findViewById(R.id.sondage_edit_option_num);
            ZoneUpload = (RelativeLayout)view.findViewById(R.id.sondage_edit_option_upload);
            btnDelete = (ImageButton)view.findViewById(R.id.sondage_edit_option_delete);
            TexteReponse = (EditText)view.findViewById(R.id.sondage_edit_option_texte);
            btnUpload = (Button)view.findViewById(R.id.sondage_edit_option_upload_btn);
            VideoPreview = (VideoView)view.findViewById(R.id.sondage_edit_option_video_preview);
            ImagePreview = (ImageView)view.findViewById(R.id.sondage_edit_option_image_preview);
            TexteError = (TextView)view.findViewById(R.id.option_edit_texte_error);
            MediaError = (TextView)view.findViewById(R.id.option_edit_media_error);

        }
    }
    public interface AdaptListener{
        //void changePage(Fragment fragment);
        void DeleteOption(int optionPosition);
        void updateOptions(ArrayList<Option> lo);
        void LoadMedia(int optionPosition, int type);
        void ToggleDoneBtn(boolean b);
        // void LoadImage(String Chemin);
        //void update(Question q, ArrayList<Option> lo);
    }
}
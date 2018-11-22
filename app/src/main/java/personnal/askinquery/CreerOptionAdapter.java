package personnal.askinquery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.provider.MediaStore;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
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
        currentEdited = -1;
    }
    @Override
    public int getCount(){
        int n = 0;
        for(Option o : OptionData){
           n += o.toBeDeleted ? 0 : 1;
        }
        return n;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        final Option option = getItem(position);
        Position = position;
        final int POSITION = position;
        if(OptionData.get(position) == null){
            OptionData.add(option);
        }
        View view = convertView;
        final CreerOptionAdapter.ViewHolder holder;
        if(view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.option_edit_element, parent, false);
            holder = new CreerOptionAdapter.ViewHolder(view, position);

            view.setTag(holder);
            //holder.TexteReponse.setTag(OptionData.get(POSITION).ID);
        }else{
            holder = (CreerOptionAdapter.ViewHolder)view.getTag();
        }
        if(OptionData.get(POSITION).toBeDeleted){
            view.setVisibility(View.GONE);
        }else {
            holder.OptionNum.setText(c.getString(R.string.Create_Option_Elem_Numerate, (position + 1)));
            if (Type != Question.TYPE_TEXTE) {//exécute ce chargement trop souvent pour rien, comment enregistrer les médias en cache?
                StorageReference OptionMedRef;
                if (OptionData.get(POSITION).notOnServer) {//si n'est pas sur le serveur, créé récement
                    if (OptionData.get(POSITION).ImagePreload == null) {//si l'image bitmap n'existe pas
                        holder.ImagePreview.setVisibility(View.GONE);
                        holder.Loading.setVisibility(View.GONE);
                        holder.MediaError.setVisibility(View.VISIBLE);
                        if(Type == Question.TYPE_VIDEO){
                            holder.MediaError.setText(c.getString(R.string.Create_Option_Elem_No_Media_Err, "vidéo"));
                        }else{
                            holder.MediaError.setText(c.getString(R.string.Create_Option_Elem_No_Media_Err, "image"));
                        }

                    } else {//si bitmap existe
                        holder.Loading.setVisibility(View.GONE);
                        holder.ImagePreview.setImageBitmap(OptionData.get(POSITION).ImagePreload);
                        holder.ImagePreview.setVisibility(View.VISIBLE);
                        holder.MediaError.setVisibility(View.INVISIBLE);
                        if (Type == Question.TYPE_VIDEO) {// + si c'est une vidéo, mets un clicklistener sur l'image qui ouvre un dialog
                            holder.Instruct.setText(R.string.Post_Elem_Instruct_Vid);
                            holder.Instruct.setVisibility(View.VISIBLE);
                            holder.ImagePreviewIcon.setVisibility(View.VISIBLE);
                            holder.ImagePreviewIcon.setImageResource(R.drawable.ic_play_circle_color);
                            holder.ImagePreview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FragmentManager fm = adaptListener.getFragmentManagerQ();
                                    FragmentTransaction ft = fm.beginTransaction();
                                    Fragment prev = fm.findFragmentByTag("fragment_video_dialog");
                                    if (prev != null) {
                                        ft.remove(prev);
                                    }
                                    ft.addToBackStack(null);
                                    VideoDialogFragment creerOptionDialog = VideoDialogFragment.newInstance(OptionData.get(POSITION).UriVideo, OptionData.get(POSITION).notOnServer);
                                    creerOptionDialog.show(ft, "fragment_video_dialog");
                                }
                            });
                        }else{
                            holder.Instruct.setText(R.string.Post_Elem_Instruct_Img);
                            holder.Instruct.setVisibility(View.VISIBLE);
                            holder.ImagePreviewIcon.setVisibility(View.VISIBLE);
                            holder.ImagePreviewIcon.setImageResource(R.drawable.ic_loupe_black_24dp);
                            holder.ImagePreview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Dialog dialog = new Dialog(c);
                                    dialog.setContentView(R.layout.image_dialog);
                                    dialog.show();
                                    final ImageView Image = dialog.findViewById(R.id.image_dialog_imageview);
                                    final ProgressBar progressBar = dialog.findViewById(R.id.image_dialog_progressBar);
                                    Image.setImageBitmap(OptionData.get(POSITION).ImageFull);
                                    progressBar.setVisibility(View.GONE);
                                    Image.setVisibility(View.VISIBLE);

                                }
                            });
                        }
                    }
                } else {
                    if(OptionData.get(POSITION).ImagePreload == null) {
                        holder.Loading.setVisibility(View.VISIBLE);
                        holder.ImagePreview.setVisibility(View.GONE);
                    }else {
                        holder.Loading.setVisibility(View.GONE);
                        holder.ImagePreview.setImageBitmap(OptionData.get(POSITION).ImagePreload);
                        holder.ImagePreview.setVisibility(View.VISIBLE);
                        holder.MediaError.setVisibility(View.INVISIBLE);
                        if (Type == Question.TYPE_VIDEO) {// + si c'est une vidéo, mets un clicklistener sur l'image qui ouvre un dialog
                            holder.Instruct.setText(R.string.Post_Elem_Instruct_Vid);
                            holder.Instruct.setVisibility(View.VISIBLE);
                            holder.ImagePreviewIcon.setVisibility(View.VISIBLE);
                            holder.ImagePreviewIcon.setImageResource(R.drawable.ic_play_circle_color);
                            holder.ImagePreview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FragmentManager fm = adaptListener.getFragmentManagerQ();
                                    FragmentTransaction ft = fm.beginTransaction();
                                    Fragment prev = fm.findFragmentByTag("fragment_video_dialog");
                                    if (prev != null) {
                                        ft.remove(prev);
                                    }
                                    ft.addToBackStack(null);
                                    VideoDialogFragment creerOptionDialog = VideoDialogFragment.newInstance(OptionData.get(POSITION).Chemin_Media, OptionData.get(POSITION).notOnServer);
                                    creerOptionDialog.show(ft, "fragment_video_dialog");
                                }
                            });
                        }else{
                            holder.Instruct.setText(R.string.Post_Elem_Instruct_Img);
                            holder.Instruct.setVisibility(View.VISIBLE);
                            holder.ImagePreviewIcon.setVisibility(View.VISIBLE);
                            holder.ImagePreviewIcon.setImageResource(R.drawable.ic_loupe_black_24dp);
                            holder.ImagePreview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Dialog dialog = new Dialog(c);
                                    dialog.setContentView(R.layout.image_dialog);
                                    dialog.show();
                                    final ImageView Image = dialog.findViewById(R.id.image_dialog_imageview);
                                    final ProgressBar progressBar = dialog.findViewById(R.id.image_dialog_progressBar);
                                    StorageReference FullImgRef = FirebaseStorage.getInstance().getReference().child(OptionData.get(POSITION).Chemin_Media);
                                    FullImgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            Image.setImageBitmap(b);
                                            progressBar.setVisibility(View.GONE);
                                            Image.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            } else {
                holder.ZoneUpload.setVisibility(View.GONE);
                holder.Instruct.setVisibility(View.GONE);
                holder.MediaError.setVisibility(View.GONE);
                holder.ImagePreviewIcon.setVisibility(View.GONE);
                holder.Loading.setVisibility(View.GONE);
                holder.ImagePreview.setVisibility(View.GONE);
            }
            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(c)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Supprimer question")
                            .setMessage("Voulez-vous vraiment supprimer cette option?")
                            .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adaptListener.DeleteOption(POSITION);
                                }

                            })
                            .setNegativeButton("Non", null)
                            .show();

                }
            });
            holder.btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    adaptListener.LoadMedia(POSITION, Type);
                }
            });
            holder.TexteReponse.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    //adaptListener.ToggleDoneBtn(!b);
                    if (b) {
                        currentEdited = POSITION;
                    }
                }
            });
            TextWatcher TxtFieldWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String newTexte = s.toString();
                    if (newTexte.isEmpty()) {
                        holder.TexteError.setText(R.string.Create_Option_Elem_No_Text_Err);
                        holder.TexteError.setVisibility(View.VISIBLE);
                    } else {
                        if (currentEdited == POSITION) {
                            if (OptionData.get(POSITION).Texte == null) {//si nouv
                                OptionData.get(POSITION).Texte = newTexte;
                            } else if (!OptionData.get(POSITION).Texte.equals(newTexte)) {
                                OptionData.get(POSITION).Texte = newTexte;
                                adaptListener.updateOptions(OptionData);
                            }
                            holder.TexteError.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            };
            TextWatcher oldWatcher = (TextWatcher) holder.TexteReponse.getTag();
            if (oldWatcher != null) {
                holder.TexteReponse.removeTextChangedListener(oldWatcher);
            }
            if (OptionData.get(POSITION).Texte != null) {
                if (!OptionData.get(POSITION).Texte.isEmpty()) {
                    holder.TexteReponse.setText(OptionData.get(POSITION).Texte);
                } else {
                    holder.TexteReponse.setText("");
                    holder.TexteError.setText(R.string.Create_Option_Elem_No_Text_Err);
                    holder.TexteError.setVisibility(View.VISIBLE);
                }
            } else {
                holder.TexteReponse.setText("");
                holder.TexteError.setText(R.string.Create_Option_Elem_No_Text_Err);
                holder.TexteError.setVisibility(View.VISIBLE);
            }
            holder.TexteReponse.setTag(TxtFieldWatcher);
            holder.TexteReponse.addTextChangedListener(TxtFieldWatcher);
        }

        return view;
    }

    static class ViewHolder{
        TextView OptionNum, Instruct;
        TextView TexteError;
        TextView MediaError;
        RelativeLayout ZoneUpload;
        ImageButton btnDelete;
        EditText TexteReponse;
        ImageView ImagePreview, ImagePreviewIcon;
        Button btnUpload;
        ProgressBar Loading;
        Bitmap Image;
        public ViewHolder(View view, int position){
            OptionNum = view.findViewById(R.id.sondage_edit_option_num);
            ZoneUpload = view.findViewById(R.id.sondage_edit_option_upload);
            btnDelete = view.findViewById(R.id.sondage_edit_option_delete);
            TexteReponse = view.findViewById(R.id.sondage_edit_option_texte);
            btnUpload = view.findViewById(R.id.sondage_edit_option_upload_btn);
            ImagePreview = view.findViewById(R.id.sondage_edit_option_image_preview);
            TexteError = view.findViewById(R.id.option_edit_texte_error);
            MediaError = view.findViewById(R.id.option_edit_media_error);
            Loading = view.findViewById(R.id.sondage_edit_option_progress);
            Instruct = view.findViewById(R.id.option_edit_instruct);
            ImagePreviewIcon = view.findViewById(R.id.sondage_edit_option_image_preview_icon);


        }
    }
    public interface AdaptListener{
        void DeleteOption(int optionPosition);
        void updateOptions(ArrayList<Option> lo);
        void LoadMedia(int optionPosition, int type);
        FragmentManager getFragmentManagerQ();
        void ToggleDoneBtn(boolean b);
    }
}
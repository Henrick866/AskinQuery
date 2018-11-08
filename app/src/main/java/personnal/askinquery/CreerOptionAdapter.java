package personnal.askinquery;

import android.app.AlertDialog;
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
        this.adaptListener = adaptListener;
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
            holder.OptionNum.setText("Option #" + (position + 1));
            if (Type != Question.TYPE_TEXTE) {//exécute ce chargement trop souvent pour rien, comment enregistrer les médias en cache?
                StorageReference OptionMedRef;
                if (OptionData.get(POSITION).notOnServer) {//si n'est pas sur le serveur, créé récement
                    if (OptionData.get(POSITION).ImagePreload == null) {//si l'image bitmap n'existe pas
                        Log.e("Test", "Dud");
                        holder.ImagePreview.setVisibility(View.GONE);
                        holder.MediaError.setVisibility(View.VISIBLE);
                        holder.MediaError.setText("Aucune image/vidéo sélectionnée, veuillez en choisir une.");
                    } else {//si bitmap existe

                        holder.ImagePreview.setImageBitmap(OptionData.get(POSITION).ImagePreload);
                        holder.ImagePreview.setVisibility(View.VISIBLE);
                        holder.MediaError.setVisibility(View.INVISIBLE);
                        Log.e("Test", "Loaded");
                        if (Type == Question.TYPE_VIDEO) {// + si c'est une vidéo, mets un clicklistener sur l'image qui ouvre un dialog
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
                        }
                    }
                } else {
                    holder.ImagePreview.setImageBitmap(OptionData.get(POSITION).ImagePreload);
                    holder.ImagePreview.setVisibility(View.VISIBLE);
                    holder.MediaError.setVisibility(View.INVISIBLE);
                    if (Type == Question.TYPE_VIDEO) {// + si c'est une vidéo, mets un clicklistener sur l'image qui ouvre un dialog
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
                    }
                }
            } else {
                holder.ZoneUpload.setVisibility(View.GONE);
                holder.MediaError.setVisibility(View.GONE);
                holder.ImagePreview.setVisibility(View.GONE);
            }
            final CreerOptionAdapter adapter = this;
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
                    if (b == true) {
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
                        holder.TexteError.setText("Vous devez proposer une réponse");
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
                    holder.TexteError.setText("Vous devez proposer une réponse");
                    holder.TexteError.setVisibility(View.VISIBLE);
                }
            } else {
                holder.TexteReponse.setText("");
                holder.TexteError.setText("Vous devez proposer une réponse");
                holder.TexteError.setVisibility(View.VISIBLE);
            }
            holder.TexteReponse.setTag(TxtFieldWatcher);
            holder.TexteReponse.addTextChangedListener(TxtFieldWatcher);
        }

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
        Button btnUpload;
        File VideoFile;
        Bitmap Image;
        public ViewHolder(View view, int position){
            OptionNum = (TextView)view.findViewById(R.id.sondage_edit_option_num);
            ZoneUpload = (RelativeLayout)view.findViewById(R.id.sondage_edit_option_upload);
            btnDelete = (ImageButton)view.findViewById(R.id.sondage_edit_option_delete);
            TexteReponse = (EditText)view.findViewById(R.id.sondage_edit_option_texte);
            btnUpload = (Button)view.findViewById(R.id.sondage_edit_option_upload_btn);
            ImagePreview = (ImageView)view.findViewById(R.id.sondage_edit_option_image_preview);
            TexteError = (TextView)view.findViewById(R.id.option_edit_texte_error);
            MediaError = (TextView)view.findViewById(R.id.option_edit_media_error);

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
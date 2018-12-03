package personnal.askinquery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class PublicationAdapter extends ArrayAdapter<Publication> {
    private Context c;

    private HashMap<String, Bitmap> BitmapMap;
    private PublicationAdapter publicationAdapter;

    PublicationAdapter(Context context, ArrayList<Publication> publications){
        super(context, 0, publications);
        c = context;
        publicationAdapter = this;
        BitmapMap = new HashMap<>();
    }

    public View getView(final int position, View convertView, final ViewGroup parent){
        final Publication publication = getItem(position);
        View view = convertView;
        final PublicationAdapter.ViewHolder holder;
        final FirebaseUser user = ((AdaptListener)c).getUser();
        if(view == null){
            view = LayoutInflater.from(c).inflate(R.layout.publication_element, parent, false);
            holder = new PublicationAdapter.ViewHolder(view);

            view.setTag(holder);
        }else{
            holder = (PublicationAdapter.ViewHolder)view.getTag();
        }
        final ValueEventListener PollLinkListeners = new SondageImgLoadListener(holder, user);
        DatabaseReference AuteurRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(publication.AuteurRef);
        AuteurRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Profil auteur = dataSnapshot.getValue(Profil.class);
                holder.UserNameView.setText(auteur.Username);
                if(auteur.Avatar.equals("N")){
                    holder.AvatarView.setImageResource(R.mipmap.ic_launcher);
                }else{
                    StorageReference AvatarRef = FirebaseStorage.getInstance().getReference().child(auteur.Avatar);
                    AvatarRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            holder.AvatarView.setImageBitmap(b);
                            holder.AvatarView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ((AdaptListener)c).changePage(ConsultProfilFragment.newInstance(auteur));
                                }
                            });
                        }
                    });
                }
                holder.UserNameView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((AdaptListener)c).changePage(ConsultProfilFragment.newInstance(auteur));
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.TitreView.setText(publication.Titre);
        holder.TexteView.setText(publication.Texte);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy", c.getResources().getConfiguration().locale);
        holder.DateView.setText(format.format(publication.Date_Public));

        if(publication.Type != Publication.TYPE_TEXTE){
            holder.Loading.setVisibility(View.VISIBLE);
            if(publication.Type == Publication.TYPE_IMAGE){
                if(BitmapMap.get(publication.ID) == null) {
                    StorageReference AvatarRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_IMAGES_THUMBNAILS).child(publication.ID + ".jpg");
                    AvatarRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            holder.Instruct.setText(R.string.Post_Elem_Instruct_Img);
                            holder.Instruct.setVisibility(View.VISIBLE);
                            holder.Icon.setImageResource(R.drawable.ic_loupe_black_24dp);
                            holder.Icon.setVisibility(View.VISIBLE);
                            holder.MediaView.setImageBitmap(b);
                            BitmapMap.put(publication.ID, b);
                            holder.MediaView.setVisibility(View.VISIBLE);
                            holder.Loading.setVisibility(View.GONE);
                            holder.MediaView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final Dialog dialog = new Dialog(c);
                                    dialog.setContentView(R.layout.image_dialog);
                                    dialog.show();
                                    final ImageView Image = dialog.findViewById(R.id.image_dialog_imageview);
                                    final ProgressBar progressBar = dialog.findViewById(R.id.image_dialog_progressBar);
                                    StorageReference FullImgRef = FirebaseStorage.getInstance().getReference().child(publication.Media);
                                    FullImgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            Image.setImageBitmap(b);
                                            progressBar.setVisibility(View.GONE);
                                            Image.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    final Button CloseBtn = dialog.findViewById(R.id.ImageCloseBtn);
                                    CloseBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();
                                        }
                                    });


                                }
                            });

                        }
                    });
                }else{
                        holder.Instruct.setText(R.string.Post_Elem_Instruct_Img);
                        holder.Instruct.setVisibility(View.VISIBLE);
                    holder.Icon.setImageResource(R.drawable.ic_loupe_black_24dp);
                    holder.Icon.setVisibility(View.VISIBLE);
                        holder.MediaView.setImageBitmap(BitmapMap.get(publication.ID));
                    holder.Loading.setVisibility(View.GONE);
                        holder.MediaView.setVisibility(View.VISIBLE);
                        holder.MediaView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Dialog dialog = new Dialog(c);
                                dialog.setContentView(R.layout.image_dialog);
                                dialog.show();
                                final ImageView Image = dialog.findViewById(R.id.image_dialog_imageview);
                                final ProgressBar progressBar = dialog.findViewById(R.id.image_dialog_progressBar);
                                StorageReference FullImgRef = FirebaseStorage.getInstance().getReference().child(publication.Media);
                                FullImgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        Image.setImageBitmap(b);
                                        progressBar.setVisibility(View.GONE);
                                        Image.setVisibility(View.VISIBLE);
                                    }
                                });
                                final Button CloseBtn = dialog.findViewById(R.id.ImageCloseBtn);
                                CloseBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });

                            }
                        });
                }
            }else if(publication.Type == Publication.TYPE_VIDEO){

                if(BitmapMap.get(publication.ID) == null) {
                    String UrlImg = FireBaseInteraction.Storage_Paths.PUBLICATION_VIDEOS_THUMBNAILS + publication.ID + ".jpg";
                    StorageReference AvatarRef = FirebaseStorage.getInstance().getReference().child(UrlImg);
                    AvatarRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            holder.Instruct.setText(R.string.Post_Elem_Instruct_Vid);
                            holder.Instruct.setVisibility(View.VISIBLE);
                            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            holder.Icon.setImageResource(R.drawable.ic_play_circle_color);
                            holder.Icon.setVisibility(View.VISIBLE);
                            holder.MediaView.setImageBitmap(b);
                            BitmapMap.put(publication.ID, b);
                            holder.MediaView.setVisibility(View.VISIBLE);
                            holder.Loading.setVisibility(View.GONE);
                            holder.MediaView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(c, VideoPopupActivity.class);
                                    intent.putExtra("Path", publication.Media);
                                    intent.putExtra("NotOnServer", false);
                                    c.startActivity(intent);
                                }
                            });
                        }
                    });
                }else{
                    holder.Instruct.setText(R.string.Post_Elem_Instruct_Vid);
                    holder.Instruct.setVisibility(View.VISIBLE);
                    holder.Icon.setImageResource(R.drawable.ic_play_circle_color);
                    holder.Icon.setVisibility(View.VISIBLE);
                    holder.MediaView.setImageBitmap(BitmapMap.get(publication.ID));
                    holder.Loading.setVisibility(View.GONE);
                    holder.MediaView.setVisibility(View.VISIBLE);
                    holder.MediaView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(c, VideoPopupActivity.class);
                            intent.putExtra("Path", publication.Media);
                            intent.putExtra("NotOnServer", false);
                            c.startActivity(intent);
                        }
                    });
                }
            }else{
                if(publication.Media.equals("N")){
                    holder.Instruct.setText(R.string.Post_Elem_Instruct_Poll);
                    holder.Instruct.setVisibility(View.VISIBLE);
                    holder.Icon.setImageResource(R.drawable.ic_sondage_debut);
                    holder.Icon.setVisibility(View.VISIBLE);
                    holder.MediaView.setImageResource(R.mipmap.ic_launcher);
                    holder.Loading.setVisibility(View.GONE);
                    holder.MediaView.setVisibility(View.VISIBLE);
                    DatabaseReference SondageRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME).child(publication.SondageRef);
                    SondageRef.addListenerForSingleValueEvent(PollLinkListeners);
                }else {
                    if(BitmapMap.get(publication.ID) == null) {
                        StorageReference ImageRef = FirebaseStorage.getInstance().getReference().child(publication.Media);
                        ImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                holder.Instruct.setText(R.string.Post_Elem_Instruct_Poll);
                                holder.Instruct.setVisibility(View.VISIBLE);
                                Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                holder.Icon.setImageResource(R.drawable.ic_sondage_debut);
                                holder.Icon.setVisibility(View.VISIBLE);
                                holder.MediaView.setImageBitmap(b);
                                holder.Loading.setVisibility(View.GONE);
                                BitmapMap.put(publication.ID, b);
                                holder.MediaView.setVisibility(View.VISIBLE);
                                DatabaseReference SondageRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME).child(publication.SondageRef);
                                SondageRef.addListenerForSingleValueEvent(PollLinkListeners);

                            }
                        });
                    }else{
                        holder.Instruct.setText(R.string.Post_Elem_Instruct_Poll);
                        holder.Instruct.setVisibility(View.VISIBLE);
                        holder.Icon.setImageResource(R.drawable.ic_sondage_debut);
                        holder.Icon.setVisibility(View.VISIBLE);

                        holder.MediaView.setImageBitmap(BitmapMap.get(publication.ID));
                        holder.Loading.setVisibility(View.GONE);
                        holder.MediaView.setVisibility(View.VISIBLE);
                        DatabaseReference SondageRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME).child(publication.SondageRef);
                        SondageRef.addListenerForSingleValueEvent(PollLinkListeners);
                    }
                }
            }
        }else{
            holder.Loading.setVisibility(View.GONE);
            holder.MediaView.setVisibility(View.GONE);
        }
        if(publication.AuteurRef.equals(user.getUid())){
            holder.ZoneAdmin.setVisibility(View.VISIBLE);
            holder.Shadow.setVisibility(View.VISIBLE);
            holder.BtnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((AdaptListener)c).changePage(CreatePostFragment.newInstance(true, publication));
                }
            });
            holder.BtnSupp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(c)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Supprimer la publication")
                            .setMessage("Voulez-vous vraiment supprimer cette publication?")
                            .setPositiveButton("Oui", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final ProgressDialog progressDialog = new ProgressDialog(c);
                                    progressDialog.setTitle("Suppression de la publication...");
                                    progressDialog.show();
                                    Thread mThread = new Thread(){
                                        @Override
                                        public void run(){
                                            final DatabaseReference PublicationRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Publications_Keys.STRUCT_NAME).child(publication.ID);
                                            if(publication.Type == Publication.TYPE_VIDEO){
                                                StorageReference PubMediaRef = FirebaseStorage.getInstance().getReference().child(publication.Media);
                                                PubMediaRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_VIDEOS_THUMBNAILS).child(publication.ID+".jpg").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                PublicationRef.removeValue();
                                                                publicationAdapter.remove(publication);
                                                                publicationAdapter.notifyDataSetChanged();
                                                                progressDialog.dismiss();

                                                            }
                                                        });
                                                    }
                                                });
                                            }else if(publication.Type == Publication.TYPE_IMAGE){
                                                FirebaseStorage.getInstance().getReference().child(publication.Media).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_IMAGES_THUMBNAILS).child(publication.ID+".jpg").delete();
                                                        PublicationRef.removeValue();
                                                        publicationAdapter.remove(publication);
                                                        publicationAdapter.notifyDataSetChanged();
                                                        progressDialog.dismiss();
                                                    }
                                                });
                                            }else{//que ce soit sondage ou texte
                                                PublicationRef.removeValue();
                                                publicationAdapter.remove(publication);
                                                publicationAdapter.notifyDataSetChanged();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    };
                                    mThread.start();
                                }

                            })
                            .setNegativeButton("Non", null)
                            .show();
                }
            });
        }else{
            holder.ZoneAdmin.setVisibility(View.GONE);
            holder.Shadow.setVisibility(View.GONE);
        }
        holder.BtnPlainte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    FragmentManager fm = ((AdaptListener)c).getFragmentManagerQ();
                    FragmentTransaction ft = fm.beginTransaction();
                    android.app.Fragment prev = fm.findFragmentByTag("fragment_dialog_plainte");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    //la vidéo n'est pas en ligne;
                    DialogPlainteFragment plainteFragment = DialogPlainteFragment.newInstance(publication.ID, Plainte.TYPE_PUBLICATION);
                    plainteFragment.show(ft, "fragment_dialog_plainte");
            }
        });

        return view;
    }
    private class SondageImgLoadListener implements ValueEventListener {
        PublicationAdapter.ViewHolder holder;
        FirebaseUser user;
        SondageImgLoadListener(PublicationAdapter.ViewHolder holder, FirebaseUser user){
            this.holder = holder;
            this.user = user;
        }
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            final Sondage S = dataSnapshot.getValue(Sondage.class);
            S.ID = dataSnapshot.getKey();
            S.ConvertDates();

            boolean Resultat;//pour det si r = true; il faut que date_echean < date Aujd, et si il fait partie de la liste répondu

            if(S.date_echeance.after(Calendar.getInstance().getTime())){//on ne peut pas répondre apres la date;
                if(user != null){//si il est connecté quand le sondage est en cours
                    Profil UtilConn = ((AdaptListener)c).getUtilisateur_Connecte();
                    if(user.getUid().equals(S.AuteurRef)){
                        holder.Instruct.setText(R.string.Post_Elem_Instruct_Poll);
                        holder.Instruct.setVisibility(View.VISIBLE);
                        holder.AlertSondage.setText(R.string.Post_Elem_Alert_Poll_Results);
                        holder.AlertSondage.setVisibility(View.VISIBLE);
                        holder.MediaView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, true));
                            }
                        });
                    }else if(UtilConn.Sondages_Faits.containsKey(S.ID)) {
                        if (UtilConn.Sondages_Faits.get(S.ID) instanceof Boolean) {//si il a terminé, le sondage a été répondu
                            //ne peut plus répondre
                            holder.AlertSondage.setText(R.string.Post_Elem_Alert_Poll_Answered);
                            holder.AlertSondage.setVisibility(View.VISIBLE);
                        } else {//si il a sauvegardé, on réponds avec sauvegarde;
                            //sauvegarde
                            holder.Instruct.setText(R.string.Post_Elem_Instruct_Poll);
                            holder.Instruct.setVisibility(View.VISIBLE);
                            holder.AlertSondage.setText(R.string.Post_Elem_Alert_Poll_Continue);
                            holder.AlertSondage.setVisibility(View.VISIBLE);
                            holder.MediaView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, false));
                                }
                            });
                        }
                    }else{
                        holder.Instruct.setText(R.string.Post_Elem_Instruct_Poll);
                        holder.Instruct.setVisibility(View.VISIBLE);
                        holder.MediaView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, false));
                            }
                        });
                    }


                }else{//si l'util n'est pas connecté quand le sondage est en cours, il réponds
                    holder.Instruct.setText(R.string.Post_Elem_Instruct_Poll);
                    holder.Instruct.setVisibility(View.VISIBLE);
                    holder.MediaView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, false));
                        }
                    });
                }
            }else{//si le sondage est terminé
                if(user != null){//si l'util est connecté
                    Profil UtilConn = ((AdaptListener)c).getUtilisateur_Connecte();
                    if(user.getUid().equals(S.AuteurRef)){
                        holder.Instruct.setText(R.string.Post_Elem_Instruct_Poll);
                        holder.Instruct.setVisibility(View.VISIBLE);
                        holder.AlertSondage.setText(R.string.Post_Elem_Alert_Poll_Results);
                        holder.AlertSondage.setVisibility(View.VISIBLE);
                        holder.MediaView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, true));
                            }
                        });
                    }else if(UtilConn.Sondages_Faits.containsKey(S.ID)) {
                        if (UtilConn.Sondages_Faits.get(S.ID) instanceof Boolean) {//si la réponse est terminée..
                            //peut consulter
                            if (S.Compil_Public) {//si la compilation est publique
                                holder.Instruct.setText(R.string.Post_Elem_Instruct_Poll);
                                holder.Instruct.setVisibility(View.VISIBLE);
                                holder.AlertSondage.setText(R.string.Post_Elem_Alert_Poll_Results);
                                holder.AlertSondage.setVisibility(View.VISIBLE);
                                holder.MediaView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, true));
                                    }
                                });
                            } else {
                                holder.AlertSondage.setText(R.string.Post_Elem_Alert_Poll_Answered);
                                holder.AlertSondage.setVisibility(View.VISIBLE);
                            }
                        } else {
                            //ne peut pas consulter
                            holder.AlertSondage.setText(R.string.Post_Elem_Alert_Poll_Finished);
                            holder.AlertSondage.setVisibility(View.VISIBLE);
                        }
                    }else{
                        holder.AlertSondage.setText(R.string.Post_Elem_Alert_Poll_Finished);
                        holder.AlertSondage.setVisibility(View.VISIBLE);
                    }

                }else{//si il n'est pas connecté et que le sondage est terminé, trop tard
                    //nope
                    holder.AlertSondage.setText(R.string.Post_Elem_Alert_Poll_Finished);
                    holder.AlertSondage.setVisibility(View.VISIBLE);
                }
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }

    static class ViewHolder{
        TextView TitreView, TexteView, UserNameView, DateView, AlertSondage, Instruct;
        ImageView MediaView, Icon, AvatarView;
        LinearLayout ZoneAdmin;
        View Shadow;
        ProgressBar Loading;
        ImageButton BtnPlainte, BtnSupp, BtnEdit;
        ViewHolder(View view){
            Icon = view.findViewById(R.id.pub_elem_img_icon);
            Instruct = view.findViewById(R.id.pub_elem_instruct);
            TitreView = view.findViewById(R.id.pub_elem_titre);
            TexteView = view.findViewById(R.id.pub_elem_texte);
            UserNameView = view.findViewById(R.id.pub_elem_username);
            DateView = view.findViewById(R.id.pub_elem_date);
            AlertSondage = view.findViewById(R.id.pub_elem_sondage_alert);
            MediaView = view.findViewById(R.id.pub_elem_image);
            AvatarView = view.findViewById(R.id.pub_elem_avatar);
            ZoneAdmin = view.findViewById(R.id.pub_elem_zone_admin);
            BtnPlainte = view.findViewById(R.id.pub_elem_plainte_btn);
            BtnSupp = view.findViewById(R.id.pub_elem_del_btn);
            Loading = view.findViewById(R.id.pub_elem_progress);
            BtnEdit = view.findViewById(R.id.pub_elem_edit_btn);
            Shadow = view.findViewById(R.id.PubElemShadow);
        }
    }

    public interface AdaptListener{
        FirebaseUser getUser();
        void changePage(Fragment fragment);
        FragmentManager getFragmentManagerQ();
        Profil getUtilisateur_Connecte();
    }
}
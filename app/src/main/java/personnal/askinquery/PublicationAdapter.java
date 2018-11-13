package personnal.askinquery;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PublicationAdapter extends ArrayAdapter<Publication> {
    Context c;
    boolean Si_Admin;
    PublicationAdapter publicationAdapter;

    public PublicationAdapter(Context context, ArrayList<Publication> publications, boolean Si_Admin){
        super(context, 0, publications);
        c = context;
        this.Si_Admin = Si_Admin;
        publicationAdapter = this;
    }

    public View getView(final int position, View convertView, final ViewGroup parent){
        final Publication publication = getItem(position);
        View view = convertView;
        final PublicationAdapter.ViewHolder holder;
        final FirebaseUser user = ((AdaptListener)c).getUser();
        if(view == null){
            view = LayoutInflater.from(c).inflate(R.layout.publication_element, parent, false);
            holder = new PublicationAdapter.ViewHolder(view, position);

            view.setTag(holder);
        }else{
            holder = (PublicationAdapter.ViewHolder)view.getTag();
        }
        final ValueEventListener PollLinkListeners = new ValueEventListener() {
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
                            holder.AlertSondage.setText("Consulter les résultats");
                            holder.AlertSondage.setVisibility(View.VISIBLE);
                            holder.MediaView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, true, "Publications", Si_Admin));
                                }
                            });
                        }else if(UtilConn.Sondages_Faits.containsKey(S.ID)) {
                            if (UtilConn.Sondages_Faits.get(S.ID) instanceof Boolean) {//si il a terminé, le sondage a été répondu
                                //ne peut plus répondre
                                holder.AlertSondage.setText("Sondage Répondu");
                                holder.AlertSondage.setVisibility(View.VISIBLE);
                            } else {//si il a sauvegardé, on réponds avec sauvegarde;
                                //sauvegarde
                                holder.AlertSondage.setText("Continuer");
                                holder.AlertSondage.setVisibility(View.VISIBLE);
                                holder.MediaView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, false, "Publications", Si_Admin));
                                    }
                                });
                            }
                        }else{
                            holder.MediaView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, false, "Publications", Si_Admin));
                                }
                            });
                        }


                    }else{//si l'util n'est pas connecté quand le sondage est en cours, il réponds
                        holder.MediaView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, false, "Publications", Si_Admin));
                            }
                        });
                    }
                }else{//si le sondage est terminé
                    if(user != null){//si l'util est connecté
                        Profil UtilConn = ((AdaptListener)c).getUtilisateur_Connecte();
                        if(user.getUid().equals(S.AuteurRef)){
                            holder.AlertSondage.setText("Consulter les résultats");
                            holder.AlertSondage.setVisibility(View.VISIBLE);
                            holder.MediaView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, true, "Publications", Si_Admin));
                                }
                            });
                        }else if(UtilConn.Sondages_Faits.containsKey(S.ID)) {
                            if (UtilConn.Sondages_Faits.get(S.ID) instanceof Boolean) {//si la réponse est terminée..
                                //peut consulter
                                if (S.Compil_Public) {//si la compilation est publique
                                    holder.AlertSondage.setText("Consulter les résultats");
                                    holder.AlertSondage.setVisibility(View.VISIBLE);
                                    holder.MediaView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(S, true, "Publications", Si_Admin));
                                        }
                                    });
                                } else {
                                    holder.AlertSondage.setText("Sondage Répondu");
                                    holder.AlertSondage.setVisibility(View.VISIBLE);
                                }
                            } else {
                                //ne peut pas consulter
                                holder.AlertSondage.setText("Sondage Terminé");
                                holder.AlertSondage.setVisibility(View.VISIBLE);
                            }
                        }else{
                            holder.AlertSondage.setText("Sondage Terminé");
                            holder.AlertSondage.setVisibility(View.VISIBLE);
                        }

                    }else{//si il n'est pas connecté et que le sondage est terminé, trop tard
                        //nope
                        holder.AlertSondage.setText("Sondage Terminé");
                        holder.AlertSondage.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
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

        if(publication.Type != Publication.TYPE_TEXTE){
            if(publication.Type == Publication.TYPE_IMAGE){
                StorageReference AvatarRef = FirebaseStorage.getInstance().getReference().child(publication.Media);
                AvatarRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        holder.MediaView.setImageBitmap(b);
                        holder.MediaView.setVisibility(View.VISIBLE);
                    }
                });
            }else if(publication.Type == Publication.TYPE_VIDEO){
                String UrlImg = FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS_THUMBNAILS+publication.ID+".jpg";
                StorageReference AvatarRef = FirebaseStorage.getInstance().getReference().child(UrlImg);
                AvatarRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        holder.MediaView.setImageBitmap(b);
                        holder.MediaView.setVisibility(View.VISIBLE);
                        holder.MediaView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FragmentManager fm = ((AdaptListener)c).getFragmentManagerQ();
                                FragmentTransaction ft = fm.beginTransaction();
                                android.app.Fragment prev = fm.findFragmentByTag("fragment_video_dialog");
                                if (prev != null) {
                                    ft.remove(prev);
                                }
                                ft.addToBackStack(null);
                                //la vidéo n'est pas en ligne;
                                VideoDialogFragment creerOptionDialog = VideoDialogFragment.newInstance(publication.Media, false);
                                creerOptionDialog.show(ft, "fragment_video_dialog");
                            }
                        });
                    }
                });
            }else{
                if(publication.Media.equals("N")){
                    holder.MediaView.setImageResource(R.mipmap.ic_launcher);
                    holder.MediaView.setVisibility(View.VISIBLE);
                    DatabaseReference SondageRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME).child(publication.SondageRef);
                    SondageRef.addListenerForSingleValueEvent(PollLinkListeners);
                }else {
                    StorageReference AvatarRef = FirebaseStorage.getInstance().getReference().child(publication.Media);
                    AvatarRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            holder.MediaView.setImageBitmap(b);
                            holder.MediaView.setVisibility(View.VISIBLE);
                            DatabaseReference SondageRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME).child(publication.SondageRef);
                            SondageRef.addListenerForSingleValueEvent(PollLinkListeners);

                        }
                    });
                }
            }
        }else{
            holder.MediaView.setVisibility(View.GONE);
        }
        if(publication.AuteurRef.equals(user.getUid())){
            holder.ZoneAdmin.setVisibility(View.VISIBLE);
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

                                                    }
                                                });
                                            }
                                        });
                                    }else if(publication.Type == Publication.TYPE_IMAGE){
                                        FirebaseStorage.getInstance().getReference().child(publication.Media).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                PublicationRef.removeValue();
                                                publicationAdapter.remove(publication);
                                                publicationAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }else{//que ce soit sondage ou texte
                                        PublicationRef.removeValue();
                                        publicationAdapter.remove(publication);
                                        publicationAdapter.notifyDataSetChanged();
                                    }


                                }

                            })
                            .setNegativeButton("Non", null)
                            .show();
                }
            });
        }else{
            holder.ZoneAdmin.setVisibility(View.GONE);
        }

        return view;
    }

    static class ViewHolder{
        TextView TitreView, TexteView, UserNameView, DateView, AlertSondage;
        ImageView MediaView;
        ImageView AvatarView;
        LinearLayout ZoneAdmin;
        ImageButton BtnPlainte, BtnSupp, BtnEdit;
        public ViewHolder(View view, int position){
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
            BtnEdit = view.findViewById(R.id.pub_elem_edit_btn);
        }
    }

    public interface AdaptListener{
        FirebaseUser getUser();
        void changePage(Fragment fragment);
        FragmentManager getFragmentManagerQ();
        Profil getUtilisateur_Connecte();
    }
}
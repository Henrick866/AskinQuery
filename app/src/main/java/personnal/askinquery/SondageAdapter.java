package personnal.askinquery;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Henrick on 2018-09-20.
 */

public class SondageAdapter  extends ArrayAdapter<Sondage>{
    private boolean si_Admin;
    private static Context c;
    private LayoutInflater mInflater;
    private static AdaptListener adaptListener;
    private SondageAdapter sondageAdapter;
    private boolean Si_Filter;
    private HashMap<String, Bitmap> BitmapMap;

    public SondageAdapter(Context context, ArrayList<Sondage> sondages, boolean Si_Admin, boolean Si_Filter){
        super(context, 0, sondages);
        si_Admin = Si_Admin;
        this.Si_Filter = Si_Filter;
        c = context;
        BitmapMap = new HashMap<>();
        mInflater = LayoutInflater.from(context);
        sondageAdapter = this;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        final Sondage sondage = getItem(position);
        final SondageAdapter.ViewHolder holder;

        View view = convertView;
        if(view == null){
            view = mInflater.inflate(R.layout.sondage_element, parent, false);
            holder = new SondageAdapter.ViewHolder(view);
            view.setTag(holder);
        }else{
            holder = (SondageAdapter.ViewHolder)view.getTag();
        }

        DatabaseReference AuteurRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(sondage.AuteurRef);

        sondage.ConvertDates();
        holder.btn_plainte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        if(((AdaptListener)c).getUser() == null){
            holder.zone_Admin.setVisibility(View.GONE);
        }else{
            if(((AdaptListener)c).getUser().getUid().equals(sondage.AuteurRef)){
                holder.btn_commencer.setVisibility(View.GONE);
            }else{
                holder.zone_Admin.setVisibility(View.GONE);
            }
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(sondage.date_echeance.after(Calendar.getInstance().getTime())){//on ne peut pas répondre apres la date;
            if(user != null){//si il est connecté quand le sondage est en cours
                Profil UtilConn = ((AdaptListener)c).getUtilisateur_Connecte();
                if(UtilConn.Sondages_Faits.isEmpty()){//si c'est la première fois et que le sondage est en coursm il réponds
                    holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, false, Si_Filter?sondage.Auteur.ID:null, si_Admin));
                        }
                    });
                }else{//si ce n'est pas la première fois;
                    if(UtilConn.Sondages_Faits.containsKey(sondage.ID)) {
                        if (UtilConn.Sondages_Faits.get(sondage.ID) instanceof Boolean) {//si il a terminé, le sondage a été répondu
                            //ne peut plus répondre
                            holder.btn_commencer.setEnabled(false);
                            holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorAccentMedDark));
                            holder.btn_commencer.setText("Sondage Répondu");
                        } else {//si il a sauvegardé, on réponds avec sauvegarde;
                            //sauvegarde
                            holder.btn_commencer.setText("Continuer");
                            holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, false, Si_Filter ? sondage.Auteur.ID : null, si_Admin));
                                }
                            });
                        }
                    }else{
                        holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, false, Si_Filter?sondage.Auteur.ID:null, si_Admin));
                            }
                        });
                    }
                }

            }else{//si l'util n'est pas connecté quand le sondage est en cours, il réponds
                holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, false, Si_Filter?sondage.Auteur.ID:null, si_Admin));
                    }
                });
            }
        }else{//si le sondage est terminé
            if(user != null){//si l'util est connecté
                Profil UtilConn = ((AdaptListener)c).getUtilisateur_Connecte();
                    if(UtilConn.Sondages_Faits.containsKey(sondage.ID)) {
                        if (UtilConn.Sondages_Faits.get(sondage.ID) instanceof Boolean) {//si la réponse est terminée..
                            //peut consulter
                            if (sondage.Compil_Public) {//si la compilation est publique
                                holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorSecondaryMedDark));
                                holder.btn_commencer.setText("Consulter les résultats");
                                holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, true, Si_Filter ? sondage.Auteur.ID : null, si_Admin));
                                    }
                                });
                            } else {
                                holder.btn_commencer.setEnabled(false);
                                holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorAccentMedDark));
                                holder.btn_commencer.setText("Sondage Répondu");
                            }
                        } else {
                            //ne peut pas consulter
                            holder.btn_commencer.setEnabled(false);
                            holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorAccentMedDark));
                            holder.btn_commencer.setText("Sondage Terminé");
                        }
                    }else{
                        holder.btn_commencer.setEnabled(false);
                        holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorAccentMedDark));
                        holder.btn_commencer.setText("Sondage Terminé");
                    }
            }else{//si il n'est pas connecté et que le sondage est terminé, trop tard
                //nope
                holder.btn_commencer.setEnabled(false);
                holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorAccentMedDark));
                holder.btn_commencer.setText("Sondage Terminé");
            }
        }
        if(sondage.Chemin_Image.equals("N")){
            holder.Image.setVisibility(View.GONE);
        }else{
                if (BitmapMap.get(sondage.ID) != null) {
                    holder.Image.setImageBitmap(BitmapMap.get(sondage.ID));
                } else {
                    StorageReference SondageImgRef = FirebaseStorage.getInstance().getReference().child(sondage.Chemin_Image);
                    SondageImgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap Image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            BitmapMap.put(sondage.ID, Image);
                            holder.Image.setImageBitmap(Image);
                        }
                    });
                }
        }
        holder.Titre.setText(sondage.Titre);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");
        holder.DateDebut.setText("Publié le: "+format.format(sondage.date_public));
        holder.DateFin.setText("Fini le : "+format.format(sondage.date_echeance));

        holder.id_Sondage.setText(sondage.ID);
        ValueEventListener auteurListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sondage.Auteur = dataSnapshot.getValue(Profil.class);
                sondage.Auteur.ID = dataSnapshot.getKey();
                holder.AuteurNom.setText(sondage.Auteur.Username);
                holder.AuteurNom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((AdaptListener)c).changePage(ConsultProfilFragment.newInstance(sondage.Auteur));
                    }
                });
                holder.id_Auteur.setText(sondage.Auteur.ID);
                holder.btn_modifier.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((AdaptListener)c).changePage(CreerSondageFragment.newInstance(true, true, sondage.Auteur, sondage));
                    }
                });
                holder.btn_Statistiques.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((AdaptListener)c).changePage(AnswerSondageFragment.newInstance(sondage, true, Si_Filter?sondage.Auteur.ID:null, si_Admin));
                    }
                });
                holder.btn_supprimer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Confirm(sondage);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        AuteurRef.addListenerForSingleValueEvent(auteurListener);
        return view;
    }
    private class ViewHolder{
        TextView AuteurNom;
        ImageButton btn_plainte;
        LinearLayout zone_Admin;
        Button btn_commencer;
        RelativeLayout btnVote;
        ImageButton btn_modifier;
        ImageButton btn_Statistiques;
        ImageButton btn_supprimer;
        TextView Titre;
        TextView DateDebut;
        TextView DateFin;
        TextView id_Sondage;
        TextView id_Auteur;
        ImageView Image;
        public ViewHolder(View view){
            AuteurNom = view.findViewById(R.id.sondage_elem_nom_auteur);
            btn_plainte = view.findViewById(R.id.sondage_elem_plainte_btn);
            zone_Admin = view.findViewById(R.id.sondage_elem_zone_admin);
            btn_commencer = view.findViewById(R.id.sondage_elem_start_btn);

            btnVote = view.findViewById(R.id.btn_vote_layout);

            btn_modifier = view.findViewById(R.id.sondage_elem_edit_btn);
            btn_Statistiques = view.findViewById(R.id.sondage_elem_stats_btn);
            btn_supprimer = view.findViewById(R.id.sondage_elem_delete_btn);

            Titre = view.findViewById(R.id.sondage_elem_titre);
            DateDebut = view.findViewById(R.id.sondage_elem_date_debut);
            DateFin = view.findViewById(R.id.sondage_elem_date_fin);
            id_Sondage = view.findViewById(R.id.sondage_elem_id);
            id_Auteur = view.findViewById(R.id.sondage_elem_id_auteur);
            Image = view.findViewById(R.id.sondage_elem_image);
        }
    }
    private void Confirm(final Sondage s){
        new AlertDialog.Builder(c)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Supprimer le sondage")
                .setMessage("Voulez-vous vraiment supprimer ce sondage?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference QuestionsRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Question_Keys.STRUCT_NAME);
                        QuestionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ArrayList<Question> Questions = new ArrayList<>();
                                String SondageRef;
                                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                                    Question q = dataSnapshot1.getValue(Question.class);
                                    SondageRef = dataSnapshot1.child(FireBaseInteraction.Question_Keys.SONDAGE_REF).getValue().toString();
                                    if(SondageRef.equals(s.ID)){
                                        ArrayList<Option> Options = new ArrayList<>();
                                        for(DataSnapshot dataSnapshot2 : dataSnapshot1.child(FireBaseInteraction.Question_Keys.OPTIONS).getChildren()){
                                            Option o = dataSnapshot2.getValue(Option.class);
                                            o.ID = dataSnapshot2.getKey();
                                            Options.add(o);
                                        }
                                        q.ID = dataSnapshot1.getKey();
                                        q.Options = Options;
                                        Questions.add(q);
                                    }

                                }
                                s.Questions = Questions;
                                DeleteSondage(s);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                })
                .setNegativeButton("Non", null)
                .show();
    }
    private void DeleteSondage(Sondage s){
        ProgressDialog progressDialog = new ProgressDialog(c);
        progressDialog.setTitle("Suppression du sondage en cours...");
        progressDialog.show();
        DatabaseReference SondageRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME).child(s.ID);
        StorageReference SondageMediaRef = FirebaseStorage.getInstance().getReference();
        for(Question q : s.Questions){
            DatabaseReference QuestionRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Question_Keys.STRUCT_NAME).child(q.ID);
            if(q.Type_Question != Question.TYPE_TEXTE) {
                for (Option o : q.Options){
                    StorageReference OptionMediaRef = FirebaseStorage.getInstance().getReference().child(o.Chemin_Media);
                    OptionMediaRef.delete();
                }
            }
            QuestionRef.removeValue();
        }
        if(!s.Chemin_Image.equals("N")){
            SondageMediaRef.child(s.Chemin_Image);
            SondageMediaRef.delete();
        }
        SondageRef.removeValue();
        progressDialog.setTitle("Sondage supprimé.");
        progressDialog.dismiss();
        sondageAdapter.remove(s);
        sondageAdapter.notifyDataSetChanged();

    }
    public interface AdaptListener{
        void changePage(Fragment fragment);
        FirebaseUser getUser();
        Profil getUtilisateur_Connecte();
    }
}


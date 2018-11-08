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
    private HashMap<String, Object> MapSave;

    public SondageAdapter(Context context, ArrayList<Sondage> sondages, boolean Si_Admin, boolean Si_Filter, HashMap<String, Object> mapSaved){
        super(context, 0, sondages);
        si_Admin = Si_Admin;
        this.Si_Filter = Si_Filter;
        c = context;
        MapSave = mapSaved;
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){//si il est connecté (anonymement ou non)
            if(!user.getUid().equals(sondage.AuteurRef)){//si son id ne correspond pas à celui de l'auteurRef (cette valeur est déja initialisé au moment de la création de cet objet)
                                                            //nb : un utilisateur anonyme NE PEUT PAS créér un formulaire, donc si cette condition est fausse, l'utilisateur a un compte non anonyme.
                    holder.zone_Admin.setVisibility(View.GONE);
                if(((AdaptListener)c).getUser() != null) {//couvre anonyme et inscrit, code pour déterminer le bouton principal.
                    //todo:: prechargez la liste des sondages terminés dans listsondagefragment

                    if(MapSave.get(sondage.ID) != null){
                        Object Value = MapSave.get(sondage.ID);
                        if(Value instanceof Boolean){
                            boolean b = (boolean)Value;
                            if(b){
                                Calendar calendar = Calendar.getInstance();
                                if(sondage.date_echeance.before(calendar.getTime())){
                                    if(sondage.Compil_Public){
                                        holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorSecondaryMedDark));
                                        holder.btn_commencer.setText("Consulter les résultats");
                                        holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                ((AdaptListener)c).changePage(AnswerSondageFragment.newInstance(sondage, true, Si_Filter?sondage.Auteur.ID:null, si_Admin), "Sondage | Résultats");
                                            }
                                        });
                                    }else{
                                        holder.btn_commencer.setEnabled(false);
                                        holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorAccentMedDark));
                                        holder.btn_commencer.setText("Sondage Terminé");
                                    }

                                }else{
                                    holder.btn_commencer.setEnabled(false);
                                    holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorAccentMedDark));
                                    holder.btn_commencer.setText("Sondage Répondu");
                                }
                            }
                        }else{
                            holder.btn_commencer.setText("Continuer");
                            holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, false, Si_Filter?sondage.Auteur.ID:null, si_Admin), "Sondage | Répondre");
                                }
                            });
                        }
                    }else{
                        holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, false, Si_Filter?sondage.Auteur.ID:null, si_Admin), "Sondage | Répondre");
                            }
                        });
                    }
                }else {
                    holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, false, Si_Filter?sondage.Auteur.ID:null, si_Admin), "Sondage | Répondre");
                        }
                    });
                }
            }else{
                holder.btnVote.setVisibility(View.GONE);
                holder.btn_plainte.setVisibility(View.GONE);
                holder.AuteurNom.setVisibility(View.GONE);
            }
        }else{
            holder.zone_Admin.setVisibility(View.GONE);
            holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, false, Si_Filter?sondage.Auteur.ID:null, si_Admin), "Sondage | Répondre");
                }
            });
        }
        if(sondage.Chemin_Image.equals("N")){
            holder.Image.setVisibility(View.GONE);
        }else{
            StorageReference SondageImgRef = FirebaseStorage.getInstance().getReference().child(sondage.Chemin_Image);
            SondageImgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap Image = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                    holder.Image.setImageBitmap(Image);
                }
            });
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
                        ((AdaptListener)c).changePage(ConsultProfilFragment.newInstance(sondage.Auteur), "Profil | Consulter");
                    }
                });
                holder.id_Auteur.setText(sondage.Auteur.ID);
                holder.btn_modifier.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((AdaptListener)c).changePage(CreerSondageFragment.newInstance(true, true, sondage.Auteur, sondage), "Sondage | Modifier");
                    }
                });
                holder.btn_Statistiques.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((AdaptListener)c).changePage(AnswerSondageFragment.newInstance(sondage, true, Si_Filter?sondage.Auteur.ID:null, si_Admin), "Sondage | Résultats");
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
            AuteurNom = (TextView)view.findViewById(R.id.sondage_elem_nom_auteur);
            btn_plainte = (ImageButton)view.findViewById(R.id.sondage_elem_plainte_btn);
            zone_Admin = (LinearLayout)view.findViewById(R.id.sondage_elem_zone_admin);
            btn_commencer = (Button)view.findViewById(R.id.sondage_elem_start_btn);

            btnVote = (RelativeLayout)view.findViewById(R.id.btn_vote_layout);

            btn_modifier = (ImageButton)view.findViewById(R.id.sondage_elem_edit_btn);
            btn_Statistiques = (ImageButton)view.findViewById(R.id.sondage_elem_stats_btn);
            btn_supprimer = (ImageButton)view.findViewById(R.id.sondage_elem_delete_btn);

            Titre = (TextView)view.findViewById(R.id.sondage_elem_titre);
            DateDebut = (TextView)view.findViewById(R.id.sondage_elem_date_debut);
            DateFin = (TextView)view.findViewById(R.id.sondage_elem_date_fin);
            id_Sondage = (TextView)view.findViewById(R.id.sondage_elem_id);
            id_Auteur = (TextView)view.findViewById(R.id.sondage_elem_id_auteur);
            Image = (ImageView)view.findViewById(R.id.sondage_elem_image);
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
        sondageAdapter.notifyDataSetChanged();

    }
    public interface AdaptListener{
        void changePage(Fragment fragment, String Title);
        FirebaseUser getUser();
    }
}


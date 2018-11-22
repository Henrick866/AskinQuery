package personnal.askinquery;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private FirebaseFunctions mFunctions;

    public SondageAdapter(Context context, ArrayList<Sondage> sondages, boolean Si_Admin, boolean Si_Filter){
        super(context, 0, sondages);
        si_Admin = Si_Admin;
        this.Si_Filter = Si_Filter;
        c = context;
        BitmapMap = new HashMap<>();
        mFunctions = FirebaseFunctions.getInstance();
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
                FragmentManager fm = ((AdaptListener)c).getFragmentManagerQ();
                FragmentTransaction ft = fm.beginTransaction();
                android.app.Fragment prev = fm.findFragmentByTag("fragment_dialog_plainte");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                //la vidéo n'est pas en ligne;
                DialogPlainteFragment plainteFragment = DialogPlainteFragment.newInstance(sondage.ID, Plainte.TYPE_SONDAGE);
                plainteFragment.show(ft, "fragment_dialog_plainte");
            }
        });
        if(((AdaptListener)c).getUser() == null){
            holder.zone_Admin.setVisibility(View.GONE);
        }else{
            if(((AdaptListener)c).getUser().getUid().equals(sondage.AuteurRef)){
                holder.btn_commencer.setVisibility(View.GONE);
                if(!sondage.Publied){
                    holder.btn_Statistiques.setImageDrawable(c.getResources().getDrawable(R.drawable.ic_publish));
                    holder.BtnStatText.setText(R.string.Sondage_Elem_BtnPublish_Desc);
                    holder.btn_Statistiques.setContentDescription(c.getString(R.string.Sondage_Elem_BtnPublish_Desc));
                }else{
                    holder.btn_modifier.setEnabled(false);
                    holder.BtnEditText.setText(R.string.Sondage_Elem_BtnEdit_Desc_Disabled);
                    holder.btn_modifier.setContentDescription(c.getString(R.string.Sondage_Elem_BtnEdit_Desc_Disabled));
                }

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
                            holder.btn_commencer.setText(R.string.Sondage_Elem_Poll_Answered);
                        } else {//si il a sauvegardé, on réponds avec sauvegarde;
                            //sauvegarde
                            holder.btn_commencer.setText(R.string.Sondage_Elem_Poll_Saved);
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
                                holder.btn_commencer.setText(R.string.Sondage_Elem_Poll_Results);
                                holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, true, Si_Filter ? sondage.Auteur.ID : null, si_Admin));
                                    }
                                });
                            } else {
                                holder.btn_commencer.setEnabled(false);
                                holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorAccentMedDark));
                                holder.btn_commencer.setText(R.string.Sondage_Elem_Poll_Answered);
                            }
                        } else {
                            //ne peut pas consulter
                            holder.btn_commencer.setEnabled(false);
                            holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorAccentMedDark));
                            holder.btn_commencer.setText(R.string.Sondage_Elem_Poll_Done);
                        }
                    }else{
                        holder.btn_commencer.setEnabled(false);
                        holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorAccentMedDark));
                        holder.btn_commencer.setText(R.string.Sondage_Elem_Poll_Done);
                    }
            }else{//si il n'est pas connecté et que le sondage est terminé, trop tard
                //nope
                holder.btn_commencer.setEnabled(false);
                holder.btn_commencer.setTextColor(getContext().getResources().getColor(R.color.colorAccentMedDark));
                holder.btn_commencer.setText(R.string.Sondage_Elem_Poll_Done);
            }
        }
        if(sondage.Chemin_Image.equals("N")){
            holder.Image.setVisibility(View.GONE);
        }else{
            holder.Loading.setVisibility(View.VISIBLE);
                if (BitmapMap.get(sondage.ID) != null) {
                    holder.Image.setImageBitmap(BitmapMap.get(sondage.ID));
                    holder.Loading.setVisibility(View.GONE);
                    holder.Image.setVisibility(View.VISIBLE);
                } else {
                    StorageReference SondageImgRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.SONDAGES_IMAGES_THUMBNAILS).child(sondage.ID+".jpg");
                    SondageImgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap Image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            BitmapMap.put(sondage.ID, Image);
                            holder.Image.setImageBitmap(Image);
                            holder.Loading.setVisibility(View.GONE);
                            holder.Image.setVisibility(View.VISIBLE);
                        }
                    });
                }
        }
        holder.Titre.setText(sondage.Titre);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy", c.getResources().getConfiguration().locale);
        holder.DateDebut.setText(String.format(c.getString(R.string.Sondage_Elem_Date_Publie), format.format(sondage.date_public)));
        holder.DateFin.setText(String.format(c.getString(R.string.Sondage_Elem_Date_End), format.format(sondage.date_echeance)));

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
                if(sondage.Publied){
                    holder.btn_modifier.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(c, "Le sondage est publié, impossible de modifier pour ne pas fausser les résultats", Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    holder.btn_modifier.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((AdaptListener) c).changePage(CreerSondageFragment.newInstance(true, sondage));
                        }
                    });
                }
                if(sondage.Publied) {
                    holder.btn_Statistiques.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, true, Si_Filter ? sondage.Auteur.ID : null, si_Admin));
                        }
                    });
                }else{
                    holder.btn_Statistiques.setImageDrawable(c.getResources().getDrawable(R.drawable.ic_publish));
                    holder.BtnStatText.setText(R.string.Sondage_Elem_BtnPublish_Desc);
                    holder.btn_Statistiques.setContentDescription(c.getString(R.string.Sondage_Elem_BtnPublish_Desc));
                    holder.btn_Statistiques.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(c)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Publier le sondage")
                                    .setMessage("Voulez-vous rendre le sondage public? Sachez que vous ne pourrez plus le modifier si il est public.")
                                    .setPositiveButton("Oui", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME).child(sondage.ID).child(FireBaseInteraction.Sondage_Keys.PUBLIED).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        PublishPoll(sondage)
                                                                .addOnCompleteListener(new OnCompleteListener<String>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<String> task) {
                                                                        if (!task.isSuccessful()) {
                                                                            Exception e = task.getException();
                                                                            if (e instanceof FirebaseFunctionsException) {
                                                                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                                                                FirebaseFunctionsException.Code code = ffe.getCode();
                                                                                Object details = ffe.getDetails();
                                                                            }

                                                                            // ...
                                                                        }

                                                                        // ...
                                                                    }
                                                                });
                                                        Toast.makeText(c, "Le sondage a été publié", Toast.LENGTH_LONG).show();
                                                        holder.btn_Statistiques.setImageDrawable(c.getResources().getDrawable(R.drawable.ic_stats));
                                                        holder.BtnStatText.setText(R.string.Sondage_Elem_BtnStats_Desc);
                                                        holder.btn_Statistiques.setContentDescription(c.getString(R.string.Sondage_Elem_BtnStats_Desc));
                                                        holder.btn_Statistiques.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                ((AdaptListener) c).changePage(AnswerSondageFragment.newInstance(sondage, true, Si_Filter ? sondage.Auteur.ID : null, si_Admin));
                                                            }
                                                        });
                                                        sondage.Publied = true;
                                                        holder.btn_modifier.setEnabled(false);
                                                        holder.BtnEditText.setText(R.string.Sondage_Elem_BtnEdit_Desc_Disabled);
                                                        holder.btn_modifier.setContentDescription(c.getString(R.string.Sondage_Elem_BtnEdit_Desc_Disabled));
                                                    }
                                                }
                                            });
                                        }

                                    })
                                    .setNegativeButton("Non", null)
                                    .show();
                        }
                    });
                }
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
    private Task<String> PublishPoll(Sondage S) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("AuteurRef", S.AuteurRef);
        data.put("UserName", S.Auteur.Username);
        data.put("ID", S.ID);

        return mFunctions
                .getHttpsCallable("publishPoll")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
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
        TextView BtnEditText;
        TextView BtnStatText;
        ImageView Image;
        ProgressBar Loading;
        public ViewHolder(View view){
            AuteurNom = view.findViewById(R.id.sondage_elem_nom_auteur);
            btn_plainte = view.findViewById(R.id.sondage_elem_plainte_btn);
            zone_Admin = view.findViewById(R.id.sondage_elem_zone_admin);
            btn_commencer = view.findViewById(R.id.sondage_elem_start_btn);
            Loading = view.findViewById(R.id.sondage_elem_progress);
            btnVote = view.findViewById(R.id.btn_vote_layout);
            BtnEditText = view.findViewById(R.id.sondage_elem_edit_text);
            BtnStatText = view.findViewById(R.id.sondage_elem_stats_text);
            btn_modifier = view.findViewById(R.id.sondage_elem_edit_btn);
            btn_Statistiques = view.findViewById(R.id.sondage_elem_stats_btn);
            btn_supprimer = view.findViewById(R.id.sondage_elem_delete_btn);

            Titre = view.findViewById(R.id.sondage_elem_titre);
            DateDebut = view.findViewById(R.id.sondage_elem_date_debut);
            DateFin = view.findViewById(R.id.sondage_elem_date_fin);
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
                    String ThumbPath;
                    if(q.Type_Question == Question.TYPE_IMAGE){
                        ThumbPath = FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES_THUMBNAILS;
                    }else{
                        ThumbPath = FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS_THUMBNAILS;
                    }
                    FirebaseStorage.getInstance().getReference().child(ThumbPath).child(o.ID+".jpg").delete();
                    StorageReference OptionMediaRef = FirebaseStorage.getInstance().getReference().child(o.Chemin_Media);
                    OptionMediaRef.delete();
                }
            }
            QuestionRef.removeValue();
        }
        if(!s.Chemin_Image.equals("N")){
            SondageMediaRef.child(s.Chemin_Image);
            SondageMediaRef.delete();
            FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.SONDAGES_IMAGES_THUMBNAILS).child(s.ID+".jpg").delete();
        }
        SondageRef.removeValue();
        progressDialog.setTitle("Sondage supprimé.");
        progressDialog.dismiss();
        sondageAdapter.remove(s);
        sondageAdapter.notifyDataSetChanged();

    }
    public interface AdaptListener{
        void changePage(Fragment fragment);
        FragmentManager getFragmentManagerQ();
        FirebaseUser getUser();
        Profil getUtilisateur_Connecte();
    }
}


package personnal.askinquery;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

/**
 * Created by Henrick on 2018-09-20.
 */

public class SondageAdapter  extends ArrayAdapter<Sondage>{
    private boolean si_Admin;
    private static Context c;
    private LayoutInflater mInflater;
    private static AdaptListener adaptListener;
    public SondageAdapter(Context context, ArrayList<Sondage> sondages, boolean Si_Admin){
        super(context, 0, sondages);
        si_Admin = Si_Admin;
        c = context;
        mInflater = LayoutInflater.from(context);
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

        DatabaseReference AuteurRef = FirebaseDatabase.getInstance().getReference().child("Profils").child(sondage.AuteurRef);

        sondage.ConvertDates();
        holder.btn_plainte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if(si_Admin == false){
            holder.zone_Admin.setVisibility(View.GONE);
            holder.btn_commencer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }else{
            holder.btnVote.setVisibility(View.GONE);
            holder.btn_plainte.setVisibility(View.GONE);
            holder.AuteurNom.setVisibility(View.GONE);
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
                        //((AdaptListener)c).changePage();
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
        new android.support.v7.app.AlertDialog.Builder(c)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Supprimer le commentaire")
                .setMessage("Voulez-vous vraiment supprimer votre commentaire?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(c instanceof AdaptListener) {
                            ((AdaptListener)c).SupprimerSondage(s);
                        }
                    }

                })
                .setNegativeButton("Non", null)
                .show();
    }
    public interface AdaptListener{
        void changePage(Fragment fragment);
        void LoadImage(String Chemin);
        void SupprimerSondage(Sondage sondage);
    }
}


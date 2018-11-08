package personnal.askinquery;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Sondage implements Serializable {
    String ID; //vrai id
    public long Date_Public;
    public long Date_Echeance;
    public String AuteurRef;
    public String Titre;
    Profil Auteur;
    public boolean Compil_Public;
    public String Chemin_Image;
    ArrayList<Question> Questions;
    Date date_echeance;
    Date date_public;
    Uri Image;
    public Sondage(){

    }
    public Sondage(String AuteurRef, long Date_Public, long Date_Echeance, boolean Compil_Public, String Titre, String Chemin_Image){

        this.Date_Public = Date_Public;
        this.Date_Echeance = Date_Echeance;
        this.Titre = Titre;
        this.AuteurRef = AuteurRef;
        this.Compil_Public = Compil_Public;
        this.Chemin_Image = Chemin_Image;



    }
    public void QueryGetAuteur(){
        ValueEventListener AuteurListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profil A = dataSnapshot.getValue(Profil.class);
                A.ID = dataSnapshot.getKey();
                Auteur = A;
                Auteur.notifyAll();
                Log.w("Test",Auteur.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        DatabaseReference ProfilRef = FirebaseDatabase.getInstance().getReference().child("Profils").child(AuteurRef);
        ProfilRef.addListenerForSingleValueEvent(AuteurListener);
    }
    public void ConvertDates(){
        date_echeance = new Date(Date_Echeance);
        date_public = new Date(Date_Public);
    }
    public Map<String, Object> Map(){
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put(FireBaseInteraction.Sondage_Keys.AUTEUR_REF, AuteurRef);
        newMap.put(FireBaseInteraction.Sondage_Keys.DATE_ECHEANCE, Date_Echeance);
        newMap.put(FireBaseInteraction.Sondage_Keys.DATE_PUBLIC, Date_Public);
        newMap.put(FireBaseInteraction.Sondage_Keys.TITRE, Titre);
        newMap.put(FireBaseInteraction.Sondage_Keys.CHEMIN_IMAGE, Chemin_Image);
        newMap.put(FireBaseInteraction.Sondage_Keys.COMPIL_PUBLIC, Compil_Public);
        return newMap;
    }
}

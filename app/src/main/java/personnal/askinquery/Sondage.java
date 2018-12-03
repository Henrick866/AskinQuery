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

public class Sondage implements Serializable{
    String ID; //vrai id
    public long Date_Public;
    public long Date_Echeance;
    public long Date_Created;
    public String AuteurRef;
    public String Titre;
    Profil Auteur;
    public boolean Compil_Public;
    public boolean Publied;
    public String Chemin_Image;
    ArrayList<Question> Questions;
    Date date_echeance;
    Date date_public;
    Uri Image;
    Sondage(){

    }
    public Sondage(String AuteurRef, long Date_Public, long Date_Echeance, long Date_Created, boolean Compil_Public, String Titre, String Chemin_Image, boolean Publied){

        this.Date_Public = Date_Public;
        this.Date_Echeance = Date_Echeance;
        this.Titre = Titre;
        this.AuteurRef = AuteurRef;
        this.Date_Created = Date_Created;
        this.Compil_Public = Compil_Public;
        this.Chemin_Image = Chemin_Image;
        this.Publied = Publied;



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
        newMap.put(FireBaseInteraction.Sondage_Keys.DATE_CREATED, Date_Created);
        newMap.put(FireBaseInteraction.Sondage_Keys.TITRE, Titre);
        newMap.put(FireBaseInteraction.Sondage_Keys.CHEMIN_IMAGE, Chemin_Image);
        newMap.put(FireBaseInteraction.Sondage_Keys.COMPIL_PUBLIC, Compil_Public);
        newMap.put(FireBaseInteraction.Sondage_Keys.PUBLIED, Publied);
        return newMap;
    }
}

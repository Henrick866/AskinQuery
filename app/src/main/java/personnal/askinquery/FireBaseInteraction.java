package personnal.askinquery;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FireBaseInteraction {
    Context context;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    ArrayList<Sondage> Sondage_list;
    Sondage SondageSolo;
    public FireBaseInteraction(){
    }
    public static class Storage_Paths{
        final static String PUBLICATION_VIDEOS = "Media/Publications/Videos/";
        final static String PUBLICATION_IMAGES = "Media/Publications/Images/";
        final static String SONDAGES_IMAGES = "Media/Sondages/Thumbnails/";
        final static String OPTIONS_IMAGES = "Media/Sondages/Options/Images/";
        final static String OPTIONS_VIDEOS = "Media/Sondages/Options/Videos/";
        final static String OPTIONS_VIDEOS_THUMBNAILS = "Media/Sondages/Options/Videos/Thumbnails/";
        final static String PROFILS_AVATARS = "Media/Profils/";
    }
    public static class Sondage_Keys{
        final static String STRUCT_NAME = "Sondages";
        final static String TITRE = "Titre";
        final static String DATE_PUBLIC = "Date_Public";
        final static String DATE_ECHEANCE = "Date_Echeance";
        final static String COMPIL_PUBLIC = "Compil_Public";
        final static String CHEMIN_IMAGE = "Chemin_Image";
        final static String AUTEUR_REF = "AuteurRef";
    }
    public static class Question_Keys{
        final static String STRUCT_NAME = "Questions";
        final static String SONDAGE_REF = "SondageRef";
        final static String TEXTE_QUESTION = "Texte_Question";
        final static String TYPE_QUESTION = "Type_Question";
        final static String OPTIONS = "Options";
    }
    public static class Option_Keys{
        final static String TEXTE = "Texte";
        final static String CHEMIN_MEDIA = "Chemin_Media";
        final static String SCORE = "Score";
    }
    public static class Profil_Keys{
        final static String STRUCT_NAME = "Profils";
        final static String USERNAME = "Username";
        final static String MDPCRYPTED = "MDPcrypted";
        final static String SALT = "salt";
        final static String COURRIEL = "Courriel";
        final static String AVATAR = "Avatar";
        final static String AUTEURS_SUIVIS = "Auteurs_Suivis";
        final static String ID_NOMENCLATURE = "ID_Nomenclature";
    }
}

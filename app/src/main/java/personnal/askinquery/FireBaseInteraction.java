package personnal.askinquery;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FireBaseInteraction {
    Context context;
    FirebaseDatabase database;
    public FireBaseInteraction(){
    }
    public static class Storage_Paths{
        final static String SONDAGES_IMAGES = "Media/Sondages/Thumbnails/";
        final static String SONDAGES_IMAGES_THUMBNAILS = "Media/Sondages/Thumbnails/Small/";
        final static String OPTIONS_IMAGES = "Media/Sondages/Options/Images/";
        final static String OPTIONS_IMAGES_THUMBNAILS = "Media/Sondages/Options/Images/Thumbnails/";
        final static String OPTIONS_VIDEOS = "Media/Sondages/Options/Videos/";
        final static String OPTIONS_VIDEOS_THUMBNAILS = "Media/Sondages/Options/Videos/Thumbnails/";
        final static String PUBLICATION_VIDEOS_THUMBNAILS = "Media/Publications/Videos/Thumbnails/";
        final static String PUBLICATION_VIDEOS = "Media/Publications/Videos/";
        final static String PUBLICATION_IMAGES = "Media/Publications/Images/";
        final static String PUBLICATION_IMAGES_THUMBNAILS = "Media/Publications/Images/Thumbnails/";
        final static String PROFILS_AVATARS = "Media/Profils/";
    }

    static class Sondage_Keys{
        final static String STRUCT_NAME = "Sondages";
        final static String TITRE = "Titre";
        final static String DATE_PUBLIC = "Date_Public";
        final static String DATE_ECHEANCE = "Date_Echeance";
        final static String DATE_CREATED = "Date_Created";
        final static String COMPIL_PUBLIC = "Compil_Public";
        final static String CHEMIN_IMAGE = "Chemin_Image";
        final static String AUTEUR_REF = "AuteurRef";
        final static String PUBLIED = "Publied";
    }
    static class Question_Keys{
        final static String STRUCT_NAME = "Questions";
        final static String SONDAGE_REF = "SondageRef";
        final static String TEXTE_QUESTION = "Texte_Question";
        final static String ORDRE = "Numero";
        final static String TYPE_QUESTION = "Type_Question";
        final static String OPTIONS = "Options";
    }
    static class Option_Keys{
        final static String TEXTE = "Texte";
        final static String CHEMIN_MEDIA = "Chemin_Media";
        final static String SCORE = "Score";
    }
    public static class Profil_Keys{
        final static String STRUCT_NAME = "Profils";
        final static String USERNAME = "Username";
        final static String COURRIEL = "Courriel";
        final static String AVATAR = "Avatar";
        final static String SONDAGES = "Sondages";
        final static String AUTEURS_SUIVIS = "Auteurs_Suivis";
    }
    static class Publications_Keys{
        final static String STRUCT_NAME = "Publications";
        final static String TITRE = "Titre";
        final static String TEXTE = "Texte";
        final static String AUTEUR = "AuteurRef";
        final static String MEDIA = "Media";
        final static String SONDAGE = "SondageRef";
        final static String TYPE_MEDIA = "Type";
        final static String DATE_PUBLIC = "date_public";
    }
    static class Plainte_Keys{
        final static String STRUCT_NAME = "Plaintes";
        final static String RAISON = "Raison";
        final static String CIBLE = "Cible";
        final static String TYPE = "Type";
        final static String DATE = "Date";
    }
}

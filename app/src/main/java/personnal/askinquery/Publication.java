package personnal.askinquery;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Publication implements Serializable {
    String ID;
    public String Media;
    String AuteurRef;
    String Titre;
    Profil Auteur;
    int Type; //0:txt;1:img;2:vid;3:sondage;
    String Texte;
    String SondageRef;
    static final int TYPE_TEXTE = 0;
    static final int TYPE_IMAGE = 1;
    static final int TYPE_VIDEO = 2;
    static final int TYPE_SONDAGE = 3;
    long date_public;
    Date Date_Public;
    public Publication(){

    }
    public Publication(String Media, String Titre,String AuteurRef, int Type, String Texte, String SondageRef, long date_public){
        this.Media = Media;
        this.AuteurRef = AuteurRef;
        this.Titre = Titre;
        this.Type = Type;
        this.Texte = Texte;
        this.SondageRef = SondageRef;
        this.date_public = date_public;
    }
    Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put(FireBaseInteraction.Publications_Keys.AUTEUR, this.AuteurRef);
        map.put(FireBaseInteraction.Publications_Keys.MEDIA, this.Media);
        map.put(FireBaseInteraction.Publications_Keys.SONDAGE, this.SondageRef);
        map.put(FireBaseInteraction.Publications_Keys.TEXTE, this.Texte);
        map.put(FireBaseInteraction.Publications_Keys.TITRE, this.Titre);
        map.put(FireBaseInteraction.Publications_Keys.TYPE_MEDIA, this.Type);
        map.put(FireBaseInteraction.Publications_Keys.DATE_PUBLIC, this.date_public);
        return map;
    }
}

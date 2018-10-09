package personnal.askinquery;

import android.app.DownloadManager;
import android.net.Uri;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Option implements Serializable

    {
        String ID;
        public int ID_Nomenclature;
        Question Question_parent;
        public String Texte;
        public String Chemin_Media;
        String Chemin_Video, Chemin_Image;
        Uri Video, Image;
        public int Score;
        boolean notOnServer, toBeDeleted = false, firstopened;
    public Option(){

        }
    public Option(String Texte, String Chemin_Media, int Score, int ID_Nomenclature){
            this.ID_Nomenclature = ID_Nomenclature;
            this.Texte = Texte;
            switch(this.Question_parent.Type_Question){
                case Question.TYPE_TEXTE:
                    this.Chemin_Video = "N";
                    this.Chemin_Image = "N";
                    break;
                case Question.TYPE_IMAGE :
                    this.Chemin_Video = "N";
                    this.Chemin_Image = Chemin_Media;
                    break;
                case Question.TYPE_VIDEO :
                    this.Chemin_Video = Chemin_Media;
                    this.Chemin_Image = "N";
                    break;
            }
            this.Score = Score;
        }
        public Map<String, Object> Map(){
            HashMap<String, Object> newMap = new HashMap<>();
            newMap.put(FireBaseInteraction.Option_Keys.CHEMIN_MEDIA, Chemin_Media);
            newMap.put(FireBaseInteraction.Option_Keys.SCORE, Score);
            newMap.put(FireBaseInteraction.Option_Keys.TEXTE, Texte);
            return newMap;
        }
}

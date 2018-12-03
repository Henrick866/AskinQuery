package personnal.askinquery;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Option implements Serializable

    {
        String ID;
        Question Question_parent;
        public String Texte;
        public String Chemin_Media;
        String Chemin_Video, Chemin_Image;
        String UriVideo, UriImage;
        transient Bitmap ImagePreload, ImageFull;
        public int Score;
        boolean notOnServer, toBeDeleted = false, DataChanged = false;
    public Option(){

        }
    public Option(String Texte, String Chemin_Media, int Score){
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
        public int getTotalVotes(){
            int Total = 0;
            for(Option o : Question_parent.Options){
                Total += o.Score;
            }
            return Total;
        }
}

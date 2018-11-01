package personnal.askinquery;

import android.app.DownloadManager;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 1. upload image
 *  1.a : image converti en bitmap local en attendant d'être uploadé
 *  1.b Bitmap uploadé quand l'utilisateur valide
 * 2. download image
 *  2.a : télécharge les images sur les bitmap sur les objets
 *  2.b : affiche ces images.
 *
 * Video
 *  1. upload video
 *      1.a lorsque la vidéo est choisie, un thumbnail est créée et enregistré dans l'objet
 *      1.b lorsque la vidéo est uploadé, on envoi la vidéo ET le thumbnail.
 *  2. download video
 *      2.a télécharge les thumbnails en batch
 *      2.b affiche ces images en liste
 *      2.c au click d'un image, un popup apparait pour afficher la vidéo, question de sortir du listview problématique.
 *
 *  Conditions show:
 *     if(type) img -> if(notonserver)  -> if(bitmap==null)  -> imgview empty
 *                                                    else   -> imgview bitmap
 *                               else   -> imgview loadImage()
 *              vid -> if(notonserver)  -> if(bitmap==null)  -> imgview empty, imgview.onclick = none
 *                               else   -> imgview loadImage() imgview.onclick = new dialog(LoadVideo(o.urlvideo))
 *                                video = id.mp4
 *                                image = id.jpg
 */

public class Option implements Serializable

    {
        String ID;
        public int ID_Nomenclature;
        Question Question_parent;
        public String Texte;
        public String Chemin_Media;
        String Chemin_Video, Chemin_Image;
        String UriVideo, UriImage;
        transient Bitmap ImagePreload;
        public int Score;
        boolean notOnServer, toBeDeleted = false, MediaIsLoading = false, DataChanged = false;
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
        public void SetChemins(){
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
            Chemin_Media = "N";
        }
        public Map<String, Object> Map(){
            HashMap<String, Object> newMap = new HashMap<>();
            newMap.put(FireBaseInteraction.Option_Keys.CHEMIN_MEDIA, Chemin_Media);
            newMap.put(FireBaseInteraction.Option_Keys.SCORE, Score);
            newMap.put(FireBaseInteraction.Option_Keys.TEXTE, Texte);
            return newMap;
        }
}

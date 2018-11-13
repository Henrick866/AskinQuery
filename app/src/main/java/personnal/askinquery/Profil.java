package personnal.askinquery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Profil implements Serializable {
    String ID;
    public String Username;
    public String Courriel;
    public String Avatar;
    //public Map<String, Object> Auteurs_Suivis_Ref = new HashMap<>(); //liens latent attendant d'être activés
    HashMap<String, String> Auteurs_Suivis;
    HashMap<String, Object> Sondages_Faits;
    //ArrayList<String> Auteurs_Suivis;
    public Profil(){

    }
    public Profil(String Username, String Courriel, String Avatar){
        this.Username = Username;
        this.Courriel = Courriel;
        this.Avatar = Avatar;
        this.Auteurs_Suivis = new HashMap<String, String>();
    }
    public HashMap<String, Object> toMap(){
        HashMap<String, Object> Map = new HashMap<>();
        Map.put(FireBaseInteraction.Profil_Keys.USERNAME, this.Username);
        Map.put(FireBaseInteraction.Profil_Keys.COURRIEL, this.Courriel);
        Map.put(FireBaseInteraction.Profil_Keys.AVATAR, this.Avatar);
        HashMap<String, String> MapFollowed = new HashMap<>();
        Iterator it = Auteurs_Suivis.entrySet().iterator();
        while(it.hasNext()){
            HashMap.Entry pair = (HashMap.Entry)it.next();
            MapFollowed.put((String)pair.getKey(), (String)pair.getValue());
        }
        Map.put(FireBaseInteraction.Profil_Keys.AUTEURS_SUIVIS, MapFollowed);
        return Map;
    }
}

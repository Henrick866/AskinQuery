package personnal.askinquery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Profil implements Serializable {
    String ID;
    public String Username;
    public String Courriel;
    public String Avatar;
    //public Map<String, Object> Auteurs_Suivis_Ref = new HashMap<>(); //liens latent attendant d'être activés
    ArrayList<String> Auteurs_Suivis;
    public Profil(){

    }
    public Profil(String Username, String Courriel, String Avatar){
        this.Username = Username;
        this.Courriel = Courriel;
        this.Avatar = Avatar;
        this.Auteurs_Suivis = new ArrayList<>();
    }
    public HashMap<String, Object> toMap(){
        HashMap<String, Object> Map = new HashMap<>();
        Map.put(FireBaseInteraction.Profil_Keys.USERNAME, this.Username);
        Map.put(FireBaseInteraction.Profil_Keys.COURRIEL, this.Courriel);
        Map.put(FireBaseInteraction.Profil_Keys.AVATAR, this.Avatar);
        HashMap<String, Boolean> MapFollowed = new HashMap<>();
        for(String a : Auteurs_Suivis){
            MapFollowed.put(a, true);
        }
        Map.put(FireBaseInteraction.Profil_Keys.AUTEURS_SUIVIS, MapFollowed);
        return Map;
    }
}

package personnal.askinquery;

import java.io.Serializable;
import java.util.ArrayList;

public class Profil implements Serializable {
    String ID;
    public int ID_Nomenclature;
    public String Username;
    public String MDPcrypted;
    public String salt;
    public String Courriel;
    public String Avatar;
    //public Map<String, Object> Auteurs_Suivis_Ref = new HashMap<>(); //liens latent attendant d'être activés
    ArrayList<Profil> Auteurs_Suivis;
    public Profil(){

    }
    public Profil(String Username, String MDPcrypted, String salt, String Avatar, String Courriel, /*Map<String, Object> Auteurs_Suivis_Ref,*/ int ID_Nomenclature){

        this.ID_Nomenclature = ID_Nomenclature;
        this.Username = Username;
        this.MDPcrypted = MDPcrypted;
        this.salt = salt;
        this.Courriel = Courriel;
        this.Avatar = Avatar;
        //this.Auteurs_Suivis_Ref = Auteurs_Suivis_Ref;
    }
}

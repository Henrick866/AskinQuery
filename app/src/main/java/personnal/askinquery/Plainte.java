package personnal.askinquery;

import java.util.HashMap;
import java.util.Map;

public class Plainte {
    String ID;
    public String Raison;
    public String CibleID;
    public String TypeCible; //au cas où 2 id se ressemblent;
    public long Date;
    static final String TYPE_SONDAGE = "SONDAGE";
    static final String TYPE_PUBLICATION = "PUBLICATION";
    static final String TYPE_PROFIL = "PROFIL";
    public Plainte(){

    }
    public Plainte(String Raison, String CibleID, String TypeCible, long Date){
        this.Raison = Raison;
        this.CibleID = CibleID;
        this.TypeCible = TypeCible;
        this.Date = Date;
    }
    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put(FireBaseInteraction.Plainte_Keys.RAISON, this.Raison);
        map.put(FireBaseInteraction.Plainte_Keys.CIBLE, this.CibleID);
        map.put(FireBaseInteraction.Plainte_Keys.TYPE, this.TypeCible);
        map.put(FireBaseInteraction.Plainte_Keys.DATE, this.Date);
        return map;
    }
}

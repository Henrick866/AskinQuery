package personnal.askinquery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Question implements Serializable {
    String ID;
    Sondage Sondage_parent;
    public int Type_Question;
    public int Numero;
    public final static int TYPE_TEXTE = 1;
    public final static int TYPE_IMAGE = 2;
    public final static int TYPE_VIDEO = 3;
    public String Texte_Question;
    public String SondageRef;
    boolean notOnServer, toBeDeleted = false;

    ArrayList<Option> Options;
    public Question(){


    }
    public Question(String Texte_Question, int Type_Question, int Numero, String SondageRef){
        this.Numero = Numero;
        this.Texte_Question = Texte_Question;
        this.Type_Question = Type_Question;
        this.SondageRef = SondageRef;
    }
    public Question(String Texte_Question, long Type_Question, long Numero, String SondageRef){
        this.Numero = (int)Numero;
        this.Texte_Question = Texte_Question;
        this.Type_Question = (int)Type_Question;
        this.SondageRef = SondageRef;
    }
    void SetOptions(ArrayList<Option> o){
        Options = o;
    }
    public int getTotal(){
        int Total = 0;
        for (Option o: Options) {
            Total += o.Score;
        }
        return Total;
    }
    public Map<String, Object> Map(){
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put(FireBaseInteraction.Question_Keys.SONDAGE_REF, SondageRef);
        newMap.put(FireBaseInteraction.Question_Keys.TEXTE_QUESTION, Texte_Question);
        newMap.put(FireBaseInteraction.Question_Keys.TYPE_QUESTION, Type_Question);
        newMap.put(FireBaseInteraction.Question_Keys.ORDRE, Numero);
        newMap.put(FireBaseInteraction.Question_Keys.OPTIONS, "");
        return newMap;
    }
}

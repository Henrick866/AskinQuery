package personnal.askinquery;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Henrick on 2018-09-21.
 */

public class CreerQuestionAdapter extends ArrayAdapter<Question> implements CreerOptionAdapter.OptionsToQuestion, Serializable {
    static public ArrayList<Question> QuestionData;
    private static CreerQuestionAdapter.AdaptListener adaptListener;
    transient CreerSondageFragment creerSondageFragment;

    static Context c;
    static TabHost Host;
    int Position;

    public CreerQuestionAdapter(Context context, ArrayList<Question> questions, CreerSondageFragment creerSondageFragment){
        super(context, 0, questions);
        c = context;
        this.creerSondageFragment = creerSondageFragment;
        adaptListener = (AdaptListener) c;
        QuestionData = questions; //liste des questions.
    }
    public View getView(int position, View convertView, final ViewGroup parent){
        Question question = getItem(position);
        Position = position;
        final int POSITION = position;
        ArrayList<Option> Liste_Options;
        //Log.e("POSITION Q", String.valueOf(position));
        if(QuestionData.get(position) == null){
            QuestionData.add(question);
        }
        View view = convertView;
        final CreerQuestionAdapter.ViewHolder viewHolder;
        if(view == null){

            view = LayoutInflater.from(getContext()).inflate(R.layout.question_edit_element, parent, false);
            viewHolder = new CreerQuestionAdapter.ViewHolder(view, position);
            viewHolder.question = QuestionData.get(position);
            viewHolder.liste_option = QuestionData.get(position).Options;
            view.setTag(viewHolder);
        }else{

            viewHolder = (CreerQuestionAdapter.ViewHolder)view.getTag();
            question = viewHolder.question;
        }
        viewHolder.NumQuestion.setText("Question #"+(position+1));

        List<String> spinnerArray =  new ArrayList<>();
        spinnerArray.add("(Non sélectionné)");
        spinnerArray.add("Texte");
        spinnerArray.add("Image");
        spinnerArray.add("Vidéo");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.TypeSelect.setAdapter(adapter);
        if(question.Type_Question != 0) {
            viewHolder.TypeSelect.setSelection(QuestionData.get(position).Type_Question);
            viewHolder.btnDropDown.setEnabled(true);
        }else{
            viewHolder.btnDropDown.setEnabled(false);
            viewHolder.TypeError.setText("Vous devez choisir un type de question");
        }
        if(QuestionData.size() < 1){
            creerSondageFragment.QuestionsError.setText("Vous devez poser au moins une question.");
            creerSondageFragment.QuestionsError.setVisibility(View.VISIBLE);
        }
        final CreerQuestionAdapter questionAdapter = this;
        final CreerQuestionAdapter.DialogFunctions dialogFunctions = new CreerQuestionAdapter.DialogFunctions();
        if(QuestionData.get(position).Options.isEmpty()){
            viewHolder.OptionsError.setText("Vous devez ajouter au moins deux choix de réponses.");
            viewHolder.OptionsError.setVisibility(View.VISIBLE);
        }
        //Question Q = QuestionData.get(position);

        viewHolder.btnDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = adaptListener.getFragmentManagerQ();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment prev = fm.findFragmentByTag("fragment_creer_option_dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                CreerOptionDialog creerOptionDialog = CreerOptionDialog.newInstance(QuestionData.get(POSITION), POSITION, dialogFunctions);
                creerOptionDialog.show(ft, "fragment_creer_option_dialog");

            }
        });
        viewHolder.btnSupp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!QuestionData.get(POSITION).notOnServer) {
                    QuestionData.get(POSITION).toBeDeleted = true;
                }
                questionAdapter.remove(questionAdapter.getItem(Position));
                //QuestionData.remove(Position);
                questionAdapter.notifyDataSetChanged();


                /*Question q = QuestionData.get(Position);
                questionToSondage.DeleteQuestion(q);*/
            }
        });
        TextWatcher questionTxtWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newTexte = s.toString();
                if(newTexte.isEmpty()){
                    viewHolder.TexteError.setText("Vous devez poser une question");
                    viewHolder.TexteError.setVisibility(View.VISIBLE);
                }else {
                    if(QuestionData.get(Position).Texte_Question == null) {//si nouv
                        QuestionData.get(Position).Texte_Question = newTexte;
                    }else if (!QuestionData.get(Position).Texte_Question.equals(newTexte)) {//si change et diff
                        QuestionData.get(Position).Texte_Question = newTexte;
                        viewHolder.TexteError.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };
        TextWatcher oldWatcher = (TextWatcher)viewHolder.TexteQuestion.getTag();
        if(oldWatcher != null){
            viewHolder.TexteQuestion.removeTextChangedListener(oldWatcher);
        }
        if(QuestionData.get(POSITION).Texte_Question != null) {
            if (!QuestionData.get(POSITION).Texte_Question.isEmpty()) {
                viewHolder.TexteQuestion.setText(QuestionData.get(POSITION).Texte_Question);
            } else {
                viewHolder.TexteQuestion.setText("");
                viewHolder.TexteError.setText("Vous devez poser une question");
                viewHolder.TexteError.setVisibility(View.VISIBLE);
            }
        }else{
            viewHolder.TexteQuestion.setText("");
            viewHolder.TexteError.setText("Vous devez poser une question");
            viewHolder.TexteError.setVisibility(View.VISIBLE);
        }
        viewHolder.TexteQuestion.setTag(questionTxtWatcher);
        viewHolder.TexteQuestion.addTextChangedListener(questionTxtWatcher);

        viewHolder.TypeSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                viewHolder.btnDropDown.setEnabled(true);
                if(pos == Question.TYPE_TEXTE){

                    QuestionData.get(POSITION).Type_Question = Question.TYPE_TEXTE;
                    viewHolder.TypeError.setVisibility(View.INVISIBLE);
                }else if(pos == Question.TYPE_VIDEO){
                    QuestionData.get(POSITION).Type_Question = Question.TYPE_VIDEO;
                    viewHolder.TypeError.setVisibility(View.INVISIBLE);
                }else if(pos == Question.TYPE_IMAGE){
                    QuestionData.get(POSITION).Type_Question = Question.TYPE_IMAGE;
                    viewHolder.TypeError.setVisibility(View.INVISIBLE);
                }else if(pos == 0){
                    viewHolder.btnDropDown.setEnabled(false);
                    QuestionData.get(POSITION).Type_Question = 0;
                    viewHolder.TypeError.setText("Vous devez choisir un type de question");
                    viewHolder.TypeError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }
    class DialogFunctions implements CreerOptionDialog.OptionDialogListener, Serializable{
        public DialogFunctions(){
        }
        public String onFinishedOptionDialog(ArrayList<Option> options, int Position){
            QuestionData.get(Position).Options = options;
            //creerOptionDialog.dismiss();
            Toast.makeText(getContext(), "La liste des options de la question #"+(Position+1)+" a été mis à jour", Toast.LENGTH_LONG).show();
            return "done";
        }

    }
    public void updateOptions(int pos, ArrayList<Option> options){
        QuestionData.get(pos).Options = options;
    }
    public ArrayList<Question> getQuestions(){
        return QuestionData;
    }
    static class ViewHolder implements Serializable{
        Spinner TypeSelect;
        EditText TexteQuestion;
        TextView NumQuestion;
        ImageButton btnSupp;
        TextView TexteError;
        TextView TypeError;
        TextView OptionsError;
        Button btnDropDown;
        boolean Deployed;
        ArrayList<Option> liste_option;
        Question question;
        int nbNewOption;
        int position;
        public ViewHolder(View view, final int p){
            Deployed = false;
            position = p;
            nbNewOption = 0;
            TexteError = (TextView)view.findViewById(R.id.question_edit_texte_error);
            TypeError = (TextView)view.findViewById(R.id.question_edit_type_error);
            OptionsError =(TextView)view.findViewById(R.id.question_edit_options_error);
            TypeSelect = (Spinner)view.findViewById(R.id.sondage_edit_question_type);
            TexteQuestion = (EditText)view.findViewById(R.id.sondage_edit_question_titre);
            NumQuestion = (TextView)view.findViewById(R.id.sondage_edit_num_quest);
            btnSupp = (ImageButton)view.findViewById(R.id.sondage_edit_question_delete);
            btnDropDown = (Button)view.findViewById(R.id.question_edit_dropdown_button);


        }
    }
    public String onFinishedOptionDialog(ArrayList<Option> Options, int Position){
        QuestionData.get(Position).Options = Options;
        //creerOptionDialog.dismiss();
        Toast.makeText(getContext(), "La liste des options de la question #"+(Position+1)+" a été mis à jour", Toast.LENGTH_LONG).show();
        return "done";
    }
    public interface AdaptListener{
        void changePage(Fragment fragment);
        FragmentManager getFragmentManagerQ();
        //void LoadImage(String Chemin);
        //void SupprimerPublication(Publication publication);
    }
}
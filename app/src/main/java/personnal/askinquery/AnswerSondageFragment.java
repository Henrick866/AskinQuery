package personnal.askinquery;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.provider.FirebaseInitProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AnswerSondageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AnswerSondageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//TODO:: TEMPS RÉEL SI L'USER EST L'AUTEUR;
public class AnswerSondageFragment extends Fragment{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

    private Sondage sondage;
    private boolean Si_Resultats, Si_Admin;
    private ArrayList<Question> liste_questions;
    private TextView TitreText, AuteurText;
    private AnswerQuestionAdapter answerQuestionAdapter;
    private ImageView ImageSondage;
    private ListView ListeQuestion;
    private Button BtnTerminer, BtnSave;
    private View Separateur;
    private OnFragmentInteractionListener mListener;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public AnswerSondageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AnswerSondageFragment.
     */
    public static AnswerSondageFragment newInstance(Sondage sondage, boolean Si_Resultats, @Nullable String filter, boolean Si_Admin) {
        AnswerSondageFragment fragment = new AnswerSondageFragment();
        Bundle args = new Bundle();

        args.putSerializable(ARG_PARAM1, sondage);
        args.putBoolean(ARG_PARAM2, Si_Resultats);
        args.putString(ARG_PARAM3, filter);
        args.putBoolean(ARG_PARAM4, Si_Admin);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sondage = (Sondage)getArguments().getSerializable(ARG_PARAM1);
            Si_Resultats = getArguments().getBoolean(ARG_PARAM2);
            //filter = getArguments().getString(ARG_PARAM3);
            Si_Admin = getArguments().getBoolean(ARG_PARAM4);
        }
        liste_questions = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        String Title;
        if(Si_Resultats){
            Title = "Sondage | Résultats";
        }else{
            Title = "Sondage | Répondre";
        }
        mListener.ChangeTitle(Title);
        View view = inflater.inflate(R.layout.fragment_answer_sondage, container, false);
        TitreText = view.findViewById(R.id.answer_sondage_title);
        TitreText.setText(sondage.Titre);
        AuteurText = view.findViewById(R.id.answer_sondage_auteur);
        if(sondage.Auteur == null){
            FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME)
                    .child(sondage.AuteurRef).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    sondage.Auteur = dataSnapshot.getValue(Profil.class);
                    AuteurText.setText(sondage.Auteur.Username);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        ImageSondage = view.findViewById(R.id.answer_sondage_image);

        if(!sondage.Chemin_Image.equals("N")) {
            StorageReference ImageRef = FirebaseStorage.getInstance().getReference().child(sondage.Chemin_Image);
            ImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                    ImageSondage.setImageBitmap(bitmap);
                }
            });
        }else{
            ImageSondage.setVisibility(View.GONE);
        }
        ListeQuestion = view.findViewById(R.id.answer_sondage_questions);






        DatabaseReference QuestionRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Question_Keys.STRUCT_NAME);
        QuestionRef.addListenerForSingleValueEvent(QuestionListener);
        BtnTerminer = view.findViewById(R.id.answer_sondage_btn_terminer);
        BtnSave = view.findViewById(R.id.answer_sondage_btn_save);
        Separateur = view.findViewById(R.id.answer_sondage_separateur);
        if(Si_Resultats){
            ((View)BtnSave.getParent()).setVisibility(View.GONE);
            Separateur.setVisibility(View.GONE);
            BtnTerminer.setText("Revenir");
            BtnTerminer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getFragmentManager().popBackStack();
                    /*if(user != null){
                        if(Si_Admin){//pour etre true, il faut acceder depuis la liste de gestion de sondages, donc il faut avoir un compte, anonyme est donc exclu
                            mListener.changePage(SondageListFragment.newInstance(true, filter), "Sondages | Gérer");
                        }else{
                            String Title;
                            if(filter == null){
                                Title = "Sondages";
                            }else{
                                Title = "Sondages | "+sondage.Auteur.Username;
                            }
                            mListener.changePage(SondageListFragment.newInstance(false, filter), Title);
                        }
                    }*/

                }
            });
        }else {
            BtnTerminer.setOnClickListener(DoneListener);
            if (user != null) {
                if (user.isAnonymous()) {
                    ((View)BtnSave.getParent()).setVisibility(View.GONE);
                    Separateur.setVisibility(View.GONE);
                } else {
                    BtnSave.setVisibility(View.VISIBLE);
                    Separateur.setVisibility(View.VISIBLE);
                    BtnSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ArrayList<Integer> ListeReponse = answerQuestionAdapter.getQuestionReponse();

                            // je gardes ce code si on fait du choix multiple
                            /*Map<String, Boolean> mapOptions = new HashMap<>();
                            for (int i = 0; i < liste_questions.size(); i++) {
                                for (int j = 0; j < liste_questions.get(i).Options.size(); j++) {
                                    if (j == ListeReponse.get(i)) {
                                        mapOptions.put(liste_questions.get(i).Options.get(j).ID, true);
                                    } else {
                                        mapOptions.put(liste_questions.get(i).Options.get(j).ID, false);
                                    }
                                }
                            }*/
                            Map<String, Integer> mapQuestions = new HashMap<>(); //(PositionQuestion, PositionOption)
                            for(int i = 0; i < ListeReponse.size(); i++){
                                mapQuestions.put(String.valueOf(i), ListeReponse.get(i));
                            }
                            FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME)
                                    .child(user.getUid()).child(FireBaseInteraction.Profil_Keys.SONDAGES).child(sondage.ID).setValue(mapQuestions);
                            Profil utilConn = mListener.getUtilisateur_Connecte();
                            utilConn.Sondages_Faits.put(sondage.ID, mapQuestions);
                            mListener.setUtilisateur_Connecte(utilConn);
                            Toast.makeText(getActivity(), "Le sondage a été enregistré pour y répondre plus tard", Toast.LENGTH_LONG).show();

                            getFragmentManager().popBackStack();
                        }
                    });
                }
            } else {
                ((View)BtnSave.getParent()).setVisibility(View.GONE);
                Separateur.setVisibility(View.GONE);
            }
        }
        return view;
    }
    private View.OnClickListener DoneListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference RefSondageResults = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Question_Keys.STRUCT_NAME);
                ArrayList<Integer> ListeReponse = answerQuestionAdapter.getQuestionReponse();
                Question q; //référence rapide;
                Option o; //référence rapise
                int ScoreFinal;
                boolean Valid = true;
                for(int i : ListeReponse){
                    if(i < 0){
                        Valid=false;
                    }
                }
                if(Valid) {
                    //todo, utiliser les transactions firebase
                    for (int i = 0; i < liste_questions.size(); i++) {
                        q = liste_questions.get(i);
                        o = q.Options.get(ListeReponse.get(i));
                        ScoreFinal = liste_questions.get(i).Options.get(ListeReponse.get(i)).Score + 1;
                        RefSondageResults.child(liste_questions.get(i).ID).child(FireBaseInteraction.Question_Keys.OPTIONS).child(o.ID).child(FireBaseInteraction.Option_Keys.SCORE).setValue(ScoreFinal);
                    }
                    if (user != null) {//indique que l'utilisateur a terminé (identique si l'utilisateur est anonyme ou non)
                        FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME)
                                .child(user.getUid()).child(FireBaseInteraction.Profil_Keys.SONDAGES).child(sondage.ID).setValue(true);
                        Profil utilConn = mListener.getUtilisateur_Connecte();
                        utilConn.Sondages_Faits.put(sondage.ID, true);
                        mListener.setUtilisateur_Connecte(utilConn);

                    } else {
                        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                user = task.getResult().getUser();
                                FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME)
                                        .child(user.getUid()).child(FireBaseInteraction.Profil_Keys.SONDAGES).child(sondage.ID).setValue(true);

                                mListener.updateMenu(user);
                            }
                        });
                    }//todo: ajouter else, etat anonyme
                    /** Anonyme
                     *  1.si l'utilisateur réponds à un sondage sans se connecter, on utilise une instance user anonyme
                     *  2. si cet instance n'existe pas on le créé
                     *  3. on créé une valeur sur la bd distante comme si c'était un profil (FirebaseInteration a déja la structure nécéssaire)
                     *  4. on y envoie {s.id, true}
                     *
                     * */
                    Toast.makeText(getActivity(), "Merci d'avoir répondu à ce sondage!", Toast.LENGTH_LONG).show();
                    getFragmentManager().popBackStack();
                }else{
                    new AlertDialog.Builder(getActivity())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Sondage non terminé")
                            .setMessage("Le sondage n'est pas terminé, veuillez le compléter pour pouvoir le soumettre.")
                            .setPositiveButton("Ok", null)
                            .show();
                }
            }
    };
    private ValueEventListener SaveListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            //si on accède, c'Est que le sondage n'est pas terminée;
            if(dataSnapshot.exists()){
                /*si il existe:
                    soit il est terminé(dans ce cas, on n'est pas supposé arrivé ici)
                    soit c'est une sauvegarde;
                  si il n'existe pas:
                    c'est la première fois que l'utilisateur réponds
                 */
            Object Value = dataSnapshot.getValue();
                if(Value instanceof Boolean){//==true pour dire qu'il a fini,, si ce n'est pas un instance de Boolean, c'est une structure de sauvegarde
                   boolean b = (boolean)Value;
                   getFragmentManager().popBackStack();
                }else{//c'est une structure
                    /*HashMap<String, Boolean> mapOptions = new HashMap<>();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        mapOptions.put(dataSnapshot1.getKey(), (boolean)dataSnapshot1.getValue());
                    }*/
                    HashMap<String, Integer> mapQuestion = new HashMap<>();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        mapQuestion.put(dataSnapshot1.getKey(), (int)(long)dataSnapshot1.getValue());
                    }
                    answerQuestionAdapter = new AnswerQuestionAdapter(getActivity(), liste_questions, Si_Resultats, mapQuestion);
                    ListeQuestion.setAdapter(answerQuestionAdapter);
                }
            }else{
                answerQuestionAdapter = new AnswerQuestionAdapter(getActivity(), liste_questions, Si_Resultats, null);
                ListeQuestion.setAdapter(answerQuestionAdapter);
            }
            //fiou
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private ValueEventListener QuestionListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String SondageRef = sondage.ID;
            for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                String TestRef = (String)dataSnapshot1.child(FireBaseInteraction.Question_Keys.SONDAGE_REF).getValue();
                if(TestRef.equals(SondageRef)){
                    ArrayList<Option> liste_options = new ArrayList<>();
                    Question q = dataSnapshot1.getValue(Question.class);
                    q.ID = dataSnapshot1.getKey();
                    q.notOnServer = false;
                    for(DataSnapshot dataSnapshot2 : dataSnapshot1.child(FireBaseInteraction.Question_Keys.OPTIONS).getChildren()){
                        Option o = dataSnapshot2.getValue(Option.class);
                        o.ID = dataSnapshot2.getKey();
                        o.Question_parent = q;
                        o.notOnServer = false;
                        liste_options.add(o);
                    }
                    q.SetOptions(liste_options);
                    liste_questions.add(q);
                }
            }
            if(user != null){
                if(!user.isAnonymous()) {
                    if(!Si_Resultats) {//si on est là pour les résultats, on ne charge pas, car le sondage est terminée
                        DatabaseReference SaveRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME)
                                .child(user.getUid()).child(FireBaseInteraction.Profil_Keys.SONDAGES).child(sondage.ID);
                        SaveRef.addListenerForSingleValueEvent(SaveListener);
                    }else{
                        answerQuestionAdapter = new AnswerQuestionAdapter(getActivity(), liste_questions, Si_Resultats, null);
                        ListeQuestion.setAdapter(answerQuestionAdapter);
                    }
                }else{
                    answerQuestionAdapter = new AnswerQuestionAdapter(getActivity(), liste_questions, Si_Resultats, null);
                    ListeQuestion.setAdapter(answerQuestionAdapter);
                }
            }else {
                answerQuestionAdapter = new AnswerQuestionAdapter(getActivity(), liste_questions, Si_Resultats, null);
                ListeQuestion.setAdapter(answerQuestionAdapter);
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void changePage(Fragment fragment);
        void updateMenu(FirebaseUser user);
        Profil getUtilisateur_Connecte();
        void ChangeTitle(String newTitle);
        void setUtilisateur_Connecte(Profil utilisateur_connecte);
    }
}

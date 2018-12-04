package personnal.askinquery;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class AnswerSondageFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Sondage sondage;
    private boolean Si_Resultats;
    private ArrayList<Question> liste_questions;
    private TextView AuteurText, DateDebut, DateFin;
    private AnswerQuestionAdapter answerQuestionAdapter;
    private ImageView ImageSondage;
    private ListView ListeQuestion;

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
    public static AnswerSondageFragment newInstance(Sondage sondage, boolean Si_Resultats) {
        AnswerSondageFragment fragment = new AnswerSondageFragment();
        Bundle args = new Bundle();

        args.putSerializable(ARG_PARAM1, sondage);
        args.putBoolean(ARG_PARAM2, Si_Resultats);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sondage = (Sondage)getArguments().getSerializable(ARG_PARAM1);
            Si_Resultats = getArguments().getBoolean(ARG_PARAM2);
        }
        liste_questions = new ArrayList<>();
    }
    @Override
    public void onDestroy(){

        super.onDestroy();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Button BtnTerminer, BtnSave;
        TextView TitreText;
        View Separateur;
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
        DateDebut = view.findViewById(R.id.sondage_answer_date_debut);
        DateFin = view.findViewById(R.id.sondage_answer_date_fin);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", getActivity().getResources().getConfiguration().locale);
        DateDebut.setText(String.format(getActivity().getString(R.string.Sondage_Elem_Date_Publie), format.format(sondage.date_public)));
        DateFin.setText(String.format(getActivity().getString(R.string.Sondage_Elem_Date_End), format.format(sondage.date_echeance)));

        AuteurText = view.findViewById(R.id.answer_sondage_auteur);
        if(sondage.Auteur == null){
            FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME)
                    .child(sondage.AuteurRef).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    sondage.Auteur = dataSnapshot.getValue(Profil.class);
                    sondage.Auteur.ID = dataSnapshot.getKey();
                    SpannableString content = new SpannableString(sondage.Auteur.Username);
                    content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                    AuteurText.setText(getResources().getString(R.string.Poll_Ans_Author, content));
                    AuteurText.setOnClickListener(ProfilListener);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            SpannableString content = new SpannableString(sondage.Auteur.Username);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            AuteurText.setText(getResources().getString(R.string.Poll_Ans_Author, content));
            AuteurText.setOnClickListener(ProfilListener);
        }

        ImageSondage = view.findViewById(R.id.answer_sondage_image);
        ListeQuestion = view.findViewById(R.id.answer_sondage_questions);
        if(!sondage.Chemin_Image.equals("N")) {
            StorageReference ImageRef = FirebaseStorage.getInstance().getReference().child(sondage.Chemin_Image);
            ImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                    ImageSondage.setImageBitmap(bitmap);
                    ImageSondage.setVisibility(View.VISIBLE);

                }
            });
        }else{
            ListeQuestion.setBackground(getResources().getDrawable(R.drawable.background));
            ImageSondage.setVisibility(View.INVISIBLE);
        }







        Query QuestionRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Question_Keys.STRUCT_NAME).orderByChild(FireBaseInteraction.Question_Keys.SONDAGE_REF).equalTo(sondage.ID);
        QuestionRef.addListenerForSingleValueEvent(QuestionListener);
        BtnTerminer = view.findViewById(R.id.answer_sondage_btn_terminer);
        BtnSave = view.findViewById(R.id.answer_sondage_btn_save);
        Separateur = view.findViewById(R.id.answer_sondage_separateur);
        if(Si_Resultats){
            ((View)BtnSave.getParent()).setVisibility(View.GONE);
            Separateur.setVisibility(View.GONE);
            BtnTerminer.setText(R.string.Poll_Answ_GoBack);
            BtnTerminer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getFragmentManager().popBackStack();

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
    private View.OnClickListener ProfilListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!Si_Resultats) {
                String Message;
                if (user != null) {
                    if (!user.isAnonymous()) {
                        Message = "Toutes vos réponses non sauvegardées seront perdues.";
                    } else {
                        Message = "Toutes vos réponses seront perdues.";
                    }
                } else {
                    Message = "Toutes vos réponses seront perdues.";

                }
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Quitter le sondage?")
                        .setMessage("Voulez-vous vraiment quitter ce sondage? " + Message)
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                mListener.changePage(ConsultProfilFragment.newInstance(sondage.Auteur));
                            }

                        })
                        .setNegativeButton("Non", null)
                        .show();
            }else{
                mListener.changePage(ConsultProfilFragment.newInstance(sondage.Auteur));
            }
        }
    };
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
                    }
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
                if(Value instanceof Boolean){//==true pour dire qu'il a fini, si ce n'est pas un instance de Boolean, c'est une structure de sauvegarde
                   boolean b = (boolean)Value;
                   getFragmentManager().popBackStack();
                }else{
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
            for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    ArrayList<Option> liste_options = new ArrayList<>();
                    Question q = dataSnapshot1.getValue(Question.class);
                    q.Sondage_parent = sondage;
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
            Collections.sort(liste_questions, new Comparator<Question>() {
                @Override
                public int compare(Question q1, Question q2) {
                    return Integer.compare(q1.Numero, q2.Numero);
                }
            });
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

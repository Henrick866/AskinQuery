package personnal.askinquery;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private EditText EmailField, PassField;
    private TextView LoginErr;
    private Button LoginBtn, CreateBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser user, OldUser;
    private OnFragmentInteractionListener mListener;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mListener.ChangeTitle("Profil | Connexion");
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        EmailField = view.findViewById(R.id.login_email);
        PassField = view.findViewById(R.id.login_pass);
        LoginErr = view.findViewById(R.id.login_err);
        LoginBtn = view.findViewById(R.id.login_btn);
        CreateBtn = view.findViewById(R.id.create_btn);
        CreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.changePage(CreerProfilFragment.newInstance(mAuth));
            }
        });
        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                OldUser = null;
                if(user != null){
                    if(user.isAnonymous()){
                        OldUser = user;
                    }
                }
                //todo :: sync les données?
                //todo:: demander la syncronisation
                //todo: via les transactions, vérifiez si le sondage a été répondu par les deux parties, si oui, décrémentez les scores
                String Pass = PassField.getEditableText().toString(), Email = EmailField.getEditableText().toString();
                mAuth.signInWithEmailAndPassword(Email, Pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            SyncSubscribe();
                        }else{
                            LoginErr.setText("Courriel ou mot de passe érronné.");
                            LoginErr.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }
        });
        return view;
    }
    private void SyncSubscribe(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(mAuth.getCurrentUser().getUid());                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Profil p = dataSnapshot.getValue(Profil.class);
                HashMap<String, String> AuteursAbo = new HashMap<>();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.child(FireBaseInteraction.Profil_Keys.AUTEURS_SUIVIS).getChildren()){
                    AuteursAbo.put(dataSnapshot1.getKey(), (String)dataSnapshot1.getValue());
                    String Key = dataSnapshot1.getKey();
                    FirebaseMessaging.getInstance().subscribeToTopic(Key);
                }
                HashMap<String,Object> SondagesDone = new HashMap<>();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.child(FireBaseInteraction.Profil_Keys.SONDAGES).getChildren()){
                    SondagesDone.put(dataSnapshot1.getKey(), dataSnapshot1.getValue());
                }
                p.Auteurs_Suivis = AuteursAbo;
                p.Sondages_Faits = SondagesDone;
                mListener.setUtilisateur_Connecte(p);
                mListener.updateMenu(mAuth.getCurrentUser());
                mListener.changePage(SondageListFragment.newInstance(false, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

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
        void ChangeTitle(String newTitle);
        Profil getUtilisateur_Connecte();
        void setUtilisateur_Connecte(Profil utilisateur_Connecte);
        void updateMenu(FirebaseUser profil);
    }
    public String getTitle(){
        return "Connexion";
    }
}

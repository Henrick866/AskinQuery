package personnal.askinquery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.view.Change;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ManageProfilFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ManageProfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManageProfilFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView UsernameText, EmailText, PassText, OtherText, DeleteAvatar, ChangeAvatar;
    private ImageView AvatarPreview;
    private FloatingActionButton SaveBtn;
    private Dialog dialog;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private EditText InputField;
    private Button Confirm;
    private ProgressBar PassStrength;
    private TextView PassStrengthIndic;
    private LinearLayout Layout;
    private Profil Utilisateur;
    private int SumPass;
    private AuthCredential credential;


    private OnFragmentInteractionListener mListener;

    public ManageProfilFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ManageProfilFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ManageProfilFragment newInstance(String param1) {
        ManageProfilFragment fragment = new ManageProfilFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }
    //Ce que je veux : pouvoir edit le nom d'utilisateur, le courriel, le mot de passe et l'avatar. delete le compte.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        user = mAuth.getCurrentUser();
        View view = inflater.inflate(R.layout.fragment_manage_profil, container, false);
        UsernameText = view.findViewById(R.id.profil_manage_usernameText);
        EmailText = view.findViewById(R.id.profil_manage_emailText);
        PassText = view.findViewById(R.id.profil_manage_passText);
        OtherText = view.findViewById(R.id.profil_manage_moreOptions);
        DeleteAvatar = view.findViewById(R.id.profil_manage_delete_avatar);
        ChangeAvatar = view.findViewById(R.id.profil_manage_change_avatar);
        AvatarPreview = view.findViewById(R.id.profil_manage_avatarPreview);
        SaveBtn = view.findViewById(R.id.profil_manage_saveBtn);
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_dialog_manage_profil);
        UsernameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                dialog.setTitle("Modifier le nom d'utilisateur");
                Layout = dialog.findViewById(R.id.dialog_username_edit);
                Layout.setVisibility(View.VISIBLE);
                InputField = dialog.findViewById(R.id.manage_profil_edit_username);
                InputField.setCompoundDrawables(null, null, null, null);
                final TextView UserErr = dialog.findViewById(R.id.manage_profil_username_err);
                Button Available = dialog.findViewById(R.id.manage_profil_dispo);
                Confirm = dialog.findViewById(R.id.manage_profil_username_confirm);
                InputField.setText(user.getDisplayName());
                Available.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!InputField.getEditableText().toString().isEmpty()) {
                            UserErr.setVisibility(View.INVISIBLE);
                            AvailableUsername(InputField.getEditableText().toString());
                        }else{
                            UserErr.setText("Le champ est vide.");
                            UserErr.setVisibility(View.VISIBLE);
                        }
                    }
                });
                Confirm.setText("Annuler");
                Confirm.setTextColor(getResources().getColor(R.color.colorRed));
                Confirm.setOnClickListener(CancelClick);

            }
        });
        EmailText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                dialog.setTitle("Modifier le courriel");
                Layout = dialog.findViewById(R.id.dialog_email_edit);
                Layout.setVisibility(View.VISIBLE);
                InputField = dialog.findViewById(R.id.manage_profil_edit_email);
                InputField.setText(user.getEmail());
                final TextView EmailErr = dialog.findViewById(R.id.manage_profil_email_err);
                Confirm = dialog.findViewById(R.id.manage_profil_email_btn);
                Confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!InputField.getEditableText().toString().isEmpty()) {
                            if (user.getEmail().equals(InputField.getEditableText().toString())) {
                                EmailErr.setVisibility(View.INVISIBLE);
                                Layout.setVisibility(View.GONE);
                                dialog.dismiss();
                                Toast.makeText(getActivity(), "Adresse courriel inchangé", Toast.LENGTH_LONG).show();
                            } else {
                                if (InputField.getEditableText().toString().matches("([A-Za-z0-9\\-\\_\\.]+)\\@([A-Za-z0-9]+)\\.([A-Za-z]{2,})")) {
                                    credential = EmailAuthProvider.getCredential(user.getEmail(), mParam1);
                                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            user.updateEmail(InputField.getEditableText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getActivity(), "Adresse courriel modifiée avec succès", Toast.LENGTH_LONG).show();
                                                        EmailErr.setVisibility(View.INVISIBLE);
                                                        Layout.setVisibility(View.GONE);
                                                        EmailText.setText(user.getEmail());
                                                        dialog.dismiss();
                                                    } else {
                                                        Toast.makeText(getActivity(), "Erreur, l'adresse courriel n'a pas pu être changé", Toast.LENGTH_LONG).show();
                                                        EmailErr.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    EmailErr.setText("Courriel invalide");
                                    EmailErr.setVisibility(View.VISIBLE);
                                }
                            }
                        }else{
                            EmailErr.setText("Le champ est vide");
                            EmailErr.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
        PassText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                dialog.setTitle("Modifier le mot de passe");
                Layout = dialog.findViewById(R.id.dialog_pass_edit);
                InputField = dialog.findViewById(R.id.manage_profil_edit_pass);
                Confirm = dialog.findViewById(R.id.manage_profil_pass_btn);
                Button Cancel = dialog.findViewById(R.id.manage_profil_cancel_pass);
                final TextView PassErr = dialog.findViewById(R.id.manage_profil_pass_err);
                if(PassStrength == null){
                    PassStrength = dialog.findViewById(R.id.manage_profil_pass_strength);
                }
                if(PassStrengthIndic == null){
                    PassStrengthIndic = dialog.findViewById(R.id.manage_profil_pass_strength_indic);
                }
                TextWatcher oldWatcher = (TextWatcher)InputField.getTag();
                if(oldWatcher != null){
                    InputField.removeTextChangedListener(oldWatcher);
                }
                InputField.setTag(PassStrengthWatcher);
                InputField.addTextChangedListener(PassStrengthWatcher);
                Confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!InputField.getEditableText().toString().isEmpty()){
                            if(InputField.getEditableText().toString().length() >= 8){
                                if(SumPass < 2){
                                    PassErr.setText("Mot de passe trop faible.");
                                    PassErr.setVisibility(View.VISIBLE);
                                }else{
                                    PassErr.setVisibility(View.INVISIBLE);
                                    credential = EmailAuthProvider.getCredential(user.getEmail(), mParam1);
                                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            user.updatePassword(InputField.getEditableText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        mParam1 = InputField.getEditableText().toString();
                                                        Toast.makeText(getActivity(), "Mot de passe changé", Toast.LENGTH_LONG).show();
                                                        Layout.setVisibility(View.GONE);
                                                        dialog.dismiss();
                                                    }else{
                                                        Toast.makeText(getActivity(), "Erreur, mot de passe inchangé", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    });

                                }
                            }else{
                                PassErr.setText("Mot de passe trop petit (8 caractères minimum).");
                                PassErr.setVisibility(View.VISIBLE);
                            }
                        }else{
                            PassErr.setText("Le champ est vide.");
                            PassErr.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
        OtherText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                dialog.setTitle("Paramètres dangereux");
                Layout = dialog.findViewById(R.id.dialog_more_options);
                Layout.setVisibility(View.VISIBLE);
                Button DeleteAccountBtn = dialog.findViewById(R.id.manage_profil_delete);
                DeleteAccountBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity(), "Compte supprimé (En fait non)", Toast.LENGTH_LONG).show();
                        credential = EmailAuthProvider.getCredential(user.getEmail(), mParam1);
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //TODO: supprimer les données du database, sauf les sondages + supprimmer le fichier avatar
                                            Toast.makeText(getActivity(), "Compte supprimé avec succès", Toast.LENGTH_LONG).show();
                                            Layout.setVisibility(View.GONE);
                                            dialog.dismiss();
                                            mListener.updateMenu(mAuth.getCurrentUser());
                                            mListener.changePage(SondageListFragment.newInstance(false));
                                        }
                                    }
                                });
                            }
                        });

                    }
                });
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Layout.setVisibility(View.GONE);
                dialog.dismiss();
            }
        });
        return view;
    }
    private TextWatcher PassStrengthWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean[] TestGroup = new boolean[4];
            //0,1,2 = non; 3 = faible; 4 = moyen 5 = fort
            String PassTest = editable.toString();
            TestGroup[0] = PassTest.matches("(?=.*[a-z]).+");
            TestGroup[1] = PassTest.matches("(?=.*[A-Z]).+");
            TestGroup[2] = PassTest.matches("(?=.*[0-9]).+");//numériques
            TestGroup[3] = PassTest.matches("(?=.*[^a-zA-z0-9\\s\"']).+");//caractères spéciaux
            SumPass = 0;
            for (int i=0; i<TestGroup.length; i++) {
                SumPass += TestGroup[i] ? 1 : 0;
            }
            if(!PassTest.isEmpty()){
                PassStrengthIndic.setVisibility(View.VISIBLE);
                switch(SumPass){
                    case 0:
                    case 1:
                        PassStrength.setProgress(0);
                        PassStrength.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                        PassStrengthIndic.setTextColor(Color.BLACK);
                        PassStrengthIndic.setText("Inacceptable");
                        break;
                    case 2: PassStrength.setProgress(33);
                        PassStrength.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorRed), PorterDuff.Mode.SRC_IN);
                        PassStrengthIndic.setTextColor(getResources().getColor(R.color.colorRed));
                        PassStrengthIndic.setText("Faible");
                        break;
                    case 3: PassStrength.setProgress(66);
                        PassStrength.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorYellow), PorterDuff.Mode.SRC_IN);
                        PassStrengthIndic.setTextColor(getResources().getColor(R.color.colorYellow));
                        PassStrengthIndic.setText("Moyen");
                        break;
                    case 4: PassStrength.setProgress(100);
                        PassStrength.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorGreen), PorterDuff.Mode.SRC_IN);
                        PassStrengthIndic.setTextColor(getResources().getColor(R.color.colorGreen));
                        PassStrengthIndic.setText("Fort");
                        break;
                }
            }
        }
    };
    private void AvailableUsername(final String tempUser){
        InputField.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.ic_wait), null);
        ValueEventListener ProfileUsernameVerifier = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean Found = false;
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    if(tempUser.equals(dataSnapshot1.child(FireBaseInteraction.Profil_Keys.USERNAME).getValue())){
                        Found = true;
                    }
                }
                if(Found){
                    InputField.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.ic_clear_color),null);
                    Confirm.setEnabled(false);
                    Confirm.setText("Annuler");
                    Confirm.setTextColor(getResources().getColor(R.color.colorRed));
                    Confirm.setOnClickListener(CancelClick);
                }else{
                    InputField.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.ic_done_color), null);
                    Confirm.setEnabled(true);
                    Confirm.setText("Confirmer");
                    Confirm.setTextColor(getResources().getColor(R.color.colorPrimary));
                    Confirm.setOnClickListener(ConfirmUsernameClick);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        DatabaseReference profilRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME);
        profilRef.addListenerForSingleValueEvent(ProfileUsernameVerifier);
    }
    View.OnClickListener CancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Layout.setVisibility(View.GONE);
            dialog.dismiss();
        }
    };
    View.OnClickListener ConfirmUsernameClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            UserProfileChangeRequest profileUpdates;
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(InputField.getEditableText().toString())
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                UsernameText.setText(InputField.getEditableText().toString());
                                Toast.makeText(getActivity(), "Nom d'utilisateur changé.", Toast.LENGTH_LONG).show();
                                Layout.setVisibility(View.GONE);
                                dialog.dismiss();
                            }else{
                                Toast.makeText(getActivity(), "Erreur, nom d'utilisateur inchangé.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
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
        // TODO: Update argument type and name
        void changePage(Fragment fragment);
        void updateMenu(FirebaseUser user);
    }
}

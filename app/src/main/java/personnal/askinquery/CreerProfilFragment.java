package personnal.askinquery;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreerProfilFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreerProfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreerProfilFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private TextView Title, UserErr, EmailErr, PassErr, PassStrengthIndic;
    private EditText UserField, EmailField, PassField;
    private ImageView AvatarPreview;
    private Button btnConfirm, btnUpload;
    private ProgressBar PassStrength;
    private Bitmap ImageBitmap;
    private Uri Image;
    private int SumPass;
    private ProgressDialog progressDialog;
    private static final int MY_PERMISSION_REQUEST_READ_STORAGE = 200;
    private final int GALL_ACTION = 17;

    private OnFragmentInteractionListener mListener;

    public CreerProfilFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreerProfilFragment.
     */
    public static CreerProfilFragment newInstance(FirebaseAuth mAuth) {
        CreerProfilFragment fragment = new CreerProfilFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mListener.ChangeTitle("Profil | Créer");
        View view = inflater.inflate(R.layout.fragment_profil, container, false);
        Title = view.findViewById(R.id.profil_form_title);
        UserErr = view.findViewById(R.id.profil_form_username_err);
        EmailErr = view.findViewById(R.id.profil_form_email_err);
        PassErr = view.findViewById(R.id.profil_form_pass_err);
        UserField = view.findViewById(R.id.profil_form_username);
        EmailField = view.findViewById(R.id.profil_form_email);
        PassField = view.findViewById(R.id.profil_form_pass);
        PassStrengthIndic = view.findViewById(R.id.profil_form_pass_strength_indic);
        PassStrength = view.findViewById(R.id.profil_form_pass_strength);
        AvatarPreview = view.findViewById(R.id.profil_form_avatar_preview);
        btnUpload = view.findViewById(R.id.profil_form_file_btn);
        btnConfirm = view.findViewById(R.id.profil_form_btn_confirm);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GALL_ACTION);
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Validation();
            }
        });
        //password init
        TextWatcher oldWatcher = (TextWatcher)PassField.getTag();
        if(oldWatcher != null){
            PassField.removeTextChangedListener(oldWatcher);
        }
        //fill content
        if(currentUser != null){
            if(!currentUser.isAnonymous()) {
                PassField.setEnabled(false);
            }
        }

        PassField.setTag(passStrengthWatcher);
        PassField.addTextChangedListener(passStrengthWatcher);
        return view;
    }

    TextWatcher passStrengthWatcher = new TextWatcher() {
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == GALL_ACTION && resultCode == Activity.RESULT_OK) {
            Image = data.getData();
            try {
                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST_READ_STORAGE);
                }else{
                    MakeImage();
                }
            }catch(Exception e){
                Log.e("Err", e.getMessage());
            }


        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_PERMISSION_REQUEST_READ_STORAGE){
            if(grantResults.length == 0){
                try {
                    ImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Image);
                    AvatarPreview.setImageBitmap(ImageBitmap);
                    AvatarPreview.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "Le fichier sera envoyé au serveur lorsque le compte sera créé",Toast.LENGTH_LONG).show();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else{//accepte
                MakeImage();
            }
        }
    }
    private void MakeImage(){
        Bitmap TempImageBitmap = TraitementImage.RotateImage(Image, getActivity());
        ImageBitmap = TraitementImage.createSquaredBitmap(TempImageBitmap, getActivity());
        AvatarPreview.setImageBitmap(ImageBitmap);
        AvatarPreview.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "Le fichier sera envoyé au serveur lorsque le compte sera créé",Toast.LENGTH_LONG).show();

    }
    private void Validation(){
        boolean Valid = true;
        String Email, User, Pass;
        Email = EmailField.getEditableText().toString();
        User = UserField.getEditableText().toString();
        Pass = PassField.getEditableText().toString();
        if (!Email.isEmpty()){
            if (!Email.matches("([A-Za-z0-9\\-\\_\\.]+)\\@([A-Za-z0-9]+)\\.([A-Za-z]{2,})")) {
                EmailErr.setText("Courriel invalide.");
                EmailErr.setVisibility(View.VISIBLE);
            }else{
                EmailErr.setVisibility(View.INVISIBLE);
            }
        }else{
            EmailErr.setText("Champ vide.");
            EmailErr.setVisibility(View.VISIBLE);
        }
        if(!Pass.isEmpty()){
            if(Pass.length() >= 8){
                if(SumPass < 2){
                    Valid = false;
                    PassErr.setVisibility(View.VISIBLE);
                    PassErr.setText("Mot de passe trop faible.");
                }else{
                    PassErr.setVisibility(View.INVISIBLE);
                }
            }else{
                Valid = false;
                PassErr.setVisibility(View.VISIBLE);
                PassErr.setText("Mot de passe trop petit (8 caractères minimum).");
            }
        }else{
            Valid = false;
            PassErr.setVisibility(View.VISIBLE);
            PassErr.setText("Champ vide.");
        }
        if(User.isEmpty()){
            Valid = false;
            UserErr.setVisibility(View.VISIBLE);
            UserErr.setText("Champ vide.");
        }else{
            UserErr.setVisibility(View.INVISIBLE);
        }
        if(Valid){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Enregistrement du compte...");
            progressDialog.show();
            RegisterClient(User, Email, Pass);
        }
    }
    private void RegisterClient(final String user, final String email, final String pass){
        final StorageReference AvatarRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PROFILS_AVATARS);
        final DatabaseReference UserDataRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME);
        if(currentUser == null) {
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentUser = mAuth.getCurrentUser();
                                String avatarPath = "N";

                                UserProfileChangeRequest profileUpdates;
                                if(ImageBitmap != null ){
                                    avatarPath = FireBaseInteraction.Storage_Paths.PROFILS_AVATARS+currentUser.getUid()+".jpg";
                                    UploadFile(AvatarRef.child(currentUser.getUid()+".jpg"), ImageBitmap, "Envoi de l'avatar");
                                }

                                profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(user)
                                        .setPhotoUri(Uri.parse(avatarPath))
                                        .build();
                                Profil p = new Profil(user, email, avatarPath);
                                HashMap<String, Object> map = p.toMap();
                                UserDataRef.child(currentUser.getUid()).setValue(map);
                                progressDialog.setTitle("enregistrement du profil");
                                currentUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progressDialog.dismiss();
                                                    mListener.updateMenu(currentUser);
                                                    mListener.changePage(SondageListFragment.newInstance(false, null));
                                                }
                                            }
                                        });

                            } else {
                                Toast.makeText(getActivity(), "Enregistrement échoué", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }else{//totest : Fusion anonyme-compte;
            if(currentUser.isAnonymous()){//si l'utilisateur est anonyme, on crée un compte et on le lui associe, question de transférer les données
                            final AuthCredential credential = EmailAuthProvider.getCredential(email, pass);
                            currentUser.linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        final String OldUserId = currentUser.getUid();
                                        currentUser = task.getResult().getUser();
                                        String avatarPath = "N";

                                        if (ImageBitmap != null) {
                                            avatarPath = FireBaseInteraction.Storage_Paths.PROFILS_AVATARS + currentUser.getUid() + ".jpg";
                                            UploadFile(AvatarRef.child(currentUser.getUid() + ".jpg"), ImageBitmap, "Envoi de l'avatar");
                                        }

                                        final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(user)
                                                .setPhotoUri(Uri.parse(avatarPath))
                                                .build();
                                        Profil p = new Profil(user, email, avatarPath);
                                        final HashMap<String, Object> map = p.toMap();
                                        DatabaseReference SondagesDoneListGetter = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(OldUserId).child(FireBaseInteraction.Profil_Keys.SONDAGES);
                                        SondagesDoneListGetter.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                map.put(FireBaseInteraction.Profil_Keys.SONDAGES, dataSnapshot.getValue());
                                                UserDataRef.child(currentUser.getUid()).setValue(map);
                                                progressDialog.setTitle("Enregistrement du profil");
                                                currentUser.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    progressDialog.dismiss();
                                                                    mListener.updateMenu(currentUser);//switch au nouvel utilisateur;
                                                                    mListener.changePage(SondageListFragment.newInstance(false, null));
                                                                }
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });


                                    } else {
                                        Toast.makeText(getActivity(), "Enregistrement échoué", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
            }
        }
    }

    private void UploadFile(StorageReference storageReference, Bitmap Image, final String FlavorText){
        progressDialog.setTitle(FlavorText);
        progressDialog.show();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = storageReference;
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.setTitle(FlavorText + "Terminé");
                        mListener.updateMenu(currentUser);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.setTitle(FlavorText + "Échoué");
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Transfert éffectué à "+(int)progress+"%");
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
        void updateMenu(FirebaseUser user);
    }
    public String getTitle(){
        return "Profil | Créer";
    }
}

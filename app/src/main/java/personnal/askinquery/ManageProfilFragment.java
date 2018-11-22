package personnal.askinquery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.provider.FirebaseInitProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ManageProfilFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ManageProfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManageProfilFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int AVATAR_GET = 5;
    private static final int MY_PERMISSION_REQUEST_READ_STORAGE = 200;

    private String mParam1;
    private String mParam2;
    private TextView UsernameText, EmailText, PassText, OtherText, DeleteAvatar, ChangeAvatar, Abonnements;
    private ImageView AvatarPreview;
    private Dialog dialog;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private EditText InputField;
    private Button Confirm, Cancel;
    private ProgressBar PassStrength;
    private TextView PassStrengthIndic;
    private LinearLayout Layout;
    private Profil Utilisateur;
    private Bitmap ImageBitmap;
    private Uri Image;
    private int SumPass;
    private AuthCredential credential;
    private StorageReference AvatarRef;
    private DatabaseReference UserReference;
    private ProgressDialog progressDialog;
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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mListener.ChangeTitle("Profil | Gestion");
        user = mAuth.getCurrentUser();
        progressDialog = new ProgressDialog(getActivity());
        View view = inflater.inflate(R.layout.fragment_manage_profil, container, false);
        UsernameText = view.findViewById(R.id.profil_manage_usernameText);
        UsernameText.setText(user.getDisplayName());
        EmailText = view.findViewById(R.id.profil_manage_emailText);
        EmailText.setText(user.getEmail());
        PassText = view.findViewById(R.id.profil_manage_passText);
        OtherText = view.findViewById(R.id.profil_manage_moreOptions);
        DeleteAvatar = view.findViewById(R.id.profil_manage_delete_avatar);
        ChangeAvatar = view.findViewById(R.id.profil_manage_change_avatar);
        AvatarPreview = view.findViewById(R.id.profil_manage_avatarPreview);
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_dialog_manage_profil);
        UserReference = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(user.getUid());
        AvatarRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PROFILS_AVATARS+user.getUid()+".jpg");
        AvatarRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                AvatarPreview.setImageBitmap(b);
            }
        });
        ChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, AVATAR_GET);
            }
        });
        DeleteAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Supprimer l'avatar")
                        .setMessage("Voulez-vous vraiment supprimer votre avatar?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AvatarRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            UserReference.child(FireBaseInteraction.Profil_Keys.AVATAR).setValue("N");
                                            Toast.makeText(getActivity(), "Avatar Supprimé", Toast.LENGTH_LONG).show();
                                            UserProfileChangeRequest profileUpdates;
                                            profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setPhotoUri(Uri.parse("N"))
                                                    .build();
                                            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        AvatarPreview.setImageResource(R.mipmap.ic_launcher);
                                                        mListener.updateMenu(user);
                                                    }
                                                }
                                            });

                                        }else{
                                            Toast.makeText(getActivity(), "Erreur, Avatar inchangé", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }

                        })
                        .setNegativeButton("Non", null)
                        .show();
            }
        });
        UsernameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                dialog.setTitle("Modifier le nom d'utilisateur");
                Layout = dialog.findViewById(R.id.dialog_username_edit);
                Layout.setVisibility(View.VISIBLE);
                InputField = dialog.findViewById(R.id.manage_profil_edit_username);
                InputField.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                final TextView UserErr = dialog.findViewById(R.id.manage_profil_username_err);
                UserErr.setVisibility(View.INVISIBLE);
                Button Available = dialog.findViewById(R.id.manage_profil_dispo);
                Confirm = dialog.findViewById(R.id.manage_profil_username_confirm);
                InputField.setText(user.getDisplayName());
                Available.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UserErr.setTextColor(getResources().getColor(R.color.colorRed));
                        if(!InputField.getEditableText().toString().isEmpty()) {
                            if(InputField.getEditableText().toString().length() < 3){
                                UserErr.setText(R.string.Username_Err_Short);
                                UserErr.setTextColor(getResources().getColor(R.color.colorRed));
                                UserErr.setVisibility(View.VISIBLE);
                            }else if(InputField.getEditableText().toString().length() > 25){
                                UserErr.setText(R.string.Username_Err_Long);
                                UserErr.setTextColor(getResources().getColor(R.color.colorRed));
                                UserErr.setVisibility(View.VISIBLE);
                            }else {
                                if (InputField.getEditableText().toString().equals(user.getDisplayName())) {
                                    UserErr.setText(R.string.Mng_Profil_Username_Err_Same);
                                    UserErr.setTextColor(Color.BLACK);
                                    UserErr.setVisibility(View.VISIBLE);
                                } else {
                                    UserErr.setVisibility(View.INVISIBLE);
                                    AvailableUsername(InputField.getEditableText().toString());
                                }
                            }

                        }else{
                            UserErr.setText(R.string.Gen_Empty_Field);
                            UserErr.setVisibility(View.VISIBLE);
                        }
                    }
                });
                Confirm.setText(R.string.Mng_Profil_Cancel);
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
                Cancel = dialog.findViewById(R.id.manage_profil_cancel_email);
                final TextView EmailErr = dialog.findViewById(R.id.manage_profil_email_err);
                EmailErr.setVisibility(View.INVISIBLE);
                Confirm = dialog.findViewById(R.id.manage_profil_email_btn);
                Cancel.setOnClickListener(CancelClick);
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
                                            if(task.isSuccessful()) {
                                                user.updateEmail(InputField.getEditableText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getActivity(), "Adresse courriel modifiée avec succès", Toast.LENGTH_LONG).show();
                                                            EmailErr.setVisibility(View.INVISIBLE);
                                                            Layout.setVisibility(View.GONE);
                                                            EmailText.setText(user.getEmail());
                                                            mListener.updateMenu(user);
                                                            UserReference.child(FireBaseInteraction.Profil_Keys.COURRIEL).setValue(user.getEmail());
                                                            dialog.dismiss();
                                                        } else {
                                                            Toast.makeText(getActivity(), "Erreur, l'adresse courriel n'a pas pu être changé", Toast.LENGTH_LONG).show();

                                                        }
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(getActivity(), "Erreur, réauthentification échouée", Toast.LENGTH_LONG).show();

                                            }

                                        }
                                    });
                                } else {
                                    EmailErr.setText(R.string.Email_Field_Err_Invalid);
                                    EmailErr.setVisibility(View.VISIBLE);
                                }
                            }
                        }else{
                            EmailErr.setText(R.string.Gen_Empty_Field);
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
                Layout.setVisibility(View.VISIBLE);
                InputField = dialog.findViewById(R.id.manage_profil_edit_pass);
                Confirm = dialog.findViewById(R.id.manage_profil_pass_btn);
                Cancel = dialog.findViewById(R.id.manage_profil_cancel_pass);
                final TextView PassErr = dialog.findViewById(R.id.manage_profil_pass_err);
                PassErr.setVisibility(View.INVISIBLE);
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
                Cancel.setOnClickListener(CancelClick);
                InputField.setTag(PassStrengthWatcher);
                InputField.addTextChangedListener(PassStrengthWatcher);
                Confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!InputField.getEditableText().toString().isEmpty()){
                            if(InputField.getEditableText().toString().length() >= 8){
                                if(SumPass < 2){
                                    PassErr.setText(R.string.Pass_Err_Too_Weak);
                                    PassErr.setVisibility(View.VISIBLE);
                                }else{
                                    PassErr.setVisibility(View.INVISIBLE);
                                    credential = EmailAuthProvider.getCredential(user.getEmail(), mParam1);
                                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                user.updatePassword(InputField.getEditableText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            mParam1 = InputField.getEditableText().toString();
                                                            Toast.makeText(getActivity(), "Mot de passe changé", Toast.LENGTH_LONG).show();
                                                            Layout.setVisibility(View.GONE);
                                                            dialog.dismiss();
                                                        } else {
                                                            Toast.makeText(getActivity(), "Erreur, mot de passe inchangé", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(getActivity(), "Erreur, réauthentification échouée", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                                }
                            }else{
                                PassErr.setText(R.string.Pass_Err_Too_Short);
                                PassErr.setVisibility(View.VISIBLE);
                            }
                        }else{
                            PassErr.setText(R.string.Gen_Empty_Field);
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
                Cancel = dialog.findViewById(R.id.manage_profil_cancel_other);
                Button DeleteAccountBtn = dialog.findViewById(R.id.manage_profil_delete);
                Cancel.setOnClickListener(CancelClick);
                DeleteAccountBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        credential = EmailAuthProvider.getCredential(user.getEmail(), mParam1);
                        final String UserId = user.getUid();
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                DatabaseReference delProfileRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(UserId);

                                                delProfileRef.removeValue();

                                                if (!user.getPhotoUrl().toString().equals("N")) {
                                                    StorageReference delAvatarRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PROFILS_AVATARS + UserId + ".jpg");
                                                    delAvatarRef.delete();
                                                }
                                                Toast.makeText(getActivity(), "Compte supprimé avec succès", Toast.LENGTH_LONG).show();
                                                Layout.setVisibility(View.GONE);
                                                dialog.dismiss();
                                                mListener.updateMenu(mAuth.getCurrentUser());
                                                mListener.changePage(SondageListFragment.newInstance(false, null));
                                            } else {
                                                Toast.makeText(getActivity(), "Erreur, compte non supprimé.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }else{
                                    Toast.makeText(getActivity(), "Erreur, réauthentification échouée",Toast.LENGTH_LONG).show();
                                }
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
        Abonnements = view.findViewById(R.id.profil_manage_followed);
        Abonnements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Profil p = mListener.getUtilisateur_Connecte();
                final ArrayList<String> ListeAbonnementID = new ArrayList<>(), ListeAbonnementNom = new ArrayList<>();
                for(HashMap.Entry<String, String> cursor : p.Auteurs_Suivis.entrySet()) {
                    ListeAbonnementID.add(cursor.getKey());
                    ListeAbonnementNom.add(cursor.getValue());
                }
                String[] StringArray = new String[p.Auteurs_Suivis.size()];
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle("Quel est le profil que vous voulez consulter?");
                b.setItems(ListeAbonnementNom.toArray(StringArray), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        DatabaseReference profilRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(ListeAbonnementID.get(i));
                        profilRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Profil p = dataSnapshot.getValue(Profil.class);
                                p.ID = dataSnapshot.getKey();
                                mListener.changePage(ConsultProfilFragment.newInstance(p));
                                dialogInterface.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                dialogInterface.dismiss();
                            }
                        });
                    }
                });
                b.show();
            }
        });
        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == AVATAR_GET && resultCode == Activity.RESULT_OK) {
            Image = data.getData();
            try {
                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST_READ_STORAGE);
                }else{
                    MakeImage();
                    UploadFile(AvatarRef, ImageBitmap, "Envoi de l'avatar");
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
                    UploadFile(AvatarRef, ImageBitmap, "Envoi de l'avatar");

                }catch(Exception e){
                    e.printStackTrace();
                }
            }else{//accepte
                MakeImage();
                UploadFile(AvatarRef, ImageBitmap, "Envoi de l'avatar");
            }
        }
    }
    private void MakeImage(){
        Bitmap TempImageBitmap = TraitementImage.RotateImage(Image, getActivity());
        ImageBitmap = TraitementImage.createSquaredBitmap(TempImageBitmap, getActivity());
        AvatarPreview.setImageBitmap(ImageBitmap);
        AvatarPreview.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "Le fichier sera envoyé au serveur lorsque le sondage sera créé/modifié",Toast.LENGTH_LONG).show();

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
                        UserReference.child(FireBaseInteraction.Profil_Keys.AVATAR).setValue(FireBaseInteraction.Storage_Paths.PROFILS_AVATARS+user.getUid()+".jpg");
                        UserProfileChangeRequest profileUpdates;
                        profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(Uri.parse(FireBaseInteraction.Storage_Paths.PROFILS_AVATARS+user.getUid()+"jpg"))
                                .build();
                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    mListener.updateMenu(user);
                                    progressDialog.dismiss();
                                }
                            }
                        });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.setTitle(FlavorText + "Échoué");
                        progressDialog.dismiss();
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
                        PassStrengthIndic.setText(R.string.Pass_Strength_Unacceptable);
                        break;
                    case 2: PassStrength.setProgress(33);
                        PassStrength.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorRed), PorterDuff.Mode.SRC_IN);
                        PassStrengthIndic.setTextColor(getResources().getColor(R.color.colorRed));
                        PassStrengthIndic.setText(R.string.Pass_Strength_Weak);
                        break;
                    case 3: PassStrength.setProgress(66);
                        PassStrength.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorYellow), PorterDuff.Mode.SRC_IN);
                        PassStrengthIndic.setTextColor(getResources().getColor(R.color.colorYellow));
                        PassStrengthIndic.setText(R.string.Pass_Strength_Medium);
                        break;
                    case 4: PassStrength.setProgress(100);
                        PassStrength.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorGreen), PorterDuff.Mode.SRC_IN);
                        PassStrengthIndic.setTextColor(getResources().getColor(R.color.colorGreen));
                        PassStrengthIndic.setText(R.string.Pass_Strength_Strong);
                        break;
                }
            }
        }
    };
    private void AvailableUsername(final String tempUser){
        InputField.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_wait), null);
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
                    InputField.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_clear_color),null);
                    Confirm.setEnabled(false);
                    Confirm.setText(R.string.Mng_Profil_Cancel);
                    Confirm.setTextColor(getResources().getColor(R.color.colorRed));
                    Confirm.setOnClickListener(CancelClick);
                }else{
                    InputField.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_done_color), null);
                    Confirm.setEnabled(true);
                    Confirm.setText(R.string.Mng_Profil_Confirm);
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
                                UserReference.child(FireBaseInteraction.Profil_Keys.USERNAME).setValue(InputField.getEditableText().toString());
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
        void changePage(Fragment fragment);
        void ChangeTitle(String newTitle);
        void updateMenu(FirebaseUser user);
        Profil getUtilisateur_Connecte();
        void setUtilisateur_Connecte(Profil utilisateur_Connecte);
    }
    public String getTitle(){
        return "Profil | Gérér";
    }
}

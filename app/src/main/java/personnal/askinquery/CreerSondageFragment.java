package personnal.askinquery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreerSondageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreerSondageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreerSondageFragment extends Fragment implements Serializable {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final int PIC_GALL = 3;
    private static final int MY_PERMISSION_REQUEST_READ_STORAGE = 200;

    private boolean Si_Edit;
    private Sondage sondage;
    private OnFragmentInteractionListener mListener;

    private EditText TitreSondage;
    private DatePicker DateEcheance;
    private ImageButton AjoutQuestion;
    private ListView ListeQuestion;

    private ArrayList<Question> liste_question;
    private Bitmap ImageBitmap;
    private CreerQuestionAdapter QuestionAdapter;
    private CreerSondageFragment creerSondageFragment;
    private ImageView ImagePreview;
    private CheckBox SiResultPublic;
    private ProgressBar LoadingQ;
    TextView QuestionsError;
    private TextView TitreError, DateEcheanceError, DateEcheanceConfirm;
    private  ProgressDialog progressDialog;
    private  boolean UploadFileFailed;
    private  boolean ImageChange = false, ImageOnServer = false;
    private int TaskDoneCount, TaskToDoCount;
    private  Uri Image;
    private FirebaseFunctions mFunctions;

    private FirebaseUser user;
    public CreerSondageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment CreerSondageFragment.
     */
    public static CreerSondageFragment newInstance(boolean Si_edit, Sondage s){
        CreerSondageFragment fragment = new CreerSondageFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM2, Si_edit);
        args.putSerializable(ARG_PARAM3, s);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Si_Edit = getArguments().getBoolean(ARG_PARAM2);
            sondage = (Sondage)getArguments().getSerializable(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Button Btn_Terminer, Btn_Upload, Btn_Cancel_Img, Btn_Enregistrer;
        TextView TitreFormulaire;
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        View view = inflater.inflate(R.layout.fragment_creer_sondage, container, false);
        final TabHost host = view.findViewById(R.id.TabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Sondage");
        spec.setContent(R.id.TabSondage);
        spec.setIndicator("Sondage");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Questions");
        spec.setContent(R.id.TabQuestions);
        spec.setIndicator("Questions");
        host.addTab(spec);
        mFunctions = FirebaseFunctions.getInstance();
        creerSondageFragment = this;
        liste_question = new ArrayList<>();
        TitreSondage = view.findViewById(R.id.sondage_edit_titre);
        DateEcheance = view.findViewById(R.id.sondage_edit_date);
        LoadingQ = view.findViewById(R.id.sondage_edit_questions_load);
        TitreError = view.findViewById(R.id.sondage_edit_titre_error);
        Calendar c = Calendar.getInstance();
        DateEcheanceError = view.findViewById(R.id.sondage_edit_date_error);
        Btn_Enregistrer = view.findViewById(R.id.sondage_edit_save);
        QuestionsError = view.findViewById(R.id.sondage_edit_questions_error);
        DateEcheance.setMinDate(c.getTimeInMillis());
        DateEcheanceConfirm = view.findViewById(R.id.sondage_edit_date_confirm);
        ImagePreview = view.findViewById(R.id.sondage_edit_image_preview);
        ListeQuestion = view.findViewById(R.id.sondage_edit_questions_list);
        ListeQuestion.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        ListeQuestion.setItemsCanFocus(true);
        Btn_Terminer = view.findViewById(R.id.sondage_edit_done);
        Btn_Upload = view.findViewById(R.id.sondage_edit_upload_img);
        SiResultPublic = view.findViewById(R.id.sondage_edit_public_results);
        Btn_Cancel_Img = view.findViewById(R.id.sondage_edit_remove_img);
        AjoutQuestion = view.findViewById(R.id.sondage_edit_add_question);
        TitreFormulaire = view.findViewById(R.id.sondage_edit_title_form);
        AjoutQuestion.setEnabled(false);
        Btn_Cancel_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ImageBitmap != null) {
                    ImageChange = true;
                    ImageBitmap = null;
                    ImagePreview.setImageBitmap(null);
                    ImagePreview.setVisibility(View.GONE);
                }
            }
        });
        final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy", getResources().getConfiguration().locale);
        //Initialisation des champs;
        mListener.ChangeTitle("Sondage | Créer");
        if(Si_Edit){
            mListener.ChangeTitle("Sondage | Modifier");
            TitreFormulaire.setText(R.string.Create_Sondage_Poll_Title_View_Edit);
            //date echance, init à l'ancienne date.
            TitreSondage.setText(sondage.Titre);
            Calendar c1 = Calendar.getInstance();
            c1.setTime(sondage.date_echeance);
            DateEcheance.init(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar c = Calendar.getInstance();
                    c.set(year, monthOfYear, dayOfMonth);

                    DateEcheanceConfirm.setText(format.format(c.getTime()));
                }
            });
            DateEcheanceConfirm.setText(format.format(sondage.date_echeance));
            SiResultPublic.setChecked(sondage.Compil_Public);
            DatabaseReference DataRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Question_Keys.STRUCT_NAME);
            Query query = DataRef.orderByChild(FireBaseInteraction.Question_Keys.SONDAGE_REF).equalTo(sondage.ID);
            query.addListenerForSingleValueEvent(loadListeSimple);
            if(!sondage.Chemin_Image.equals("N")) {
                ImageOnServer = true;
                StorageReference SondageImgRef = FirebaseStorage.getInstance().getReference().child(sondage.Chemin_Image);
                SondageImgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap Image = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                        ImagePreview.setImageBitmap(Image);
                        ImagePreview.setVisibility(View.VISIBLE);
                    }
                });
            }else{
                ImagePreview.setVisibility(View.GONE);
            }

        }else{
            //champs date echeance, init à aujourd'hui
            LoadingQ.setVisibility(View.GONE);
            AjoutQuestion.setEnabled(true);
            sondage = new Sondage();
            sondage.AuteurRef = FirebaseAuth.getInstance().getCurrentUser().getUid();
            sondage.ID = "temp";
            Calendar c2 = Calendar.getInstance();
            DateEcheance.init(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar c = Calendar.getInstance();
                    c.set(year, monthOfYear, dayOfMonth);

                    DateEcheanceConfirm.setText(format.format(c.getTime()));
                }
            });

            AjoutQuestion.setEnabled(true);
            QuestionAdapter = new CreerQuestionAdapter(getActivity(), liste_question, creerSondageFragment);
            ListeQuestion.setAdapter(QuestionAdapter);
            QuestionsError.setVisibility(View.VISIBLE);
            QuestionsError.setText(R.string.Create_Sondage_NoQuestion_Err);
        }
        //Fin initialisation champs

        TitreSondage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()){
                    TitreError.setVisibility(View.INVISIBLE);
                }else{
                    TitreError.setVisibility(View.VISIBLE);
                    TitreError.setText(R.string.Create_Sondage_No_Title_Err);
                }
            }
        });
        //TODO:: ##### Call direct function
        Btn_Terminer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Publier le sondage")
                        .setMessage("Voulez-vous rendre directement le sondage public? Sachez que vous ne pourrez plus le modifier si il est public.")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                progressDialog = new ProgressDialog(getActivity());
                                progressDialog.setTitle("Initialisation...");
                                progressDialog.show();
                                TaskDoneCount = 0;
                                TaskToDoCount = 0;
                                AddTask();
                                Thread mThread = new Thread(){
                                    @Override
                                    public void run() {

                                Sondage s = new Sondage();
                                if(Si_Edit){
                                    s.Date_Created = sondage.Date_Created;
                                }else {
                                    s.Date_Created = Calendar.getInstance().getTime().getTime();
                                }
                                    Calendar c = Calendar.getInstance();
                                int day = DateEcheance.getDayOfMonth(), month = DateEcheance.getMonth(), year = DateEcheance.getYear();
                                c.set(year, month, day);
                                s.Date_Echeance = c.getTime().getTime();
                                s.date_echeance = new Date(s.Date_Echeance);
                                s.Date_Public = Calendar.getInstance().getTime().getTime(); //là on le publie et on aura besoin de cette date pour trier sur la liste principale;
                                s.date_public = new Date(s.Date_Public);
                                s.Titre = TitreSondage.getText().toString();
                                s.Compil_Public = SiResultPublic.isChecked();
                                s.Questions = QuestionAdapter.getQuestions();
                                s.Publied = true;
                                s.AuteurRef = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                if(Si_Edit) {
                                    s.ID = sondage.ID;
                                }
                                Validation(s);
                                    }
                                };
                                mThread.start();
                            }

                        })
                        .setNegativeButton("Non", null)
                        .show();

            }
        });
        Btn_Enregistrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("Initialisation...");
                progressDialog.show();
                TaskDoneCount = 0;
                TaskToDoCount = 0;
                AddTask();
                Thread mThread = new Thread(){
                    @Override
                    public void run() {
                        Sondage s = new Sondage();
                        if(Si_Edit){
                            s.Date_Created = sondage.Date_Created;
                        }else {
                            s.Date_Created = Calendar.getInstance().getTime().getTime();
                        }
                        Calendar c = Calendar.getInstance();
                        int day = DateEcheance.getDayOfMonth(), month = DateEcheance.getMonth(), year = DateEcheance.getYear();
                        c.set(year, month, day);
                        s.Date_Echeance = c.getTime().getTime();
                        s.date_echeance = new Date(s.Date_Echeance);
                        s.Date_Public = 0; //puisque il n'est pas publié et que la liste de gestion est trié selon la date de création et que ce sondage ne sera visible que là-bas, on peut mettre 0
                        s.date_public = new Date(s.Date_Public);
                        s.Titre = TitreSondage.getText().toString();
                        s.Compil_Public = SiResultPublic.isChecked();
                        s.Questions = QuestionAdapter.getQuestions();
                        s.Publied = false;
                        s.AuteurRef = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        if(Si_Edit) {
                            s.ID = sondage.ID;
                        }
                        Validation(s);
                    }
                };
                mThread.start();

            }
        });
        Btn_Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, PIC_GALL);
            }
        });
        Btn_Cancel_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ImageChange = true;

                ImagePreview.setVisibility(View.GONE);
                ImagePreview.setImageBitmap(null);
                Image = null;
                ImageBitmap = null;

            }
        });

        AjoutQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Question q = new Question();
                q.Options = new ArrayList<>();
                addQuestion(q);
            }
        });

        return view;
    }
    private ValueEventListener loadListeSimple = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                Question Q = dataSnapshot1.getValue(Question.class);
                Q.ID = dataSnapshot1.getKey();
                ArrayList<Option> ListOption = new ArrayList<>();
                for(DataSnapshot dataSnapshot2 : dataSnapshot1.child(FireBaseInteraction.Question_Keys.OPTIONS).getChildren()){
                    Option o = dataSnapshot2.getValue(Option.class);
                    o.ID = dataSnapshot2.getKey();
                    ListOption.add(o);
                }
                Q.Options = ListOption;
                liste_question.add(Q);
            }
            QuestionAdapter = new CreerQuestionAdapter(getActivity(), liste_question, creerSondageFragment);
            ListeQuestion.setAdapter(QuestionAdapter);
            AjoutQuestion.setEnabled(true);
            LoadingQ.setVisibility(View.GONE);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PIC_GALL && resultCode == Activity.RESULT_OK) {
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
                    ImagePreview.setImageBitmap(ImageBitmap);
                    ImagePreview.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "Le fichier sera envoyé au serveur lorsque le sondage sera créé/modifié",Toast.LENGTH_LONG).show();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else{//accepte
                MakeImage();
            }
        }
    }
    private void MakeImage(){
        ImageBitmap = TraitementImage.RotateImage(Image, getActivity());
        ImagePreview.setImageBitmap(ImageBitmap);
        ImageChange = true;
        ImagePreview.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "Le fichier sera envoyé au serveur lorsque le sondage sera créé/modifié",Toast.LENGTH_LONG).show();
    }
    private void UploadFile(StorageReference storageReference, Uri File, final String FlavorText){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitle(FlavorText);
            }
        });
        AddTask();
        storageReference.putFile(File)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setTitle(FlavorText + "Terminé");
                            }
                        });
                        TaskDone();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setTitle(FlavorText + "Échoué");
                            }
                        });
                        UploadFileFailed = true;
                        TaskDone();
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
    private void UploadFile(StorageReference storageReference, Bitmap Image, final String FlavorText){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitle(FlavorText);
            }
        });
        AddTask();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        storageReference.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setTitle(FlavorText + "Terminé");
                            }
                        });
                        TaskDone();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setTitle(FlavorText + "Échoué");
                            }
                        });
                        UploadFileFailed = true;
                        TaskDone();
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
    private void Validation(Sondage S){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitle("Validation du sondage...");
            }
        });
        boolean Valid = true;
        if(S.Titre.isEmpty()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TitreError.setText(R.string.Create_Sondage_No_Title_Err);
                    TitreError.setVisibility(View.VISIBLE);
                }
            });

            Valid = false;
        }else{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TitreError.setVisibility(View.INVISIBLE);
                }
            });
        }
        if(!Si_Edit) {//à moins qu'il ne soit nouveau, ce n'est pas grave.
            if (S.date_echeance.before(S.date_public)) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DateEcheanceError.setText(R.string.Create_Sondage_Date_Err);
                        DateEcheanceError.setVisibility(View.VISIBLE);
                    }
                });

                Valid = false;
            }
        }else{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DateEcheanceError.setVisibility(View.INVISIBLE);
                }
            });
        }
        if(!S.Questions.isEmpty()){

            boolean QV = true;
            for(Question q : S.Questions){
                boolean OV = true;
                if(q.Type_Question == 0){
                    QV = false;
                } else if(q.Texte_Question.isEmpty()){
                    QV = false;
                }else if(!q.Options.isEmpty()){

                    for(Option o : q.Options){
                        if(o.Texte.isEmpty()){
                            OV = false;
                        }
                        if(o.Chemin_Media != null) {
                            if(o.Chemin_Media.equals("N")){
                                if ((q.Type_Question == Question.TYPE_IMAGE && o.ImagePreload == null) || (q.Type_Question == Question.TYPE_VIDEO && o.UriVideo == null && o.ImagePreload == null)) {
                                    OV = false;
                                }
                            }
                        }

                    }
                }else{
                    QV = false;
                }
                if(!OV){
                    QV = false;
                }
            }
            if(!QV){
                Valid = false;
            }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        QuestionsError.setVisibility(View.INVISIBLE);
                    }
                });
        }else{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    QuestionsError.setText(R.string.Create_Sondage_NoQuestion_Err);
                    QuestionsError.setVisibility(View.VISIBLE);
                }
            });
            Valid = false;
            progressDialog.dismiss();
        }
        if(Valid){
            Upload(S);
    }else{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Le sondage est érroné, veuillez vérifier et corriger les érreurs", Toast.LENGTH_LONG).show();
                }
            });
    }

    }
    public void Upload(Sondage S){

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitle("Envoi des données...");
            }
        });
        DatabaseReference SondageRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME);
        if(!Si_Edit){//si nouveau sondage, assigne id
            S.ID = SondageRef.push().getKey();
        }
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        if(ImageChange) {//si il y a un changement d'image
            StorageReference SondageImgRef = FirebaseStorage.getInstance().getReference();
            if(ImageBitmap != null) {//si l'image existe et a changé, remplace ou ajoute
                S.Chemin_Image = FireBaseInteraction.Storage_Paths.SONDAGES_IMAGES+S.ID+".jpg";
                SondageImgRef = SondageImgRef.child(S.Chemin_Image);
                UploadFile(SondageImgRef, ImageBitmap, "Envoi de l'image du sondage...");
                Bitmap b = TraitementImage.CreateThumbnail(ImageBitmap, getActivity(), 512);
                SondageImgRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.SONDAGES_IMAGES_THUMBNAILS).child(S.ID+".jpg");
                UploadFile(SondageImgRef, b, "Envoi de l'image du sondage (miniature)...");

            }else{//si l'image a été supprimé (ne peut être supprimé que si il existait avant)
                if(ImageOnServer){//si l'image existait sur le serveur, on supprime
                    S.Chemin_Image = sondage.Chemin_Image; //si l'image est sur le serveur, on a un chemin
                    SondageImgRef.child(S.Chemin_Image);
                    SondageImgRef.delete();
                }
            }
        }else{
            if(ImageOnServer){
                S.Chemin_Image = FireBaseInteraction.Storage_Paths.SONDAGES_IMAGES+S.ID+".jpg";
            }else{
                S.Chemin_Image = "N";
            }
        }

        Map<String, Object> Map = S.Map();
        SondageRef = SondageRef.child(S.ID);
        SondageRef.setValue(Map);
        //questions
        for(Question q : S.Questions) {//pour chaque question
            DatabaseReference QuestionRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Question_Keys.STRUCT_NAME);
            if (q.notOnServer) {//si il n'est pas sur le serveur (donc nouveau), assigne un id
                q.ID = QuestionRef.push().getKey();
            }
            q.Numero = S.Questions.indexOf(q);
            q.SondageRef = S.ID;//met à jour la référence du sondage de la question
            QuestionRef = QuestionRef.child(q.ID);
            if (q.toBeDeleted) { //si toBeDeleted == true, il est sur le serveur;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setTitle("Suppresion de la question");
                    }
                });
                    for(Option o : q.Options){
                    StorageReference OptionsMedDelRef = FirebaseStorage.getInstance().getReference();
                    //si il est en ligne on supprime et il a un chemin existant
                    if(q.Type_Question != Question.TYPE_TEXTE){
                        OptionsMedDelRef = OptionsMedDelRef.child(o.Chemin_Media);
                        OptionsMedDelRef.delete();
                    }
                }
                QuestionRef.removeValue(); //puisque les options font partie des questions, leurs données sont supprimés
            } else {
                Map<String, Object> QuestionMap = q.Map();
                Map<String, Object> OptionsMap = new HashMap<>();

                for (Option o : q.Options) {//pour chaque option
                    if (o.notOnServer) {//si il n'est pas sur le serveur (assigne un id)
                        o.ID = QuestionRef.child(FireBaseInteraction.Question_Keys.OPTIONS).push().getKey();
                    }

                    StorageReference OptionMedRef = FirebaseStorage.getInstance().getReference();
                    if(o.toBeDeleted){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setTitle("Suppression de l'option");
                            }
                        });

                        DatabaseReference OptionDataDelRef = FirebaseDatabase.getInstance().getReference()
                                .child(FireBaseInteraction.Question_Keys.STRUCT_NAME)
                                .child(q.ID).child(FireBaseInteraction.Question_Keys.OPTIONS)
                                .child(o.ID);
                        if(q.Type_Question != Question.TYPE_TEXTE) {
                            if(q.Type_Question == Question.TYPE_IMAGE){
                                FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES_THUMBNAILS).child(o.ID+".jpg").delete();
                            }else{
                                FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS_THUMBNAILS).child(o.ID+".jpg").delete();
                            }
                            OptionMedRef = OptionMedRef.child(o.Chemin_Media);
                            OptionMedRef.delete();
                        }
                        OptionDataDelRef.removeValue();
                    }else{
                        if (q.Type_Question != Question.TYPE_TEXTE) {//si ce n'est pas type texte
                            if (q.Type_Question == Question.TYPE_IMAGE) {//si c'est un type image
                                o.Chemin_Media = FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES + o.ID + ".jpg";
                                if (o.DataChanged) {//si l'image a changé, téléverse
                                    OptionMedRef = OptionMedRef.child(o.Chemin_Media);
                                    UploadFile(OptionMedRef, o.ImageFull, "envoi de " + o.ID + ".jpg");
                                    OptionMedRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES_THUMBNAILS).child(o.ID+".jpg");

                                    UploadFile(OptionMedRef, o.ImagePreload, "envoi de " + o.ID + ".jpg (miniature)");
                                }
                                if(!o.notOnServer){//si il existe sur le serveur, on éfface les fichiers vidéos qui existent peut-être
                                    final String ID = o.ID;
                                    final StorageReference VidRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS).child(o.ID+".mp4");
                                    VidRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {//il existe on supprime, sinon, rien;
                                            VidRef.delete();
                                            FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS_THUMBNAILS).child(ID+".jpg").delete();
                                        }
                                    });
                                }
                            }
                            if (q.Type_Question == Question.TYPE_VIDEO) {//si c'est une vidéo
                                String Extension = mime.getExtensionFromMimeType(getActivity().getContentResolver().getType(Uri.parse(o.UriVideo)));
                                o.Chemin_Media = FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS + o.ID + "." + Extension;
                                if (o.DataChanged) {//si l'option a changé, (changement de vidéo), éléverse le thumbnail et la vidéo
                                    OptionMedRef = OptionMedRef.child(o.Chemin_Media);
                                    UploadFile(OptionMedRef, Uri.parse(o.UriVideo), "envoi de " + o.ID + "." + Extension);
                                    OptionMedRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS_THUMBNAILS).child(o.ID + ".jpg");
                                    UploadFile(OptionMedRef, o.ImagePreload, "envoi de " + o.ID + "." + Extension + " (miniature)");
                                }
                                if(!o.notOnServer){//si il existe sur le serveur, on éfface les images possiblement existantes médias
                                    final String ID = o.ID;
                                    final StorageReference ImgRefDel = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES).child(o.ID+".jpg");
                                    ImgRefDel.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {//il existe on supprime, sinon, rien;
                                            ImgRefDel.delete();
                                            FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES_THUMBNAILS).child(ID+".jpg").delete();
                                        }
                                    });
                                }
                            }
                        } else {//si c'est type texte,
                            o.UriImage = "";
                            o.UriVideo = "";
                            o.ImagePreload = null;
                            o.Chemin_Media = "N";
                            o.Chemin_Image = "N";
                            o.Chemin_Video = "N";
                            if(!o.notOnServer){//si il existe sur le serveur, on éfface les fichiers médias
                                final String ID = o.ID;
                                final StorageReference ImgRefDel = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES).child(o.ID+".jpg");
                                ImgRefDel.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {//il existe on supprime, sinon, rien;
                                        ImgRefDel.delete();
                                        FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES_THUMBNAILS).child(ID+".jpg").delete();
                                    }
                                });
                                final StorageReference VidRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS).child(o.ID+".mp4");
                                VidRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {//il existe on supprime, sinon, rien;
                                        VidRef.delete();
                                        FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS_THUMBNAILS).child(ID+".jpg").delete();
                                    }
                                });
                            }
                        }
                        Map<String, Object> OptionMap = o.Map();
                        OptionsMap.put(o.ID, OptionMap);
                    }
                }
                QuestionMap.put(FireBaseInteraction.Question_Keys.OPTIONS, OptionsMap);
                QuestionRef.setValue(QuestionMap);
            }
        }
        if(S.Publied){
            PublishPoll(S)
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Exception e = task.getException();
                                if (e instanceof FirebaseFunctionsException) {
                                    FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                    FirebaseFunctionsException.Code code = ffe.getCode();
                                    Object details = ffe.getDetails();
                                }

                                // ...
                            }

                            // ...
                        }
                    });
        }
        TaskDone();

    }
    private void AddTask(){
        TaskToDoCount++;
    }
    private synchronized void TaskDone(){
        TaskDoneCount++;
        if(TaskToDoCount == TaskDoneCount){
            progressDialog.dismiss();
            if(UploadFileFailed){
                Toast.makeText(getActivity(), "Des fichiers n'ont pas étés envoyés, veuillez recommencer.", Toast.LENGTH_LONG).show();
            }else {
                mListener.changePage(SondageListFragment.newInstance(true, null));
            }
        }
    }
    private Task<String> PublishPoll(Sondage S) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("AuteurRef", user.getUid());
        data.put("UserName", user.getDisplayName());
        data.put("ID", S.ID);

        return mFunctions
                .getHttpsCallable("publishPoll")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        return (String) task.getResult().getData();
                    }
                });
    }

    public void addQuestion(Question q){
        q.ID = String.valueOf(liste_question.size());
        q.notOnServer = true;
            q.SondageRef = sondage.ID;
        q.Sondage_parent = sondage;
        liste_question.add(q);
        if(liste_question.size() >= 1){
            QuestionsError.setVisibility(View.INVISIBLE);
        }
        QuestionAdapter.notifyDataSetChanged();
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
    }
    public String getTitle(){
        return Si_Edit?"Sondage | Modifier":"Sondage | Créer";
    }
}

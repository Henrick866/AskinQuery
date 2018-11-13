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
import android.graphics.Path;
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
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
public class CreerSondageFragment extends Fragment implements CreerOptionDialog.OptionDialogListener, Serializable {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final int PIC_GALL = 3;
    private static final int MY_PERMISSION_REQUEST_READ_STORAGE = 200;
    private static final int MEDIA_GALL_OPTION = 5;

    private boolean Si_Admin;
    private boolean Si_Edit;
    private Sondage sondage;
    private Profil profil;
    private OnFragmentInteractionListener mListener;
    private Option tempOption;
    private String CheminImage;
    private EditText TitreSondage;
    private DatePicker DateEcheance;
    private ImageButton AjoutQuestion;
    private ListView ListeQuestion;
    private Button Btn_Terminer, Btn_Upload, Btn_Cancel_Img;
    private ArrayList<Question> liste_question;
    private Bitmap ImageBitmap;
    private CreerQuestionAdapter QuestionAdapter;
    private ImageView ImagePreview;
    private CheckBox SiResultPublic;
    TextView QuestionsError;
    private TextView TitreError, DateError, DateConfirm, TitreFormulaire;
    private  ProgressDialog progressDialog;
    private  boolean UploadFileFailed;
    private  boolean ImageChange = false, ImageOnServer = false;
    private  Uri Image;
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
    public static CreerSondageFragment newInstance(boolean Si_Admin, boolean Si_edit, Profil auteur, Sondage s){
        CreerSondageFragment fragment = new CreerSondageFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, Si_Admin);
        args.putBoolean(ARG_PARAM2, Si_edit);
        args.putSerializable(ARG_PARAM3, s);
        args.putSerializable(ARG_PARAM4, auteur);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Si_Admin = getArguments().getBoolean(ARG_PARAM1);
            Si_Edit = getArguments().getBoolean(ARG_PARAM2);
            sondage = (Sondage)getArguments().getSerializable(ARG_PARAM3);
            profil = (Profil)getArguments().getSerializable(ARG_PARAM4);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_creer_sondage, container, false);
        final TabHost host = (TabHost)view.findViewById(R.id.TabHost);
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

        liste_question = new ArrayList<>();
        TitreSondage = view.findViewById(R.id.sondage_edit_titre);
        DateEcheance = view.findViewById(R.id.sondage_edit_date);
        TitreError = view.findViewById(R.id.sondage_edit_titre_error);
        Calendar c = Calendar.getInstance();
        DateError = view.findViewById(R.id.sondage_edit_date_error);
        QuestionsError = view.findViewById(R.id.sondage_edit_questions_error);
        DateEcheance.setMinDate(c.getTimeInMillis());
        DateConfirm = view.findViewById(R.id.sondage_edit_date_confirm);
        ImagePreview = view.findViewById(R.id.sondage_edit_image_preview);
        ListeQuestion = view.findViewById(R.id.sondage_edit_questions_list);
        ListeQuestion.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        Btn_Terminer = view.findViewById(R.id.sondage_edit_done);
        Btn_Upload = view.findViewById(R.id.sondage_edit_upload_img);
        SiResultPublic = view.findViewById(R.id.sondage_edit_public_results);
        Btn_Cancel_Img = view.findViewById(R.id.sondage_edit_remove_img);
        AjoutQuestion = view.findViewById(R.id.sondage_edit_add_question);
        TitreFormulaire = view.findViewById(R.id.sondage_edit_title_form);
        AjoutQuestion.setEnabled(false);
        /**
         * simple ajout : IC f -> t
         * simple supp :
         * */
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
        final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");
        final CreerSondageFragment creerSondageFragment = this;
        //Initialisation des champs;
        mListener.ChangeTitle("Sondage | Créer");
        if(Si_Edit){
            mListener.ChangeTitle("Sondage | Modifier");
            TitreFormulaire.setText("Modifier un sondage");
            //date echance, init à l'ancienne date.
            TitreSondage.setText(sondage.Titre);
            Calendar c1 = Calendar.getInstance();
            c1.setTime(sondage.date_echeance);
            DateEcheance.init(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar c = Calendar.getInstance();
                    c.set(year, monthOfYear, dayOfMonth);

                    DateConfirm.setText(format.format(c.getTime()));
                }
            });
            DateConfirm.setText(format.format(sondage.date_echeance));
            SiResultPublic.setChecked(sondage.Compil_Public);
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
            sondage = new Sondage();
            sondage.AuteurRef = profil.ID;
            sondage.ID = "temp";
            Calendar c2 = Calendar.getInstance();
            DateEcheance.init(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar c = Calendar.getInstance();
                    c.set(year, monthOfYear, dayOfMonth);

                    DateConfirm.setText(format.format(c.getTime()));
                }
            });

            AjoutQuestion.setEnabled(true);
            QuestionAdapter = new CreerQuestionAdapter(getActivity(), liste_question, creerSondageFragment);
            ListeQuestion.setAdapter(QuestionAdapter);
            QuestionsError.setVisibility(View.VISIBLE);
            QuestionsError.setText("Vous devez poser au moins une question.");
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
                    TitreError.setText("Vous devez mettre un titre au sondage.");
                }
            }
        });

        Btn_Terminer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //à refaire pour envoyer les données sur le firebase
                //todo: faire du ménage séparer les méthodes
                Sondage s = new Sondage();
                s.AuteurRef = profil.ID;
                s.date_public = Calendar.getInstance().getTime();
                Calendar c = Calendar.getInstance();
                int day = DateEcheance.getDayOfMonth(), month = DateEcheance.getMonth(), year = DateEcheance.getYear();
                c.set(year, month, day);
                s.date_echeance = c.getTime();
                s.Date_Echeance = s.date_echeance.getTime();
                s.Date_Public = s.date_public.getTime();
                s.Titre = TitreSondage.getText().toString();
                s.Compil_Public = SiResultPublic.isChecked();
                s.Questions = QuestionAdapter.getQuestions();
                s.AuteurRef = profil.ID;
                if(Si_Edit) {
                    s.ID = sondage.ID;
                }
                /*###### TEST on laisse faire les questions, on teste crud et valid du sondage ######*/
                boolean Valid = true;
                if(s.Titre.isEmpty()) {
                    TitreError.setText("Vous devez mettre un titre au sondage.");
                    TitreError.setVisibility(View.VISIBLE);
                    Valid = false;
                }//si titre vide
                if(!Si_Edit) {//à moins qu'il ne soit nouveau, ce n'est pas grave.
                    if (s.date_echeance.before(s.date_public)) {
                        DateError.setText("La date d'échéance ne peut être avant la date d'aujourd'hui");
                        DateError.setVisibility(View.VISIBLE);
                        Valid = false;
                    }
                }
                if(!s.Questions.isEmpty()){
                    boolean QV = true;
                    for(Question q : s.Questions){
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
                                /**
                                 *  new opt : CM = null, CI/CV = "N" (T,V,I)
                                 *  new opt with img : CM = "N", CI/CV = path (I,V)
                                 *  unchanged opt : CM = path, CI/CV = null (I,V)
                                 *  modif opt : CM = "N", CI/CV = path2
                                 *
                                 * si unchanged, type valide, skip
                                 *
                                 */
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
                }else{
                    QuestionsError.setText("Vous devez poser au moins une question");
                    QuestionsError.setVisibility(View.VISIBLE);
                    Valid = false;
                }
                if(Valid){
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setTitle("Initialisation...");
                    progressDialog.show();
                    DatabaseReference SondageRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME);
                    if(!Si_Edit){//si nouveau sondage, assigne id
                        s.ID = SondageRef.push().getKey();
                    }
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    if(ImageChange) {//si il y a un changement d'image
                        StorageReference SondageImgRef = FirebaseStorage.getInstance().getReference();
                        if(ImageBitmap != null) {//si l'image existe et a changé, remplace ou ajoute
                            s.Chemin_Image = FireBaseInteraction.Storage_Paths.SONDAGES_IMAGES+s.ID+".jpg";
                            SondageImgRef = SondageImgRef.child(s.Chemin_Image);
                            UploadFile(SondageImgRef, ImageBitmap, "Envoi de l'image du sondage...");
                        }else{//si l'image a été supprimé (ne peut être supprimé que si il existait avant)
                            if(ImageOnServer){//si l'image existait sur le serveur, on supprime
                                s.Chemin_Image = sondage.Chemin_Image; //si l'image est sur le serveur, on a un chemin
                                SondageImgRef.child(s.Chemin_Image);
                                SondageImgRef.delete();
                            }
                        }
                    }else{
                        if(ImageOnServer){
                            s.Chemin_Image = FireBaseInteraction.Storage_Paths.SONDAGES_IMAGES+s.ID+".jpg";
                        }else{
                            s.Chemin_Image = "N";
                        }
                    }

                    Map<String, Object> Map = s.Map();
                    SondageRef = SondageRef.child(s.ID);
                    SondageRef.setValue(Map);
                    //questions
                    for(Question q : s.Questions) {//pour chaque question
                        DatabaseReference QuestionRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Question_Keys.STRUCT_NAME);
                        if (q.notOnServer) {//si il n'est pas sur le serveur (donc nouveau), assigne un id
                            q.ID = QuestionRef.push().getKey();
                        }
                        q.SondageRef = s.ID;//met à jour la référence du sondage de la question
                        QuestionRef = QuestionRef.child(q.ID);
                        if (q.toBeDeleted) { //si toBeDeleted == true, il est sur le serveur;
                            progressDialog.setTitle("Suppresion de la question");
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
                                        progressDialog.setTitle("Suppression de l'option");
                                        DatabaseReference OptionDataDelRef = FirebaseDatabase.getInstance().getReference()
                                                                            .child(FireBaseInteraction.Question_Keys.STRUCT_NAME)
                                                                            .child(q.ID).child(FireBaseInteraction.Question_Keys.OPTIONS)
                                                                            .child(o.ID);
                                    if(q.Type_Question != Question.TYPE_TEXTE) {
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
                                                UploadFile(OptionMedRef, o.ImagePreload, "envoi de " + o.ID + ".jpg");
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
                                        }
                                    } else {//si c'est type texte,
                                        o.UriImage = "";
                                        o.UriVideo = "";
                                        o.ImagePreload = null;
                                        o.Chemin_Media = "N";
                                        o.Chemin_Image = "N";
                                        o.Chemin_Video = "N";
                                    }
                                    Map<String, Object> OptionMap = o.Map();
                                    OptionsMap.put(o.ID, OptionMap);
                                }
                            }
                            QuestionMap.put(FireBaseInteraction.Question_Keys.OPTIONS, OptionsMap);
                            QuestionRef.setValue(QuestionMap);
                        }
                    }
                    //QuestionAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                    mListener.changePage(SondageListFragment.newInstance(true, null));
                }else{
                    Toast.makeText(getActivity(), "Le sondage est érroné, veuillez vérifier et corriger les érreurs", Toast.LENGTH_LONG).show();
                }

                /*##### Fin TEST #####*/
                //Validation(s);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PIC_GALL && resultCode == Activity.RESULT_OK) {
            CheminImage = data.toUri(0);
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
        progressDialog.setTitle(FlavorText);
        StorageReference ref = storageReference;
        ref.putFile(File)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.setTitle(FlavorText + "Terminé");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.setTitle(FlavorText + "Échoué");
                        UploadFileFailed = true;
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
        progressDialog.setTitle(FlavorText);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = storageReference;
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.setTitle(FlavorText + "Terminé");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.setTitle(FlavorText + "Échoué");
                        UploadFileFailed = true;
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
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Validation du sondage...");
        boolean SondageValide = true;
        if(S.Titre.isEmpty()) {
            TitreError.setText("Vous devez mettre un titre au sondage.");
            TitreError.setVisibility(View.VISIBLE);
            SondageValide = false;
        }
        if(S.Questions.size() < 1){
            SondageValide = false;
        }
        if(SondageValide) {
            boolean QuestionsValide = true;
            progressDialog.setMessage("Vérification des questions...");
            for (Question q : S.Questions) {
                if (q.Texte_Question.isEmpty() || q.Type_Question == 0) {
                    QuestionsValide = false;
                }
                if (q.Options.size() < 2) {
                    QuestionsValide = false;
                } else {
                    boolean OptionsValide = true;
                    for (Option o : q.Options) {
                        if (q.Type_Question == 0) {
                            OptionsValide = false;
                        } else if (q.Type_Question != Question.TYPE_TEXTE) {
                            if ((q.Type_Question == Question.TYPE_IMAGE && Uri.parse(o.UriImage) == null) || (q.Type_Question == Question.TYPE_VIDEO && Uri.parse(o.UriVideo) == null)) {
                                OptionsValide = false;
                            }
                        }
                        if (o.Texte.isEmpty()) {
                            OptionsValide = false;
                        }
                    }
                    if (!OptionsValide) {
                        QuestionsValide = false;
                    }
                }
            }
            if (!QuestionsValide) {
                SondageValide = false;
            }
        }
        progressDialog.dismiss();
        if(SondageValide){
            Upload(S);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Votre Sondage contient des érreurs, veuillez vérifier chaque élément, chaque question et chacune de leurs options.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }
    public void Upload(Sondage S){
        progressDialog = new ProgressDialog(getActivity());
        UploadFileFailed = false;
        progressDialog.setTitle("Envoi du sondage... Préparation");
        DatabaseReference SondageRef = FirebaseDatabase.getInstance().getReference().child("Sondages");
        if(!Si_Edit){
            S.ID = SondageRef.push().getKey();
            if(Image != null) {
                S.Chemin_Image = FireBaseInteraction.Storage_Paths.SONDAGES_IMAGES + S.ID;
            }
        }
        StorageReference SondageImgRef = FirebaseStorage.getInstance().getReference().child(S.Chemin_Image);
        Map<String, Object> Map = S.Map();
        SondageRef = SondageRef.child(S.ID);
        SondageRef.setValue(Map);
        DatabaseReference QuestionRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Question_Keys.STRUCT_NAME);
        int noQuestion = 1;
        for(Question q : S.Questions){

            if(q.toBeDeleted){ //si il est marqué pour suppression, il est sur le serveur et a un id assigné par le serveur
                progressDialog.setTitle("Suppression de la question #"+noQuestion+"...");
                DatabaseReference QuestionDel = QuestionRef.child(q.ID);
                QuestionDel.removeValue();
            }else{//sinon on modifie ou ajoute
                progressDialog.setTitle("Envoi de la question #"+noQuestion+"...");
                if(q.notOnServer) { //asigne un id a la question si il n'est pas sur le serveur;
                    q.ID = QuestionRef.push().getKey();
                }
                QuestionRef = QuestionRef.child(q.ID);
                Map<String, Object> questionMap = q.Map();
                Map<String, Object> OptionsMap = new HashMap<>();
                DatabaseReference OptionsRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Question_Keys.STRUCT_NAME).child(q.ID).child(FireBaseInteraction.Question_Keys.OPTIONS);
                int optionNum = 1;
                for(Option o : q.Options){
                    if(o.toBeDeleted){
                        progressDialog.setTitle("Question #"+noQuestion+" : Suppression de l'option #"+optionNum+"...");
                        DatabaseReference OptionDel = OptionsRef.child(o.ID);
                        OptionDel.removeValue();
                    }else{
                        progressDialog.setTitle("Question #"+noQuestion+" : Préparation de l'option #"+optionNum+"...");
                        if(o.notOnServer) { //si il n'est pas sur le serveur, il est nouveau et a besoin d'un nouveau ID généré par le serveur
                            o.ID = OptionsRef.push().getKey();
                        }
                        if(q.Type_Question != Question.TYPE_TEXTE) {
                            StorageReference OptionRef;
                            if (q.Type_Question == Question.TYPE_IMAGE) {
                                o.Chemin_Media = FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES+o.ID;
                                OptionRef = FirebaseStorage.getInstance().getReference().child(o.Chemin_Media);
                                UploadFile(OptionRef, Uri.parse(o.UriImage), "Question #"+noQuestion+" : Envoi de l'image de l'option #"+optionNum+"...");
                            }
                            if (q.Type_Question == Question.TYPE_VIDEO) {
                                o.Chemin_Media = FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS+o.ID;
                                OptionRef = FirebaseStorage.getInstance().getReference().child(o.Chemin_Media);
                                UploadFile(OptionRef, Uri.parse(o.UriVideo),"Question #"+noQuestion+" : Envoi de la vidéo de l'option #"+optionNum+"...");
                            }
                        }else{
                            o.UriVideo = "";
                            o.UriImage = "";
                            o.Chemin_Video = "N";
                            o.Chemin_Image = "N";
                            o.Chemin_Media = "N";
                        }
                        Map<String, Object> OptionMap = o.Map();
                        OptionsMap.put(o.ID, OptionMap);
                    }
                    optionNum++;
                }
                progressDialog.setTitle("Envoi de la question #"+noQuestion+"...");
                questionMap.put(FireBaseInteraction.Question_Keys.OPTIONS, OptionsMap);
                QuestionRef.setValue(questionMap);
            }
            noQuestion++;
        }
        UploadFile(SondageImgRef, Image, "Envoi de l'image du sondage...");
        progressDialog.dismiss();
        if(UploadFileFailed){
            Toast.makeText(getActivity(), "L'envoi de vidéos ou d'images au serveur a échoué.", Toast.LENGTH_LONG);
        }else{
            mListener.changePage(SondageListFragment.newInstance(true, null));
        }

    }

    public void addQuestion(Question q){
        //liste_question = QuestionAdapter.getQuestions();
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
    public String onFinishedOptionDialog(ArrayList<Option> Options, int Position){
        liste_question.get(Position).Options = Options;
        QuestionAdapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), "La liste des options de la question #"+Position+" a été mis à jour", Toast.LENGTH_LONG).show();
        return "Done";
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

package personnal.askinquery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreatePostFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreatePostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreatePostFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int MEDIA_GALL_OPTION = 3;
    private static final int MY_PERMISSION_REQUEST_READ_STORAGE = 200;

    private Publication publication;
    private boolean Si_Edit, mediachange;//media change pour determiner si il faut uploader ou non, donc si le lien est un sondage, pas besoin d'uploader de fichier

    private TextView TitreErr, ContentErr, Instruct;
    private EditText TitreField, ContentField;
    private Button PollLinkBtn, DoneBtn, MedRmvBtn;
    private ImageView MediaPreview, MediaPreviewIcon;


    private FirebaseUser user;
    private ArrayList<String> ListeSondagesID;
    private ArrayList<String> ListeSondagesNom;
    private String SondageLink, ImgLinkSondage;
    private Uri Media;
    private ProgressDialog progressDialog;
    private int Type, typeTempo, TaskTotal, TaskDone;
    private Bitmap ImageBitmap;
    private FirebaseFunctions mFunctions;

    private OnFragmentInteractionListener mListener;

    public CreatePostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreatePostFragment.
     */
    public static CreatePostFragment newInstance(boolean Si_edit, Publication p) {
        CreatePostFragment fragment = new CreatePostFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, Si_edit);
        args.putSerializable(ARG_PARAM2, p);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Si_Edit = getArguments().getBoolean(ARG_PARAM1);
            publication = (Publication)getArguments().getSerializable(ARG_PARAM2);
        }
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        ListeSondagesID = new ArrayList<>();
        ListeSondagesNom = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        TextView TitreForm;
        Button ImgUpBtn, VidUpBtn;
        mediachange = false;
        mFunctions = FirebaseFunctions.getInstance();
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);
        TitreForm = view.findViewById(R.id.publication_edit_title_form);
        TitreErr = view.findViewById(R.id.publication_edit_titre_err);
        ContentErr = view.findViewById(R.id.publication_edit_contenu_err);
        TitreField = view.findViewById(R.id.publication_edit_titre_field);
        ContentField = view.findViewById(R.id.publication_edit_contenu_field);
        ImgUpBtn = view.findViewById(R.id.publication_edit_imgUpBtn);
        VidUpBtn = view.findViewById(R.id.publication_edit_vidUpBtn);
        PollLinkBtn = view.findViewById(R.id.publication_edit_pollLinkBtn);
        DoneBtn = view.findViewById(R.id.publication_edit_done_btn);
        MediaPreview = view.findViewById(R.id.publication_edit_media_preview);
        MedRmvBtn = view.findViewById(R.id.publication_edit_mediaRmvBtn);
        Instruct = view.findViewById(R.id.publication_edit_instruct);
        MediaPreviewIcon = view.findViewById(R.id.publication_edit_media_preview_icon);
        Type = Publication.TYPE_TEXTE;

        DatabaseReference SondageByAuthorRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME);
        SondageByAuthorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    String AuteurId = (String)dataSnapshot1.child(FireBaseInteraction.Sondage_Keys.AUTEUR_REF).getValue();
                    if(AuteurId.equals(user.getUid())){
                        String Nom = (String)dataSnapshot1.child(FireBaseInteraction.Sondage_Keys.TITRE).getValue();
                        ListeSondagesID.add(dataSnapshot1.getKey());
                        ListeSondagesNom.add(Nom);
                    }
                }
                PollLinkBtn.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mListener.ChangeTitle("Publication | Créer");
        if(Si_Edit){
            mListener.ChangeTitle("Publication | Modifier");
            TitreForm.setText(R.string.Post_Form_Title_Edit);
            TitreField.setText(publication.Titre);
            ContentField.setText(publication.Texte);
            Type = publication.Type;
            if(publication.Type != Publication.TYPE_TEXTE) {//disqualifie le type texte
                StorageReference ImageRef = FirebaseStorage.getInstance().getReference();

                if(publication.Type == Publication.TYPE_VIDEO) {

                     ImageRef  = ImageRef.child(FireBaseInteraction.Storage_Paths.PUBLICATION_VIDEOS_THUMBNAILS).child(publication.ID+".jpg");
                     MedRmvBtn.setText(R.string.Post_Form_Media_Remove_Vid);
                    MediaPreview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), VideoPopupActivity.class);
                            intent.putExtra("Path", publication.Media);
                            intent.putExtra("NotOnServer", false);
                            getActivity().startActivity(intent);
                        }
                    });
                    ImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Instruct.setText(R.string.Post_Elem_Instruct_Vid);
                            Instruct.setVisibility(View.VISIBLE);
                            MediaPreviewIcon.setImageResource(R.drawable.ic_play_circle_color);
                            MediaPreviewIcon.setVisibility(View.VISIBLE);
                            ImageBitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                            MediaPreview.setImageBitmap(ImageBitmap);
                            MediaPreview.setVisibility(View.VISIBLE);
                            MedRmvBtn.setVisibility(View.VISIBLE);
                        }
                    });
                }else{//sondage ou image (si sondage: si img sondage = null, met le logo)

                    if(publication.Type == Publication.TYPE_IMAGE){
                        ImageRef = ImageRef.child(FireBaseInteraction.Storage_Paths.PUBLICATION_IMAGES_THUMBNAILS).child(publication.ID+".jpg");//charge thumb
                        MedRmvBtn.setText(R.string.Post_Form_Media_Remove_Img);
                        ImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Instruct.setText(R.string.Post_Elem_Instruct_Img);
                                Instruct.setVisibility(View.VISIBLE);
                                MediaPreviewIcon.setImageResource(R.drawable.ic_loupe_black_24dp);
                                MediaPreviewIcon.setVisibility(View.VISIBLE);
                                ImageBitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                                MediaPreview.setImageBitmap(ImageBitmap);
                                MediaPreview.setVisibility(View.VISIBLE);
                                MedRmvBtn.setVisibility(View.VISIBLE);
                                MediaPreview.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        final Dialog dialog = new Dialog(getActivity());
                                        dialog.setContentView(R.layout.image_dialog);
                                        dialog.show();
                                        final ImageView Image = dialog.findViewById(R.id.image_dialog_imageview);
                                        final ProgressBar progressBar = dialog.findViewById(R.id.image_dialog_progressBar);
                                        StorageReference FullImgRef = FirebaseStorage.getInstance().getReference().child(publication.Media);
                                        FullImgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                Image.setImageBitmap(b);
                                                progressBar.setVisibility(View.GONE);
                                                Image.setVisibility(View.VISIBLE);
                                            }
                                        });
                                        final Button CloseBtn = dialog.findViewById(R.id.ImageCloseBtn);
                                        CloseBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }else{
                        SondageLink = publication.SondageRef;
                        MedRmvBtn.setText(R.string.Post_Form_Media_Remove_Poll);
                        Instruct.setText(R.string.Post_Elem_Instruct_Poll_Disabled);
                        Instruct.setVisibility(View.VISIBLE);
                        if(publication.Media.equals("N")){
                            MediaPreview.setImageResource(R.mipmap.ic_launcher);
                            MediaPreviewIcon.setImageResource(R.drawable.ic_sondage_debut);
                            MediaPreviewIcon.setVisibility(View.VISIBLE);
                            MediaPreview.setVisibility(View.VISIBLE);
                            MedRmvBtn.setVisibility(View.VISIBLE);
                        }else{
                            ImageRef = ImageRef.child(publication.Media);
                            ImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    ImageBitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                                    MediaPreviewIcon.setImageResource(R.drawable.ic_sondage_debut);
                                    MediaPreviewIcon.setVisibility(View.VISIBLE);
                                    MediaPreview.setImageBitmap(ImageBitmap);
                                    MediaPreview.setVisibility(View.VISIBLE);
                                    MedRmvBtn.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                    }
                }

            }
        }
        ImgUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaChoose(1);
            }
        });
        VidUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaChoose(2);
            }
        });
        PollLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaChoose(3);
            }
        });
        MedRmvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(Type){
                    case Publication.TYPE_IMAGE:
                            MediaPreview.setVisibility(View.GONE);
                            MediaPreview.setImageBitmap(null);
                            ImageBitmap = null;
                            MedRmvBtn.setVisibility(View.GONE);
                        break;
                    case Publication.TYPE_VIDEO:
                        MediaPreview.setVisibility(View.GONE);
                        MediaPreview.setOnClickListener(null);
                        Media = null;
                        ImageBitmap = null;
                        MedRmvBtn.setVisibility(View.GONE);
                        break;
                    case Publication.TYPE_SONDAGE:
                        MediaPreview.setVisibility(View.GONE);
                        MediaPreview.setImageBitmap(null);
                        ImageBitmap = null;
                        SondageLink = null;
                        ImgLinkSondage = null;
                        MedRmvBtn.setVisibility(View.GONE);
                        break;
                }
                MediaPreviewIcon.setVisibility(View.GONE);
                Instruct.setVisibility(View.GONE);
                Type = Publication.TYPE_TEXTE;
            }
        });
        DoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Initialisation...");
                progressDialog.show();
                TaskDone = 0;
                TaskTotal = 0;
                Thread mThread = new Thread(){
                    @Override
                    public void run(){
                        AddTask();
                        Publication newPublication = new Publication();
                        //Assignation
                        newPublication.Type = Type;
                        newPublication.SondageRef = SondageLink==null?"N":SondageLink;
                        newPublication.Titre = TitreField.getEditableText().toString();
                        newPublication.Texte = ContentField.getEditableText().toString();
                        newPublication.AuteurRef = user.getUid(); //user ne sera pas null, car il faut être connecté pour acceder à cette page;
                        newPublication.date_public = Calendar.getInstance().getTime().getTime();
                        if(Si_Edit){
                            newPublication.ID = publication.ID;
                        }
                        //validation
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setTitle("Validation...");
                            }
                        });
                        boolean Valid = true;
                        if(newPublication.Titre.isEmpty()){
                            Valid = false;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TitreErr.setText(R.string.Gen_Empty_Field);
                                    TitreErr.setVisibility(View.VISIBLE);
                                }
                            });

                        }else{
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TitreErr.setVisibility(View.INVISIBLE);
                                }
                            });

                        }
                        if(newPublication.Texte.isEmpty()){
                            Valid = false;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    ContentErr.setText(R.string.Gen_Empty_Field);
                                    ContentErr.setVisibility(View.VISIBLE);
                                }
                            });
                        }else{
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    ContentErr.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                        /*Type et image
                         * le Type ne change que si le media est validé*/
                        //Enregistrement
                        if(Valid){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setTitle("Envoi des données...");
                                }
                            });
                            DatabaseReference PublicationRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Publications_Keys.STRUCT_NAME);
                            if(Si_Edit){
                                newPublication.ID = publication.ID;
                            }else{
                                newPublication.ID = PublicationRef.push().getKey();
                            }
                            PublicationRef = PublicationRef.child(newPublication.ID);
                            //le média
                            if(Type != Publication.TYPE_TEXTE){

                                StorageReference ImgRef = FirebaseStorage.getInstance().getReference();
                                if(Type == Publication.TYPE_IMAGE){//on upload l'image
                                    //clear vid et vidthumb
                                    if(publication != null){//il est sur le serveur, car on l'édite
                                        final StorageReference VidRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_VIDEOS).child(publication.ID+".jpg");
                                        VidRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {//il existe on supprime, sinon, rien;
                                                VidRef.delete();
                                                FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_VIDEOS_THUMBNAILS).child(publication.ID+".jpg").delete();
                                            }
                                        });
                                    }
                                    newPublication.Media = FireBaseInteraction.Storage_Paths.PUBLICATION_IMAGES + newPublication.ID + ".jpg";
                                    if(mediachange) {
                                        ImgRef = ImgRef.child(newPublication.Media);
                                        UploadFile(ImgRef, ImageBitmap, "Envoi de " + newPublication.ID + ".jpg");
                                        ImgRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_IMAGES_THUMBNAILS).child(newPublication.ID+".jpg");
                                        UploadFile(ImgRef, TraitementImage.CreateThumbnail(ImageBitmap, getActivity(),512), "Envoi de " + newPublication.ID + ".jpg (miniature)");
                                    }
                                }
                                if(Type == Publication.TYPE_VIDEO){
                                    if(publication != null){//il est sur le serveur, car on l'édite
                                        final StorageReference ImgRefDel = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_IMAGES).child(publication.ID+".jpg");
                                        ImgRefDel.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {//il existe on supprime, sinon, rien;
                                                ImgRefDel.delete();
                                                FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_IMAGES_THUMBNAILS).child(publication.ID+".jpg").delete();
                                            }
                                        });
                                    }
                                    if(mediachange) {
                                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                                    String Extension = mime.getExtensionFromMimeType(getActivity().getContentResolver().getType(Media));
                                    newPublication.Media = FireBaseInteraction.Storage_Paths.PUBLICATION_VIDEOS + newPublication.ID + "." + Extension;
                                        ImgRef = ImgRef.child(newPublication.Media);
                                        UploadFile(ImgRef, Media, "Envoi de " + newPublication.ID + "." + Extension);
                                        ImgRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_VIDEOS_THUMBNAILS).child(newPublication.ID + ".jpg");
                                        UploadFile(ImgRef, ImageBitmap, "Envoi de " + newPublication.ID + "." + Extension + " (miniature)");
                                    }else{
                                        newPublication.Media = publication.Media;
                                    }
                                }
                                if(Type == Publication.TYPE_SONDAGE){
                                    if(publication != null){//il est sur le serveur, car on l'édite
                                        final StorageReference ImgRefDel = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_IMAGES).child(publication.ID+".jpg");
                                        ImgRefDel.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {//il existe on supprime, sinon, rien;
                                                ImgRefDel.delete();
                                                FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_IMAGES_THUMBNAILS).child(publication.ID+".jpg").delete();
                                            }
                                        });
                                        final StorageReference VidRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_VIDEOS).child(publication.ID+".mp4");
                                        VidRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {//il existe on supprime, sinon, rien;
                                                VidRef.delete();
                                                FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_VIDEOS_THUMBNAILS).child(publication.ID+".jpg").delete();
                                            }
                                        });
                                    }
                                    newPublication.Media = ImgLinkSondage;
                                }
                            }else{
                                if(publication != null){//il est sur le serveur, car on l'édite
                                    final StorageReference ImgRefDel = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_IMAGES).child(publication.ID+".jpg");
                                    ImgRefDel.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {//il existe on supprime, sinon, rien;
                                            ImgRefDel.delete();
                                            FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_IMAGES_THUMBNAILS).child(publication.ID+".jpg").delete();
                                        }
                                    });
                                    final StorageReference VidRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_VIDEOS).child(publication.ID+".mp4");
                                    VidRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {//il existe on supprime, sinon, rien;
                                            VidRef.delete();
                                            FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PUBLICATION_VIDEOS_THUMBNAILS).child(publication.ID+".jpg").delete();
                                        }
                                    });
                                }
                                newPublication.Media = "N";
                            }

                            Map<String, Object> PublicMap = newPublication.toMap();
                            PublicationRef.setValue(PublicMap);
                            PublishPost(newPublication);
                            TaskDone();
                        }else{
                            progressDialog.dismiss();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "Erreurs détectés, veuillez les corriger", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                };
                mThread.run();

            }
        });
        return view;
    }
    private void AddTask(){
        TaskTotal++;
    }
    private void TaskDone(){
        TaskDone++;
        if(TaskTotal == TaskDone){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    mListener.changePage(PublicationListFragment.newInstance(true, null));
                }
            });
        }
    }
    private void PublishPost(Publication P) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("AuteurRef", user.getUid());
        data.put("UserName", user.getDisplayName());
        data.put("ID", P.ID);

         mFunctions.getHttpsCallable("publishPost").call(data);
    }
    private void UploadFile(StorageReference storageReference, Uri File, final String FlavorText){
        AddTask();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitle(FlavorText);
            }
        });
        StorageReference ref = storageReference;
        ref.putFile(File)
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
        AddTask();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitle(FlavorText);
            }
        });
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = storageReference;
        ref.putBytes(data)
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
    private void MediaChoose(int option){
        switch (option){
            case Publication.TYPE_IMAGE://image
                MediaPreview.setOnClickListener(null);
                typeTempo = Publication.TYPE_IMAGE;
                Intent i = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, MEDIA_GALL_OPTION);
                SondageLink = null;
                break;
            case Publication.TYPE_VIDEO://video
                typeTempo = Publication.TYPE_VIDEO;
                Intent i2 = new Intent(Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i2, MEDIA_GALL_OPTION);
                SondageLink = null;
                break;
            case Publication.TYPE_SONDAGE://sondage
                MediaPreview.setOnClickListener(null); //pas vraiment besoin de changer de page pour le moment.
                typeTempo = Publication.TYPE_SONDAGE;
                String[] StringArray = new String[ListeSondagesNom.size()];
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle("Quel est le sondage que vous voulez inclure en lien?");
                b.setItems(ListeSondagesNom.toArray(StringArray), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SondageLink = ListeSondagesID.get(i);
                        GetImage(SondageLink);
                        dialogInterface.dismiss();
                    }
                });
                b.show();
                break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == MEDIA_GALL_OPTION && resultCode == Activity.RESULT_OK){

            if(typeTempo == Publication.TYPE_IMAGE){
                Media = data.getData();
                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST_READ_STORAGE);
                }else{
                    MakeImage();
                }
            }
            if(typeTempo == Publication.TYPE_VIDEO){

                Media = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(data.getData(), filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                ImageBitmap = ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.MINI_KIND);
                MediaPreview.setImageBitmap(ImageBitmap);
                MediaPreviewIcon.setImageResource(R.drawable.ic_play_circle_color);
                MediaPreviewIcon.setVisibility(View.VISIBLE);
                MediaPreview.setVisibility(View.VISIBLE);
                MedRmvBtn.setVisibility(View.VISIBLE);
                MedRmvBtn.setText(R.string.Post_Form_Media_Remove_Vid);
                Instruct.setText(R.string.Post_Elem_Instruct_Vid);
                Instruct.setVisibility(View.VISIBLE);
                MediaPreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), VideoPopupActivity.class);
                        intent.putExtra("Path", Media.toString());
                        intent.putExtra("NotOnServer", true);
                        getActivity().startActivity(intent);
                    }
                });
                mediachange = true;
                Type = typeTempo;
            }


            Toast.makeText(getActivity(), "Le fichier sera envoyé au serveur lorsque le sondage sera créé/modifié",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_PERMISSION_REQUEST_READ_STORAGE){
            if(grantResults.length == 0){
                try {
                    ImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Media);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Media = null;
                mediachange = true;
                Instruct.setText(R.string.Post_Elem_Instruct_Img);
                Instruct.setVisibility(View.VISIBLE);
                MediaPreviewIcon.setImageResource(R.drawable.ic_loupe_black_24dp);
                MediaPreviewIcon.setVisibility(View.VISIBLE);
                MedRmvBtn.setText(R.string.Post_Form_Media_Remove_Img);
                MedRmvBtn.setVisibility(View.VISIBLE);
                MediaPreview.setImageBitmap(ImageBitmap);
                MediaPreview.setVisibility(View.VISIBLE);
                MediaPreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.setContentView(R.layout.image_dialog);
                        dialog.show();
                        final ImageView Image = dialog.findViewById(R.id.image_dialog_imageview);
                        final ProgressBar progressBar = dialog.findViewById(R.id.image_dialog_progressBar);
                        Image.setImageBitmap(ImageBitmap);
                        progressBar.setVisibility(View.GONE);
                        Image.setVisibility(View.VISIBLE);
                        final Button CloseBtn = dialog.findViewById(R.id.ImageCloseBtn);
                        CloseBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                Type = typeTempo;
            }else{//accepte
                MakeImage();

            }
        }
    }
    private void MakeImage(){
        ImageBitmap = TraitementImage.RotateImage(Media, getActivity());
        Media = null;
        mediachange = true;
        Instruct.setText(R.string.Post_Elem_Instruct_Img);
        Instruct.setVisibility(View.VISIBLE);
        MediaPreview.setImageBitmap(ImageBitmap);
        MediaPreviewIcon.setImageResource(R.drawable.ic_loupe_black_24dp);
        MedRmvBtn.setText(R.string.Post_Form_Media_Remove_Img);
        MedRmvBtn.setVisibility(View.VISIBLE);
        MediaPreviewIcon.setVisibility(View.VISIBLE);
        MediaPreview.setVisibility(View.VISIBLE);
        MediaPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.image_dialog);
                dialog.show();
                final ImageView Image = dialog.findViewById(R.id.image_dialog_imageview);
                final ProgressBar progressBar = dialog.findViewById(R.id.image_dialog_progressBar);
                Image.setImageBitmap(ImageBitmap);
                progressBar.setVisibility(View.GONE);
                Image.setVisibility(View.VISIBLE);
                final Button CloseBtn = dialog.findViewById(R.id.ImageCloseBtn);
                CloseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });
        MedRmvBtn.setVisibility(View.VISIBLE);
        Type = typeTempo;
    }
    private void GetImage(final String SondageID){
        DoneBtn.setEnabled(false);
        DatabaseReference SondageImgLink = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME).child(SondageID).child(FireBaseInteraction.Sondage_Keys.CHEMIN_IMAGE);
        SondageImgLink.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String result = (String)dataSnapshot.getValue();
                if(result.equals("N")){
                    Instruct.setText(R.string.Post_Elem_Instruct_Poll_Disabled);
                    Instruct.setVisibility(View.VISIBLE);
                    ImgLinkSondage = "N";
                    MediaPreview.setImageResource(R.mipmap.ic_launcher);
                    MediaPreviewIcon.setImageResource(R.drawable.ic_sondage_debut);
                    MediaPreviewIcon.setVisibility(View.VISIBLE);
                    MediaPreview.setVisibility(View.VISIBLE);
                    MedRmvBtn.setVisibility(View.VISIBLE);
                    MedRmvBtn.setText(R.string.Post_Form_Media_Remove_Poll);
                }else{
                    ImgLinkSondage = FireBaseInteraction.Storage_Paths.SONDAGES_IMAGES_THUMBNAILS + SondageID + ".jpg";
                    StorageReference ImgSondageGet = FirebaseStorage.getInstance().getReference().child(ImgLinkSondage);
                    ImgSondageGet.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Instruct.setText(R.string.Post_Elem_Instruct_Poll_Disabled);
                            Instruct.setVisibility(View.VISIBLE);
                            Bitmap Image = BitmapFactory.decodeByteArray(bytes,0, bytes.length);//pas besoin de imagebitmap, car il existe déja sur le serveur
                            MediaPreview.setImageBitmap(Image);
                            MediaPreviewIcon.setImageResource(R.drawable.ic_sondage_debut);
                            MediaPreviewIcon.setVisibility(View.VISIBLE);
                            MediaPreview.setVisibility(View.VISIBLE);
                            MedRmvBtn.setVisibility(View.VISIBLE);
                            MedRmvBtn.setText(R.string.Post_Form_Media_Remove_Poll);
                        }
                    });
                }

                Type = typeTempo;
                DoneBtn.setEnabled(true);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                DoneBtn.setEnabled(true);
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
        // TODO: Update argument type and name
        void changePage(Fragment fragment);
        void ChangeTitle(String newTitle);
    }
}

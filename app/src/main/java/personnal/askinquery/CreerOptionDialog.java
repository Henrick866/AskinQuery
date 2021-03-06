package personnal.askinquery;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreerOptionDialog.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreerOptionDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreerOptionDialog extends DialogFragment implements CreerOptionAdapter.AdaptListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int MEDIA_GALL_OPTION = 3;
    private static final int MY_PERMISSION_REQUEST_READ_STORAGE = 200;
    private Question mParam1;
    ListView OptionListe;
    TextView OptionsError;
    ImageButton btnAjout;
    Button Cancel;
    Button Done;
    Uri Image;
    Bitmap ImageBitmap;
    int optionPosition;
    CreerOptionAdapter creerOptionAdapter;
    int QuestionPosition, TaskTodo, TaskDone;
    public CreerOptionDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreerOptionDialog.
     */
    public static CreerOptionDialog newInstance(Question question, int Position) {
        CreerOptionDialog fragment = new CreerOptionDialog();
        Bundle args = new Bundle();

        args.putSerializable(ARG_PARAM1, question);
        args.putInt(ARG_PARAM2, Position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = (Question)getArguments().getSerializable(ARG_PARAM1);
            QuestionPosition = getArguments().getInt(ARG_PARAM2);
        }
    }


    @Override
    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((int) (size.x * 0.95), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        // Call super onResume after sizing
        super.onResume();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_creer_option_dialog, container, false);

        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        OptionListe = view.findViewById(R.id.sondage_edit_question_option_liste);
        OptionsError = view.findViewById(R.id.option_edit_error);
        btnAjout = view.findViewById(R.id.sondage_edit_question_add_option);
        Done = view.findViewById(R.id.option_edit_done_btn);
        Cancel = view.findViewById(R.id.option_edit_cancel_btn);

        creerOptionAdapter = new CreerOptionAdapter(getActivity(), mParam1.Options, mParam1.Type_Question, this);
        if(mParam1.Type_Question != Question.TYPE_TEXTE){
            DownloadImagesList();
        }
        OptionListe.setAdapter(creerOptionAdapter);
        btnAjout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Option o = new Option();
                o.Score = 0;
                o.Chemin_Video="N";
                o.Chemin_Image="N";
                o.notOnServer = true;
                o.ID = String.valueOf(mParam1.Options.size());
                o.Question_parent = mParam1;
                mParam1.Options.add(o);
                creerOptionAdapter.notifyDataSetChanged();
            }
        });
        Done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                String isDone = CreerQuestionAdapter.DialogFunctions.onFinishedOptionDialog(mParam1.Options, QuestionPosition);
                OptionListe.setAdapter(null);
                dismiss();
            }
        });
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
    private void AddTask(){
        TaskTodo++;
    }
    private void TaskDone(){
        TaskDone++;
        if(TaskDone == TaskTodo){
                    creerOptionAdapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
    public void LoadMedia(int optionPosition, int type){
        if(type == Question.TYPE_IMAGE) {
            this.optionPosition = optionPosition;
            Intent i = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, MEDIA_GALL_OPTION);
        }
        if(type == Question.TYPE_VIDEO) {
            this.optionPosition = optionPosition;
            Intent i = new Intent(Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, MEDIA_GALL_OPTION);
        }
    }
    public void updateOptions(ArrayList<Option> lo){
        mParam1.Options = lo;
    }
    public void DeleteOption(int optionPosition) {
        if(!mParam1.Options.get(optionPosition).notOnServer) { //si il est sur le serveur, marque-le pour la suppresion sur le serveur
            mParam1.Options.get(optionPosition).toBeDeleted = true;
        }else {
            mParam1.Options.remove(optionPosition);
        }
        creerOptionAdapter.notifyDataSetChanged();
        if (mParam1.Options.size() < 2) {
            OptionsError.setText(R.string.Create_Question_Option_Min_Err);
            OptionsError.setVisibility(View.VISIBLE);
        } else {
            OptionsError.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == MEDIA_GALL_OPTION && resultCode == Activity.RESULT_OK){
            Image = data.getData();
            if(mParam1.Options.get(this.optionPosition).Question_parent.Type_Question == Question.TYPE_IMAGE){
                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST_READ_STORAGE);
                }else{
                    MakeImage();
                }
            }
            if(mParam1.Options.get(this.optionPosition).Question_parent.Type_Question == Question.TYPE_VIDEO){

                    mParam1.Options.get(this.optionPosition).UriVideo = data.getData().toString();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(data.getData(), filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                mParam1.Options.get(this.optionPosition).ImagePreload = ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.MINI_KIND);
                    //création bitmap + assignation bitmap;
                creerOptionAdapter.notifyDataSetChanged();
                mParam1.Options.get(this.optionPosition).DataChanged = true;
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
                    mParam1.Options.get(this.optionPosition).ImagePreload = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Image);
                    mParam1.Options.get(this.optionPosition).Chemin_Media = "N";
                    creerOptionAdapter.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Image = null;
            }else{//accepte
                MakeImage();

            }
        }
    }
    private void DownloadImagesList(){ // charge les images/thumbnails une fois lors de l'initialisation de la liste.
        //si imagepreload = null -> si opt.notonserv = false
        StorageReference OptionMedRef;
        TaskDone = 0;
        TaskTodo = 0;
        AddTask();
        for(int i = 0; i < mParam1.Options.size(); i++){
            Option o = mParam1.Options.get(i);
            final int index = i;
            if(o.ImagePreload == null){
                if(!o.notOnServer){ //si il est sur le serveur et que l'image == null ET que le type est bon (condition avant l'appel de la methode).
                    if(mParam1.Type_Question == Question.TYPE_IMAGE) {
                        OptionMedRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES_THUMBNAILS).child(o.ID+".jpg");
                    }else{//le type TEXTE est déjà préfiltré lors de l'appel
                        OptionMedRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS_THUMBNAILS).child(o.ID+".jpg");
                    }
                    Log.e("Test", "Chargement");
                    AddTask();
                    OptionMedRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            mParam1.Options.get(index).ImagePreload = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            TaskDone();
                        }
                    });
                }//si il n'st pas sur le serveur, il est local, deux options, l'image a été choisi donc un bitmap existe, sinon il ne l'est pas;


            }
        }
        TaskDone();

    }
    interface OnFragmentInteractionListener{}
    private void MakeImage(){

        ImageBitmap = TraitementImage.RotateImage(Image, getActivity());
        mParam1.Options.get(this.optionPosition).Chemin_Media = "N";
        mParam1.Options.get(this.optionPosition).ImagePreload = TraitementImage.CreateThumbnail(ImageBitmap, getActivity(), 512);
        mParam1.Options.get(this.optionPosition).ImageFull = ImageBitmap;
        Image = null;
        ImageBitmap = null;
        mParam1.Options.get(this.optionPosition).DataChanged = true;
        creerOptionAdapter.notifyDataSetChanged();
    }
}

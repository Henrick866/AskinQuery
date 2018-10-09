package personnal.askinquery;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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

import java.io.Serializable;
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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private static final int MEDIA_GALL_OPTION = 3;

    // TODO: Rename and change types of parameters
    private Question mParam1;
    ListView OptionListe;
    TextView OptionsError;
    ImageButton btnAjout;
    Button Cancel;
    Button Done;
    int optionPosition;
    CreerOptionAdapter creerOptionAdapter;
    private OnFragmentInteractionListener mListener;
    OptionDialogListener optionDialogListener;
    int QuestionPosition;
    public CreerOptionDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreerOptionDialog.
     */
    // TODO: Rename and change types and number of parameters
    public static CreerOptionDialog newInstance(Question question, int Position, CreerQuestionAdapter creerQuestionAdapter) {
        CreerOptionDialog fragment = new CreerOptionDialog();
        Bundle args = new Bundle();

        args.putSerializable(ARG_PARAM1, question);
        args.putInt(ARG_PARAM2, Position);
        args.putSerializable(ARG_PARAM3, creerQuestionAdapter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = (Question)getArguments().getSerializable(ARG_PARAM1);
            optionDialogListener = (CreerQuestionAdapter)getArguments().getSerializable(ARG_PARAM3);
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
        creerOptionAdapter = new CreerOptionAdapter(getContext(), mParam1.Options, mParam1.Type_Question, this);
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
                optionDialogListener.onFinishedOptionDialog(mParam1.Options, QuestionPosition);
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
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            //mListener.onFragmentInteraction(uri);
        }
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
        }
        mParam1.Options.remove(optionPosition);
        creerOptionAdapter.notifyDataSetChanged();
        if (mParam1.Options.size() < 2) {
            OptionsError.setText("Vous devez avoir au moins deux choix de réponses");
            OptionsError.setVisibility(View.VISIBLE);
        } else {
            OptionsError.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == MEDIA_GALL_OPTION && resultCode == Activity.RESULT_OK){

            if(mParam1.Options.get(this.optionPosition).Question_parent.Type_Question == Question.TYPE_IMAGE){
                mParam1.Options.get(this.optionPosition).Image = data.getData();
            }
            if(mParam1.Options.get(this.optionPosition).Question_parent.Type_Question == Question.TYPE_VIDEO){
                mParam1.Options.get(this.optionPosition).Video = data.getData();
            }

            creerOptionAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Le fichier sera envoyé au serveur lorsque le sondage sera créé/modifié",Toast.LENGTH_LONG);
        }
    }
    public void ToggleDoneBtn(boolean b){
        Done.setEnabled(b);
    }
    public interface OptionDialogListener{
        void onFinishedOptionDialog(ArrayList<Option> liste_option, int Position);
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
    }
}

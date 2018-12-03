package personnal.askinquery;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DialogPlainteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DialogPlainteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialogPlainteFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String CibleID;
    private String Type;

    private EditText PlainteField;
    private TextView PlainteErr;
    public DialogPlainteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DialogPlainteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DialogPlainteFragment newInstance(String CibleID, String Type) {
        DialogPlainteFragment fragment = new DialogPlainteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, CibleID);
        args.putString(ARG_PARAM2, Type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            CibleID = getArguments().getString(ARG_PARAM1);
            Type = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_plainte, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button BtnConfirm, BtnCancel;
        PlainteField = view.findViewById(R.id.plainte_field);
        BtnConfirm = view.findViewById(R.id.plainte_confirm);
        BtnCancel = view.findViewById(R.id.plainte_cancel);
        PlainteErr = view.findViewById(R.id.plainte_error);
        BtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PlainteField.getEditableText().toString().isEmpty()){
                    PlainteErr.setVisibility(View.VISIBLE);
                }else{
                    PlainteErr.setVisibility(View.INVISIBLE);
                    Plainte p = new Plainte(PlainteField.getEditableText().toString(), CibleID, Type, Calendar.getInstance().getTimeInMillis());
                    Map<String, Object> map = p.toMap();
                    DatabaseReference PlainteRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Plainte_Keys.STRUCT_NAME);
                    p.ID = PlainteRef.push().getKey();
                    PlainteRef.child(p.ID).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), "Merci d'avoir porté plainte, notre équipe se chargera de la suite", Toast.LENGTH_LONG).show();
                                dismiss();
                            }else{
                                Toast.makeText(getActivity(), "Erreur, plainte non envoyée.",Toast.LENGTH_LONG).show();
                                dismiss();
                            }
                        }
                    });
                }
            }
        });
        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
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
    public interface OnFragmentInteractionListener {
    }
}

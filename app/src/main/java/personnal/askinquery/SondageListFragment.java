package personnal.askinquery;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SondageListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SondageListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SondageListFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private boolean mParam1;
    private boolean mParam2;
    Profil AuteurTEST;
    boolean Loading_DONE;
    ArrayList<Sondage> SondageList;
    private OnFragmentInteractionListener mListener;

    public SondageListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SondageListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SondageListFragment newInstance(boolean Admin) {
        SondageListFragment fragment = new SondageListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, Admin);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getBoolean(ARG_PARAM1);
        }
        Loading_DONE = false;
        SondageList = new ArrayList<>();

        ValueEventListener SondageListListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Sondage S = dataSnapshot1.getValue(Sondage.class);
                    S.ID = dataSnapshot1.getKey();
                    SondageList.add(S);
                    //À supprimer

                }
                SondageAdapter adapter = new SondageAdapter(getActivity(), SondageList, mParam1);
                setListAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        DatabaseReference sondageRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME);
        sondageRef.addListenerForSingleValueEvent(SondageListListener);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sondage_list, container, false);
        final Button btn_Ajout = (Button)view.findViewById(R.id.sondage_ajout_btn);

        ValueEventListener AuteurTestListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AuteurTEST = dataSnapshot.getValue(Profil.class);
                AuteurTEST.ID = dataSnapshot.getKey();
                btn_Ajout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.changePage(CreerSondageFragment.newInstance(true, false, AuteurTEST, null));

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        DatabaseReference AuteurRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child("Profil1");
        AuteurRef.addListenerForSingleValueEvent(AuteurTestListener);
        if(mParam1 == false){
            btn_Ajout.setVisibility(View.GONE);
        }
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event


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

    }
}

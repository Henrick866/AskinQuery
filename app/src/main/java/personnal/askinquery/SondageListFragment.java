package personnal.askinquery;

import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bumptech.glide.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    // TODO: Rename and change types of parameters
    private static final String mParam1 = "Si_Gestion_Sond";
    Profil Utilisateur;
    boolean Loading_DONE, Si_Gestion_Sond;
    ArrayList<Sondage> SondageList;
    private OnFragmentInteractionListener mListener;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ProgressDialog progressDialog;
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
    public static SondageListFragment newInstance(boolean si_Gestion_Sond) {
        SondageListFragment fragment = new SondageListFragment();
        Bundle args = new Bundle();
        args.putBoolean(mParam1, si_Gestion_Sond);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Si_Gestion_Sond = getArguments().getBoolean(mParam1);
        }
        Loading_DONE = false;
        SondageList = new ArrayList<>();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Chargement des sondages...");
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null && Si_Gestion_Sond) {
            Utilisateur = new Profil(user.getUid(), user.getEmail(), user.getPhotoUrl().toString());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sondage_list, container, false);
        final Button btn_Ajout = (Button)view.findViewById(R.id.sondage_ajout_btn);


        if(!Si_Gestion_Sond){
            btn_Ajout.setVisibility(View.GONE);
        }else{

            btn_Ajout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     mListener.changePage(CreerSondageFragment.newInstance(true, false, Utilisateur, null));
                }
            });
        }
        progressDialog.show();
        ValueEventListener SondageListListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Sondage S = dataSnapshot1.getValue(Sondage.class);
                    if(Si_Gestion_Sond){ //si on gere les sondages, on ne retire que ceux qui nous intéresse
                        if(S.AuteurRef == user.getUid()){
                            S.ID = dataSnapshot1.getKey();
                            SondageList.add(S);
                        }
                    }else {//sinon on prend tout
                        S.ID = dataSnapshot1.getKey();
                        SondageList.add(S);
                    }
                    //À supprimer
                }
                SondageAdapter adapter = new SondageAdapter(getActivity(), SondageList, Si_Gestion_Sond);
                setListAdapter(adapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        DatabaseReference sondageRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME);
        sondageRef.addListenerForSingleValueEvent(SondageListListener);
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

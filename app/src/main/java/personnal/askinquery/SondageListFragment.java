package personnal.askinquery;

import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SondageListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SondageListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SondageListFragment extends ListFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String mParam1 = "Si_Gestion_Sond";
    private static final String mParam2 = "Filter";
    private static final String mParam3 = "Answered";
    private ProgressBar progressBar;
    private TextView Empty;
    Profil Utilisateur;
    private String Filter;
    boolean Answered;
    boolean Loading_DONE, Si_Gestion_Sond;
    ArrayList<Sondage> SondageList;
    private OnFragmentInteractionListener mListener;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ProgressDialog progressDialog;
    private FirebaseMultiQuery firebaseMultiQuery;
    public SondageListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SondageListFragment.
     */
    public static SondageListFragment newInstance(boolean si_Gestion_Sond,@Nullable String Filter) {
        SondageListFragment fragment = new SondageListFragment();
        Bundle args = new Bundle();
        args.putBoolean(mParam1, si_Gestion_Sond);
        args.putString(mParam2, Filter);
        args.getBoolean(mParam3, false);
        fragment.setArguments(args);

        return fragment;
    }
    public static SondageListFragment newInstance(boolean si_Gestion_Sond,@Nullable String Filter, boolean Answered) {
        SondageListFragment fragment = new SondageListFragment();
        Bundle args = new Bundle();
        args.putBoolean(mParam1, si_Gestion_Sond);
        args.putString(mParam2, Filter);
        args.putBoolean(mParam3, Answered);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Si_Gestion_Sond = getArguments().getBoolean(mParam1);
            Filter = getArguments().getString(mParam2);
            Answered = getArguments().getBoolean(mParam3);
        }
        Loading_DONE = false;

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Chargement des sondages...");
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null && Si_Gestion_Sond) {
            String Avatar;
            if(user.getPhotoUrl() == null){
                Avatar = "N";
            }else{
                Avatar = user.getPhotoUrl().toString();
            }
            Utilisateur = new Profil(user.getDisplayName(), user.getEmail(), Avatar);
            Utilisateur.ID = user.getUid();
        }

    }
    @Override
    public void onStart(){
        super.onStart();
        DatabaseReference DataRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME);
        Query query;
        if(Si_Gestion_Sond){ //si on gere les sondages, on ne retire que ceux qui nous intéresse
            query = DataRef.orderByChild(FireBaseInteraction.Sondage_Keys.AUTEUR_REF).equalTo(user.getUid());
            query.addListenerForSingleValueEvent(loadListeSimple);
        }else {
            if(Filter != null) {
                query = DataRef.orderByChild(FireBaseInteraction.Sondage_Keys.AUTEUR_REF).equalTo(Filter);
                query.addListenerForSingleValueEvent(loadListeSimple);
            }else{
                if(Answered){
                    Profil UtilConn = mListener.getUtilisateur_Connecte();
                    ArrayList<DatabaseReference> listdbref = new ArrayList<>();
                    for(HashMap.Entry<String, Object> cursor : UtilConn.Sondages_Faits.entrySet()) {
                        listdbref.add(DataRef.child(cursor.getKey()));
                    }
                    firebaseMultiQuery = new FirebaseMultiQuery(listdbref);
                    final Task<Map<DatabaseReference, DataSnapshot>> allLoad = firebaseMultiQuery.start();
                    allLoad.addOnCompleteListener(getActivity(), new AllOnCompleteListener());

                }else {//tout
                    query = DataRef.orderByChild(FireBaseInteraction.Sondage_Keys.PUBLIED).equalTo(true);
                    query.addListenerForSingleValueEvent(loadListeSimple);
                }
            }
        }
    }
    private ValueEventListener loadListeSimple = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<Sondage> SondageList = new ArrayList<>();
            for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                Sondage S = dataSnapshot1.getValue(Sondage.class);
                if(S.Publied) {
                    S.ID = dataSnapshot1.getKey();
                    SondageList.add(S);
                }
            }
            if(SondageList.isEmpty()){
                Empty.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
            SondageAdapter adapter = new SondageAdapter(getActivity(), SondageList, Si_Gestion_Sond, Filter!=null);
            setListAdapter(adapter);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private class AllOnCompleteListener implements OnCompleteListener<Map<DatabaseReference, DataSnapshot>> {
        @Override
        public void onComplete(@NonNull Task<Map<DatabaseReference, DataSnapshot>> task) {
            ArrayList<Sondage> SondageList = new ArrayList<>();
            if (task.isSuccessful()) {
                final Map<DatabaseReference, DataSnapshot> result = task.getResult();
                for(Map.Entry<DatabaseReference, DataSnapshot> cursor : result.entrySet()) {

                    Sondage S = cursor.getValue().getValue(Sondage.class);
                    if(S.Publied) {
                        S.ID = cursor.getValue().getKey();
                        SondageList.add(S);
                    }
                }
                // Look up DataSnapshot objects using the same DatabaseReferences you passed into FirebaseMultiQuery
            }
            else {
                // log the error or whatever you need to do
            }
            // Do stuff with views
            if(SondageList.isEmpty()){
                Empty.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
            SondageAdapter adapter = new SondageAdapter(getActivity(), SondageList, Si_Gestion_Sond, Filter!=null);
            setListAdapter(adapter);
        }
    }
    @Override
    public void onStop(){
        super.onStop();
        if(firebaseMultiQuery!=null) {
            firebaseMultiQuery.stop();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sondage_list, container, false);
        final Button btn_Ajout = view.findViewById(R.id.sondage_ajout_btn);

        progressBar = view.findViewById(R.id.sondage_list_loader);
        Empty = view.findViewById(R.id.sondage_list_empty);
        if(Si_Gestion_Sond){
            mListener.ChangeTitle("Sondages | Gestion");
        }else if(Filter!=null){
            FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(Filter).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Profil p = dataSnapshot.getValue(Profil.class);
                    mListener.ChangeTitle("Sondages | "+p.Username);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else if(Answered){
            mListener.ChangeTitle("Sondages | Répondus");
        }else{
            mListener.ChangeTitle("Sondages");
        }
        SondageList = new ArrayList<>();
        if(!Si_Gestion_Sond){
            btn_Ajout.setVisibility(View.GONE);
        }else{

            btn_Ajout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     mListener.changePage(CreerSondageFragment.newInstance(false, null));
                }
            });
        }
        //progressDialog.show();
        return view;
    }
    public void multiTasker(){
        Collection<Task<Void>> tasks;

// during onCreate:

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
        Profil getUtilisateur_Connecte();

    }
}

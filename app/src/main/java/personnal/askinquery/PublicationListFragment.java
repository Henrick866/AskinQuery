package personnal.askinquery;

import android.app.ListFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PublicationListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PublicationListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PublicationListFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private boolean Si_Gestion;
    private String Filtre;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseMultiQuery firebaseMultiQuery;
    private OnFragmentInteractionListener mListener;

    public PublicationListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PublicationListFragment.
     */
    // TODO: Rename and change types and number of parameters
    //needed: sigestion (oui, seulement afficher ceux créés par l'auteur, sinon toust ceux qu'on est abo), filtre (null ou l'auteur ciblé)
    public static PublicationListFragment newInstance(boolean Si_Gestion,@Nullable String filter) {
        PublicationListFragment fragment = new PublicationListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, Si_Gestion);
        args.putString(ARG_PARAM2, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Si_Gestion = getArguments().getBoolean(ARG_PARAM1);
            Filtre = getArguments().getString(ARG_PARAM2);
        }
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }
    @Override
    public void onStart(){
        super.onStart();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Publications_Keys.STRUCT_NAME);
        Query query;
        if(Filtre != null) {//si filtré
            query = databaseReference.orderByChild(FireBaseInteraction.Publications_Keys.AUTEUR).equalTo(Filtre);
            query.addListenerForSingleValueEvent(LoadPubs);
        }else{//sinon
            if(Si_Gestion){//si on gère
                query = databaseReference.orderByChild(FireBaseInteraction.Publications_Keys.AUTEUR).equalTo(user.getUid());
                query.addListenerForSingleValueEvent(LoadPubs);
            }else{//standard
                ArrayList<DatabaseReference> ListDataRef = new ArrayList<>();
                Profil p = mListener.getUtilisateur_Connecte();
                for(HashMap.Entry<String, String> cursor : p.Auteurs_Suivis.entrySet()) {
                    ListDataRef.add(databaseReference.orderByChild(FireBaseInteraction.Publications_Keys.AUTEUR).equalTo(cursor.getKey()).getRef());
                }
                ListDataRef.add(databaseReference.orderByChild(FireBaseInteraction.Publications_Keys.AUTEUR).equalTo(user.getUid()).getRef());
                firebaseMultiQuery = new FirebaseMultiQuery(ListDataRef);
                final Task<Map<DatabaseReference, DataSnapshot>> allLoad = firebaseMultiQuery.start();
                allLoad.addOnCompleteListener(getActivity(), new AllOnCompleteListener());
            }

        }

    }
    @Override
    public void onStop(){
        super.onStop();
        if(firebaseMultiQuery != null) {
            firebaseMultiQuery.stop();
        }

    }
    private ValueEventListener LoadPubs = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<Publication> PublicationList = new ArrayList<>();
            for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                Publication P = dataSnapshot1.getValue(Publication.class);
                P.ID = dataSnapshot1.getKey();
                P.Date_Public = new Date(P.date_public);
                PublicationList.add(P);
            }

            PublicationAdapter adapter = new PublicationAdapter(getActivity(), PublicationList, Si_Gestion);
            setListAdapter(adapter);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private class AllOnCompleteListener implements OnCompleteListener<Map<DatabaseReference, DataSnapshot>> {
        @Override
        public void onComplete(@NonNull Task<Map<DatabaseReference, DataSnapshot>> task) {
            ArrayList<Publication> PublicationList = new ArrayList<>();
            if (task.isSuccessful()) {
                final Map<DatabaseReference, DataSnapshot> result = task.getResult();

                for(Map.Entry<DatabaseReference, DataSnapshot> cursor : result.entrySet()) {//j'itère toutes les listes
                    DataSnapshot dataSnapshot1 = cursor.getValue();//une sous-liste
                    for(DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren()){
                        Publication P = dataSnapshot2.getValue(Publication.class);
                        P.ID = dataSnapshot2.getKey();
                        P.Date_Public = new Date(P.date_public);
                        PublicationList.add(P);
                    }
                }
                // Look up DataSnapshot objects using the same DatabaseReferences you passed into FirebaseMultiQuery
            }
            else {
                // log the error or whatever you need to do
            }
            // Do stuff with views
            PublicationAdapter adapter = new PublicationAdapter(getActivity(), PublicationList, Si_Gestion);
            setListAdapter(adapter);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(Si_Gestion){
            mListener.ChangeTitle("Publications | Gestion");
        }else if(Filtre != null){
            FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(Filtre).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Profil p = dataSnapshot.getValue(Profil.class);
                    mListener.ChangeTitle("Publications | "+p.Username);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            mListener.ChangeTitle("Publications");
        }
        View view = inflater.inflate(R.layout.fragment_publication_list, container, false);
        final Button AddBtn = view.findViewById(R.id.pub_list_add_btn);
        if(Si_Gestion){
            AddBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.changePage(CreatePostFragment.newInstance(false, null));
                }
            });

        }else{
            AddBtn.setVisibility(View.GONE);
        }


        return view;
    }

    public void LoadPubFiltre(){
        DatabaseReference PublicRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Publications_Keys.STRUCT_NAME);
        PublicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Publication> ListePubs = new ArrayList<>();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    String AuteurID = (String)dataSnapshot1.child(FireBaseInteraction.Publications_Keys.AUTEUR).getValue();
                    if(AuteurID == Filtre) {
                        Publication p = dataSnapshot1.getValue(Publication.class);
                        p.Date_Public = new Date(p.date_public);
                        p.ID = dataSnapshot1.getKey();
                        ListePubs.add(p);
                    }
                }
                PublicationAdapter publicationAdapter = new PublicationAdapter(getActivity(), ListePubs, Si_Gestion);
                setListAdapter(publicationAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void LoadGestionFiltre(){
        DatabaseReference PublicRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Publications_Keys.STRUCT_NAME);
        PublicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Publication> ListePubs = new ArrayList<>();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    String AuteurID = (String)dataSnapshot1.child(FireBaseInteraction.Publications_Keys.AUTEUR).getValue();
                    if(AuteurID.equals(user.getUid())) {
                        Publication p = dataSnapshot1.getValue(Publication.class);
                        p.Date_Public = new Date(p.date_public);
                        p.ID = dataSnapshot1.getKey();
                        ListePubs.add(p);
                    }
                }
                PublicationAdapter publicationAdapter = new PublicationAdapter(getActivity(), ListePubs, Si_Gestion);
                setListAdapter(publicationAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void LoadAllRelated(){
        //etape 1, charger la liste des abonnement
        final Profil p = mListener.getUtilisateur_Connecte();
                DatabaseReference PublicRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Publications_Keys.STRUCT_NAME);
                PublicRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<Publication> ListePubs = new ArrayList<>();
                        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                            String AuteurID = (String)dataSnapshot1.child(FireBaseInteraction.Publications_Keys.AUTEUR).getValue();
                            if(p.Auteurs_Suivis.containsKey(AuteurID) || AuteurID.equals(user.getUid())){
                                Publication p = dataSnapshot1.getValue(Publication.class);
                                p.ID = dataSnapshot1.getKey();
                                p.Date_Public = new Date(p.date_public);
                                ListePubs.add(p);
                            }
                        }
                        PublicationAdapter publicationAdapter = new PublicationAdapter(getActivity(), ListePubs, Si_Gestion);
                        setListAdapter(publicationAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
        Profil getUtilisateur_Connecte();
    }
}

package personnal.askinquery;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConsultProfilFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConsultProfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConsultProfilFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    private Profil mProfil, profilUser;
    private ImageView Avatar;
    private FloatingActionButton Fab;
    private TextView Username, ListSondages, ListPublications;
    private Button AbonnementBtn;
    private OnFragmentInteractionListener mListener;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference profilUserRef;

    public ConsultProfilFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConsultProfilFragment.
     */
    public static ConsultProfilFragment newInstance(Profil profil) {
        ConsultProfilFragment fragment = new ConsultProfilFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, profil);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mProfil = (Profil)getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_consult_profil, container, false);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        AbonnementBtn = view.findViewById(R.id.profil_check_abonnement);
        if(user != null){
            if(!user.isAnonymous()) {
                profilUserRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(user.getUid());
                profilUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        profilUser = dataSnapshot.getValue(Profil.class);
                        profilUser.ID = dataSnapshot.getKey();
                        HashMap<String, String> suivisList = new HashMap<>();
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.child(FireBaseInteraction.Profil_Keys.AUTEURS_SUIVIS).getChildren()) {
                            suivisList.put(dataSnapshot1.getKey(), dataSnapshot1.getValue().toString());
                        }
                        profilUser.Auteurs_Suivis = suivisList;
                        AbonnementBtn.setEnabled(true);
                        if (profilUser.Auteurs_Suivis.containsKey(mProfil.ID)) {
                            AbonnementBtn.setText("Se désabonner");
                        } else {
                            AbonnementBtn.setText("S'abonner");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }else{
                AbonnementBtn.setEnabled(false);
            }
        }else{
            AbonnementBtn.setEnabled(false);
        }
        Username = view.findViewById(R.id.profil_check_usernameText);
        Username.setText(mProfil.Username);
        Avatar = view.findViewById(R.id.profil_check_avatar);
        if(mProfil.Avatar.equals("N")){
            Avatar.setImageResource(R.mipmap.ic_launcher);
        }else{
            StorageReference AvatarRef = FirebaseStorage.getInstance().getReference().child(mProfil.Avatar);
            AvatarRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap Image = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                    Avatar.setImageBitmap(Image);
                }
            });
        }
        ListPublications = view.findViewById(R.id.profil_check_publications);
        //todo: ajouter la liste des publications
        ListSondages = view.findViewById(R.id.profil_check_sondages);
        ListSondages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.changePage(SondageListFragment.newInstance(false, mProfil.ID),"Sondages | "+mProfil.Username);
            }
        });


        AbonnementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo: gérer les notification par subscription
                if(user == null){
                    Toast.makeText(getActivity(), "Vous devez vous connecter pour vous abonner à ce créateur.", Toast.LENGTH_LONG).show();
                }else{
                    if(profilUser.Auteurs_Suivis.containsKey(mProfil.ID)){//Todo :: notification push susbcribe topic
                        profilUser.Auteurs_Suivis.remove(mProfil.ID);
                        profilUserRef.child(FireBaseInteraction.Profil_Keys.AUTEURS_SUIVIS).child(mProfil.ID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getActivity(), "Désabonnement confirmé", Toast.LENGTH_LONG).show();
                                    AbonnementBtn.setText("S'abonner");
                                }else{
                                    Toast.makeText(getActivity(), "Erreur, désabonnement échoué", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }else{
                        profilUser.Auteurs_Suivis.put(mProfil.ID, mProfil.Username);
                        profilUserRef.child(FireBaseInteraction.Profil_Keys.AUTEURS_SUIVIS).child(mProfil.ID).setValue(mProfil.Username).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getActivity(), "Abonnement confirmé", Toast.LENGTH_LONG).show();
                                    AbonnementBtn.setText("Se Désabonner");

                                }else{
                                    Toast.makeText(getActivity(), "Erreur, Abonnement échoué", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                }
            }
        });

        return view;
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
        void changePage(Fragment fragment, String Title);
    }
}

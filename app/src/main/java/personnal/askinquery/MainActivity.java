package personnal.askinquery;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SondageListFragment.OnFragmentInteractionListener,
        CreerSondageFragment.OnFragmentInteractionListener,
        CreerQuestionAdapter.AdaptListener,
        SondageAdapter.AdaptListener,
        LoginFragment.OnFragmentInteractionListener,
        CreerProfilFragment.OnFragmentInteractionListener,
        ManageProfilFragment.OnFragmentInteractionListener,
        ConsultProfilFragment.OnFragmentInteractionListener,
        AnswerOptionAdapter.AdaptListener,
        AnswerSondageFragment.OnFragmentInteractionListener,
        PublicationListFragment.OnFragmentInteractionListener,
        PublicationAdapter.AdaptListener,
        CreatePostFragment.OnFragmentInteractionListener
{
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    private FirebaseAuth mAuth;
    private DrawerLayout drawer;
    private TextView MenuUsername, MenuUserEmail, ConnectLink;
    private ImageView MenuUserAvatar;

    private Boolean ActivitySaved, FragmentLoaded;

    private MenuItem Connected, NotConnected;

    private Profil Utilisateur_Connecte;
    private FirebaseUser user;
    private Fragment fragmentToLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        FragmentLoaded = false;
        NavigationView navigationView;
        navigationView = findViewById(R.id.nav_view);
        MenuUserAvatar = navigationView.getHeaderView(0).findViewById(R.id.menu_user_avatar);
        MenuUserEmail = navigationView.getHeaderView(0).findViewById(R.id.menu_user_email);
        MenuUsername = navigationView.getHeaderView(0).findViewById(R.id.menu_user_username);
        ConnectLink = navigationView.getHeaderView(0).findViewById(R.id.menu_loginoff);
        Menu menu = navigationView.getMenu();
        ConnectLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logInOut();
            }
        });
        NotConnected = menu.findItem(R.id.NotConnected);
        Connected = menu.findItem(R.id.Connected);
        drawer = findViewById(R.id.drawer_layout);
        navigationView.setNavigationItemSelectedListener(this);
        fragmentManager = getFragmentManager();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        updateMenu(user);



        //changePage(BlankFragment.newInstance("",""));
    }
    @Override
    protected void onStart(){
        super.onStart();
        ActivitySaved = false;
        if(!FragmentLoaded){
            ActivityInitFragment();
        }
    }
    private void ActivityInitFragment(){
        Intent intent = getIntent();
        if(intent != null){//si on a recu une notification, on est connecté.
            if(intent.getStringExtra("FragmentName")!=null) {
                if (intent.getStringExtra("FragmentName").equals("NewPoll")) {
                    String IdSondage = intent.getStringExtra("SondageID");
                    FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Sondage_Keys.STRUCT_NAME).child(IdSondage).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Sondage s = dataSnapshot.getValue(Sondage.class);
                            s.ID = dataSnapshot.getKey();
                            fragmentToLoad = AnswerSondageFragment.newInstance(s, false);
                            LoadUser();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                if (intent.getStringExtra("FragmentName").equals("PubList")) {
                    String IdAuteur = intent.getStringExtra("AuthorID");
                    fragmentToLoad = PublicationListFragment.newInstance(false, IdAuteur);
                    LoadUser();
                }
            }else{
                fragmentToLoad = SondageListFragment.newInstance(false, null);
                LoadUser();
            }
        }else{
            fragmentToLoad = SondageListFragment.newInstance(false, null);
            LoadUser();
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
    }
    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Utilisateur_Connecte = dataSnapshot.getValue(Profil.class);
            HashMap<String, Object> MapSondagesDone = new HashMap<>();
            for (DataSnapshot dataSnapshot1 : dataSnapshot.child(FireBaseInteraction.Profil_Keys.SONDAGES).getChildren()) {
                MapSondagesDone.put(dataSnapshot1.getKey(), dataSnapshot1.getValue());
            }
            if (!user.isAnonymous()) {
                HashMap<String, String> MapAbonn = new HashMap<>();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.child(FireBaseInteraction.Profil_Keys.AUTEURS_SUIVIS).getChildren()) {
                    MapAbonn.put(dataSnapshot1.getKey(), (String) dataSnapshot1.getValue());
                }
                Utilisateur_Connecte.Auteurs_Suivis = MapAbonn;
            }
            Utilisateur_Connecte.Sondages_Faits = MapSondagesDone;
            changePage(fragmentToLoad);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private void LoadUser(){
        DatabaseReference UtilRef;
        if(user != null) {
            UtilRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(user.getUid());
            UtilRef.addListenerForSingleValueEvent(valueEventListener);
        }else{
            changePage(SondageListFragment.newInstance(false, null));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menu.findItem(R.id.Connected).setVisible(false);
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_home){
            changePage(SondageListFragment.newInstance(false, null));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profil_connexion) {
            changePage(LoginFragment.newInstance());
        } else if (id == R.id.nav_profil_creation) {
            changePage(CreerProfilFragment.newInstance(mAuth));
        } else if (id == R.id.nav_profil_consulter) {
            if(user != null) {
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.password_input_dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = promptsView.findViewById(R.id.dialog_pass_check_field);
                alertDialogBuilder
                        .setCancelable(false).setTitle("SÉCURITÉ")
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, int id) {
                                        // get user input and set it to result
                                        // edit text
                                        if(!userInput.getEditableText().toString().isEmpty()) {
                                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), userInput.getEditableText().toString());
                                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        changePage(ManageProfilFragment.newInstance(userInput.getEditableText().toString()));
                                                        Toast.makeText(getApplicationContext(), "Vous êtes bien " + user.getDisplayName(), Toast.LENGTH_LONG).show();
                                                        dialog.dismiss();
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "Erreur, veuillez rééssayer", Toast.LENGTH_LONG).show(); //message volontairement vague pour tenter de dissuader le brute force manuel
                                                    }
                                                }
                                            });
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), "Champ vide, veuillez rééssayer", Toast.LENGTH_LONG).show(); //message volontairement vague pour tenter de dissuader le brute force manuel
                                        }
                                    }
                                })
                        .setNegativeButton("Annuler",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }).show();
            }

        } else if (id == R.id.nav_profil_sondages_manage) {
            changePage(SondageListFragment.newInstance(true, null));
        } else if (id == R.id.nav_profil_communaute) {
            changePage(PublicationListFragment.newInstance(false, null));
        } else if (id == R.id.nav_profil_publication_manage) {
            changePage(PublicationListFragment.newInstance(true, null));
        } else if (id == R.id.nav_profil_suivis) {
            final ArrayList<String> AuteursId = new ArrayList<>(), AuteursNom = new ArrayList<>();
            for(HashMap.Entry<String, String> cursor : Utilisateur_Connecte.Auteurs_Suivis.entrySet()) {
                AuteursId.add(cursor.getKey());
                AuteursNom.add(cursor.getValue());
            }
            String[] ListeString = new String[AuteursId.size()];
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Choisissez le profil que vous voulez consulter");
            b.setItems(AuteursNom.toArray(ListeString), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, int i) {
                    DatabaseReference profilRef = FirebaseDatabase.getInstance().getReference().child(FireBaseInteraction.Profil_Keys.STRUCT_NAME).child(AuteursId.get(i));
                    profilRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Profil p = dataSnapshot.getValue(Profil.class);
                            p.ID = dataSnapshot.getKey();
                            changePage(ConsultProfilFragment.newInstance(p));
                            dialogInterface.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            dialogInterface.dismiss();
                        }
                    });

                }
            }).show();
        }else if(id == R.id.nav_sondages){
            changePage(SondageListFragment.newInstance(false, null));
        }else if(id == R.id.nav_profil_sondages_repondus){
            changePage(SondageListFragment.newInstance(false, null, true));
        }



        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void logInOut(){
        if(user == null){
            drawer.closeDrawer(GravityCompat.START);
            changePage(LoginFragment.newInstance());
        }else{
            if(!user.isAnonymous()) {//on unregister
                drawer.closeDrawer(GravityCompat.START);
                mAuth.signOut();
                user = mAuth.getCurrentUser();
                updateMenu(user);
                for(HashMap.Entry<String, String> cursor : Utilisateur_Connecte.Auteurs_Suivis.entrySet()) {//pour désactiver les notifications qui ne sont plus ciblés vers cet appareil
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(cursor.getKey());
                }
                changePage(SondageListFragment.newInstance(false, null));
            }else{
                drawer.closeDrawer(GravityCompat.START);
                changePage(LoginFragment.newInstance());
            }
        }
    }

    public void changePage(Fragment fragment) {

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Fragment_Container, fragment);
        fragmentTransaction.addToBackStack(null);
        if (!ActivitySaved) {
            FragmentLoaded = true;
            fragmentTransaction.commit();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle OutState){
        super.onSaveInstanceState(OutState);
        ActivitySaved = true;
    }
    public FragmentManager getFragmentManagerQ(){
        return fragmentManager;
    }
    public void updateMenu(FirebaseUser user){
        FirebaseUser oldUser = this.user;
        this.user = user;
        if(user == null){
            MenuUserAvatar.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_profil_connecter));
            MenuUsername.setText(R.string.Main_NotConnected);
            MenuUserEmail.setText(R.string.Main_PleaseConnect);
            Connected.setVisible(false);
            NotConnected.setVisible(true);
            ConnectLink.setText(R.string.Connexion);
        }else{

            if(user.isAnonymous()){
                MenuUserAvatar.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_profil_connecter));
                MenuUsername.setText(R.string.Main_NotConnected);
                MenuUserEmail.setText(R.string.Main_PleaseConnect);
                Connected.setVisible(false);
                NotConnected.setVisible(true);
                ConnectLink.setText(R.string.Connexion);
            }else {
                MenuUserEmail.setText(user.getEmail());
                MenuUsername.setText(user.getDisplayName());

                if (user.getPhotoUrl() != null) {
                    if (user.getPhotoUrl().toString().equals("N")) {//pas d'image
                        MenuUserAvatar.setImageResource(R.mipmap.ic_launcher);
                    } else {//une image
                        StorageReference ProfilAvRef = FirebaseStorage.getInstance().getReference().child(FireBaseInteraction.Storage_Paths.PROFILS_AVATARS + user.getUid() + ".jpg");
                        ProfilAvRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                MenuUserAvatar.setImageBitmap(b);
                            }
                        });
                    }
                }
                Connected.setVisible(true);
                NotConnected.setVisible(false);
                ConnectLink.setText(R.string.Deconnexion);
            }
        }

    }
    public Profil getUtilisateur_Connecte(){
        return Utilisateur_Connecte;
    }
    public void setUtilisateur_Connecte(Profil utilisateur_Connecte){
        this.Utilisateur_Connecte = utilisateur_Connecte;
    }
    public FirebaseUser getUser(){
        return user;
    }
    public void ChangeTitle(String newTitle){
        this.getSupportActionBar().setTitle(newTitle);
    }

}
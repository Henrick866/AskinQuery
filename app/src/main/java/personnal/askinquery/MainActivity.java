package personnal.askinquery;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SondageListFragment.OnFragmentInteractionListener,
        CreerSondageFragment.OnFragmentInteractionListener,
        CreerOptionDialog.OnFragmentInteractionListener,
        CreerQuestionAdapter.AdaptListener,
        SondageAdapter.AdaptListener,
        LoginFragment.OnFragmentInteractionListener,
        CreerProfilFragment.OnFragmentInteractionListener,
        ManageProfilFragment.OnFragmentInteractionListener
        //PublicationAdapter.AdaptListener
{
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    public FireBaseInteraction fireBaseInteraction;
    private FirebaseAuth mAuth;
    private DrawerLayout drawer;
    private TextView MenuUsername, MenuUserEmail, ConnectLink;
    private ImageView MenuUserAvatar;
    private NavigationView navigationView;
    private MenuItem Connected, NotConnected;
    private Profil UtilisateurConnecte;
    private Menu menu;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        MenuUserAvatar = navigationView.getHeaderView(0).findViewById(R.id.menu_user_avatar);
        MenuUserEmail = navigationView.getHeaderView(0).findViewById(R.id.menu_user_email);
        MenuUsername = navigationView.getHeaderView(0).findViewById(R.id.menu_user_username);
        ConnectLink = navigationView.getHeaderView(0).findViewById(R.id.menu_loginoff);
        menu = navigationView.getMenu();
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
        changePage(SondageListFragment.newInstance(false));

        //changePage(BlankFragment.newInstance("",""));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        if (id == R.id.action_settings) {
            return true;
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
                                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), userInput.getEditableText().toString());
                                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    changePage(ManageProfilFragment.newInstance(userInput.getEditableText().toString()));
                                                    Toast.makeText(getApplicationContext(), "Vous êtes bien "+user.getDisplayName(), Toast.LENGTH_LONG).show();
                                                    dialog.dismiss();
                                                }else{
                                                    Toast.makeText(getApplicationContext(), "Erreur, veuillez rééssayer", Toast.LENGTH_LONG).show(); //message volontairement vague pour tenter de dissuader le brute force manuel
                                                }
                                            }
                                        });
                                    }
                                })
                        .setNegativeButton("Annuler",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
            }

        } else if (id == R.id.nav_profil_sondages_manage) {
            changePage(SondageListFragment.newInstance(true));
        } else if (id == R.id.nav_profil_communaute) {

        } else if (id == R.id.nav_profil_suivis) {

        }



        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void logInOut(){
        if(user == null){
            drawer.closeDrawer(GravityCompat.START);
            changePage(LoginFragment.newInstance());
        }else{
            drawer.closeDrawer(GravityCompat.START);
            mAuth.signOut();
            user = mAuth.getCurrentUser();
            updateMenu(user);
        }
    }

    public void changePage(Fragment fragment){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Fragment_Container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    public void LoadImage(String Image){

    }
    public void SupprimerSondage(Sondage sondage){

    }
    public FragmentManager getFragmentManagerQ(){
        return fragmentManager;
    }
    public void updateMenu(FirebaseUser user){
        this.user = user;
        if(user == null){
            MenuUserAvatar.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_profil_connecter));
            MenuUsername.setText("Non connecté.");
            MenuUserEmail.setText("Veuillez vous connecter.");
            Connected.setVisible(false);
            NotConnected.setVisible(true);
            ConnectLink.setText(R.string.Connexion);
        }else{
            MenuUserEmail.setText(user.getEmail());
            MenuUsername.setText(user.getDisplayName());
            if(user.getPhotoUrl() != null) {
                if (user.getPhotoUrl().toString().equals("N")) {//pas d'image
                    MenuUserAvatar.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_profil_connecter));
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
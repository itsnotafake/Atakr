package templar.atakr.framework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observer;

import templar.atakr.R;
import templar.atakr.databaseobjects.User;
import templar.atakr.databaseobjects.Video;
import templar.atakr.design.AtakrPagerAdapter;
import templar.atakr.sync.VideoSyncIntentService;

/**
 * Created by Devin on 2/20/2017.
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 111;

    private Activity mActivity;
    private Context mContext;

    //Firebase related variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    public DatabaseReference mVideoDatabaseReference;

    //Layout related variables
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    //Data variables & Variables for holding the video data
    // synced from FBDB
    public static String mUsername;
    public static ArrayList<Video> mTopVideoList = new ArrayList<>();
    public static ArrayList<Video> mHotVideoList = new ArrayList<>();
    public static ArrayList<Video> mNewVideoList = new ArrayList<>();
    public static ArrayList<Video> mGameVideoList = new ArrayList<>();

    //Variables for filtering data from Firebasedatabase
    public static long mStartTopQueryAt = 0;
    public static double mStartHotQueryAt = 0;
    public static long mStartNewQueryAt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        mContext = this;

        //Firebase Variable initialization
        initializeDatabase();
        initializeAuthStateListener();
        //Drawer&Toolbar related initialization
        initializeDrawer();
        //Setup ViewPager and Tabs
        initializeViewPager();
        //Begin syncing content provider with firebase
        initializeVideoSync(
                VideoSyncIntentService.ALL_REQUEST_MG,
                VideoSyncIntentService.ALL_DELETE
        );
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop(){
        super.onStop();
        //TODO delete contents of Content Provider
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //Inflate the main_menu; adding items to action bar if present
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        int id = menuItem.getItemId();
        switch(id){
            case(R.id.toolbar_search):
                //TODO 'implement search fragment and action'
            default:
                return true;
        }
    }

    @Override
    public void onBackPressed(){
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_CANCELED){
                finish();
            }
        }
    }

    //Initializes our Authentication State Listener
    private void initializeAuthStateListener(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null){
                    initializeUser();
                }else{
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .setTheme(R.style.AppTheme)
                                    .setIsSmartLockEnabled(false)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    //initializes our Firebase database
    private void initializeDatabase(){
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("Users");
        mVideoDatabaseReference = mFirebaseDatabase.getReference().child("Videos");
    }

    //Initializes our drawer
    private void initializeDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.main_drawer_navigation);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.navigation_menu_games){
                    //TODO
                }else if(id == R.id.navigation_menu_game_genre){
                    //TODO
                }else if(id == R.id.navigation_menu_video_genre){
                    //TODO
                }else if(id == R.id.navigation_menu_signout){
                    AuthUI.getInstance().signOut(mActivity);
                }

                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    //Initialize view pager along with tabs
    private void initializeViewPager(){
        mViewPager = (ViewPager)findViewById(R.id.main_viewpager);
        mViewPager.setAdapter(new AtakrPagerAdapter(getSupportFragmentManager()));
        mTabLayout = (TabLayout) findViewById(R.id.main_tablayout);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    //Creates new User account in User database if new User
    private void initializeUser(){
        mUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            private FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
            private String firebaseUserID = firebaseUser.getUid();
            private String firebaseDisplayName = firebaseUser.getDisplayName();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(firebaseUserID)){
                    User user = new User(firebaseUserID, firebaseDisplayName);
                    mUserDatabaseReference.child(firebaseUserID).setValue(user);
                }
                mUsername = firebaseDisplayName;
                TextView tv = (TextView)findViewById(R.id.main_navigation_user);
                tv.setText(mUsername);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /**
    For some reason this sync that does nothing is necessary. Taking it out
     produces null pointer exceptions. Maybe TODO?? I dunno
    */
    private void initializeVideoSync(int requestCode, int deleteCode){
        Intent intent = new Intent(mContext, VideoSyncIntentService.class);
        intent.putExtra(VideoSyncIntentService.INTENT_REQUEST, requestCode);
        intent.putExtra(VideoSyncIntentService.INTENT_TITLE, "");
        intent.putExtra(VideoSyncIntentService.INTENT_DELETE, deleteCode);
        mContext.startService(intent);
    }

}

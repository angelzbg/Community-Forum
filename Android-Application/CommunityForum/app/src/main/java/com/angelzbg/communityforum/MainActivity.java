package com.angelzbg.communityforum;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.angelzbg.communityforum.models.Post;
import com.angelzbg.communityforum.uimodels.ConstraintLayoutFriend;
import com.angelzbg.communityforum.uimodels.ConstraintLayoutSavedCommunity;
import com.angelzbg.communityforum.utils.SoundHelper;
import com.angelzbg.communityforum.utils.UIHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* Home:
 * posts ordered by date, scrolling down loads older posts, scrolling up is loading newly added posts
 *
 * Saved:
 * user saved psots ordered by date (date added not posts date), scrolling down loads older posts, saving posts from any activity is adding this post to saved at the top (no need for scrolling up)
 *
 * Notifications:
 * all notiications will be displayed here even tho for now there will be only friend requests ( ACCEPT, DECLINE, BLOCK )
 */

public class MainActivity extends AppCompatActivity {

    // Firebase
    private DatabaseReference dbRootReference;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Display metrix
    private int width, height;

    // ScrollsInfo
    private long firstPostHome = 0L, lastPostHome = 0L, firstPostSaved = 0L, lastPostSaved = 0L;

    // Requests helpers (helps preventing firing duplicate queries whenu are offline and not receiving data from scrolling up and down)
    private boolean inProcessPostsNew = false, inProcessPostsOld = false, inProcessSavedOld = false;

    // Loading animation view
    private View main_V_Loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase
        dbRootReference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser(); // should be updated after login or logout
        UIHelper.dbRootReference = dbRootReference;
        UIHelper.currentUser = currentUser;

        // Display metrix / UIHelper
        width  = Resources.getSystem().getDisplayMetrics().widthPixels;
        height = Resources.getSystem().getDisplayMetrics().heightPixels;
        if(width > height){
            int temp = width;
            width = height;
            height = temp;
        }
        UIHelper.width = width;
        UIHelper.height = height;
        UIHelper.font_roboto_regular = ResourcesCompat.getFont(this, R.font.roboto_regular);
        UIHelper.font_roboto_light = ResourcesCompat.getFont(this, R.font.roboto_light);
        UIHelper.font_roboto_medium = ResourcesCompat.getFont(this, R.font.roboto_medium);
        UIHelper.font_roboto_bold = ResourcesCompat.getFont(this, R.font.roboto_bold);
        UIHelper.font_roboto_black = ResourcesCompat.getFont(this, R.font.roboto_black);

        main_V_Loading = findViewById(R.id.main_V_Loading);
        main_V_Loading.getLayoutParams().width = width/2; main_V_Loading.getLayoutParams().height = main_V_Loading.getLayoutParams().width;


        /* Resizing the Main Menu [ START ] */
        findViewById(R.id.main_CL_MainMenu).getLayoutParams().height = height / 16;
        findViewById(R.id.main_CL_MainMenu).setPadding(height/80, height/80, height/80, height/80);
        findViewById(R.id.main_CL_MainMenu).setElevation(height/80); // другите /160 !

        GradientDrawable gdNotifBox = new GradientDrawable();
        gdNotifBox.setColor(ContextCompat.getColor(getApplicationContext(), R.color.whiteBlue));
        gdNotifBox.setStroke(height/600, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        gdNotifBox.setShape(GradientDrawable.RECTANGLE);
        gdNotifBox.setCornerRadius(height/160);
        findViewById(R.id.main_TV_NotifCount_MainMenu).setBackground(gdNotifBox);
        ((TextView)findViewById(R.id.main_TV_NotifCount_MainMenu)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/50);
        findViewById(R.id.main_TV_NotifCount_MainMenu).setPadding(height/160,0,height/160,0);

        GradientDrawable gdNotifBoxWrap = new GradientDrawable();
        gdNotifBoxWrap.setColor(ContextCompat.getColor(getApplicationContext(), R.color.whiteBlue));
        gdNotifBoxWrap.setShape(GradientDrawable.RECTANGLE);
        gdNotifBoxWrap.setCornerRadius(height/160);
        findViewById(R.id.main_CL_Wrap_NotifCount).setBackground(gdNotifBoxWrap);
        findViewById(R.id.main_CL_Wrap_NotifCount).setPadding(height/400, 0, height/400, height/400);
        /* Resizing the Main Menu [  END  ] */

        /* ImageButtons MainMenu onClickListeners [ START ] */
        findViewById(R.id.main_IB_Drawer_MainMenu).setOnClickListener(menuSwitcher);
        findViewById(R.id.main_IB_Saved_MainMenu).setOnClickListener(menuSwitcher);
        findViewById(R.id.main_IB_Home_MainMenu).setOnClickListener(menuSwitcher);
        findViewById(R.id.main_IB_Notifications_MainMenu).setOnClickListener(menuSwitcher);
        findViewById(R.id.main_IB_Search_MainMenu).setOnClickListener(menuSwitcher);
        /* ImageButtons MainMenu onClickListeners [  END  ] */

        /* Resizing Drawer [ START ] */
        findViewById(R.id.drawer).getLayoutParams().width = (width/100)*90; // 90%
        /* Resizing Drawer [  END  ] */

        // Load Posts
        firstPostHome = System.currentTimeMillis();
        lastPostHome = System.currentTimeMillis();
        firstPostSaved = System.currentTimeMillis();
        lastPostSaved = System.currentTimeMillis();
        UIHelper.showLoadingAnimation(main_V_Loading, 1);
        loadPostsHomeOld();

        if(currentUser == null) {
            /* Resizing the Login [ START ] */
            ((TextView)findViewById(R.id.login_TV_Cant)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/40);
            ((TextView)findViewById(R.id.login_TV_Register)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/40);
            ConstraintSet cs = new ConstraintSet();
            cs.clone((ConstraintLayout)findViewById(R.id.drawer_CL_Login));
            cs.connect(R.id.login_TV_Cant, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, height/20);
            cs.connect(R.id.login_TV_Cant, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, height/27);
            cs.connect(R.id.login_TV_Register, ConstraintSet.START, R.id.login_TV_Cant, ConstraintSet.START, 0);
            cs.connect(R.id.login_TV_Register, ConstraintSet.BOTTOM, R.id.login_TV_Cant, ConstraintSet.TOP, 0);
            cs.connect(R.id.login_CL_Wrap, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, height/20);
            cs.connect(R.id.login_CL_Wrap, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, height/20);
            cs.applyTo((ConstraintLayout)findViewById(R.id.drawer_CL_Login));

            ((TextView)findViewById(R.id.login_TV_Title)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/18);

            ((EditText)findViewById(R.id.login_ET_Email)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/40);
            ((EditText)findViewById(R.id.login_ET_Password)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/40);

            findViewById(R.id.login_B_Login).getLayoutParams().width = height/11;

            GradientDrawable gdInput = new GradientDrawable();
            gdInput.setColor(ContextCompat.getColor(getApplicationContext(), R.color.login_input_background));
            gdInput.setShape(GradientDrawable.RECTANGLE);
            gdInput.setCornerRadius(height/320);
            findViewById(R.id.login_ET_Email).setBackground(gdInput);
            findViewById(R.id.login_ET_Password).setBackground(gdInput);
            /* Resizing the Login [  END  ] */
        } else {
            loadLoggedUI();
        }

        //ScrollListeners
        final int[] prevScrollYHome = {0};
        final ScrollView main_SV_Posts = findViewById(R.id.main_SV_Posts);
        main_SV_Posts.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

                if(main_SV_Posts.getVisibility() == View.INVISIBLE) return; // for some reason even tho the scrollview is invisible this shit fires...

                if(prevScrollYHome[0] < main_SV_Posts.getScrollY()) { // scrolling down
                    if(isBottomMenuShown) { // should hide the bottom menu
                        isBottomMenuShown = false;
                        findViewById(R.id.main_CL_MainMenu).setElevation(-1);
                    }
                } else if(prevScrollYHome[0] >= main_SV_Posts.getScrollY()) { // scrolling up
                    if(!isBottomMenuShown) { // should show the bottom menu
                        Animation anim_bottom_menu_show = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_bottom_menu_show);
                        findViewById(R.id.main_CL_MainMenu).startAnimation(anim_bottom_menu_show);
                        isBottomMenuShown = true;
                        findViewById(R.id.main_CL_MainMenu).setElevation(height/80);
                    }
                }

                prevScrollYHome[0] = main_SV_Posts.getScrollY();

                if (main_SV_Posts.getHeight() == main_SV_Posts.getChildAt(0).getHeight() - main_SV_Posts.getScrollY()) {
                    //scroll view is at the bottom
                    if(!inProcessPostsOld) {
                        inProcessPostsOld = true;
                        UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_START);
                        loadPostsHomeOld();
                    } else {
                        UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_SOMETHING_HAPPENED);
                    }
                } else if(main_SV_Posts.getScrollY() == 0) {
                    //scroll view is at the top
                    if(!inProcessPostsNew) {
                        inProcessPostsNew = true;
                        UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_START);
                        loadPostsHomeNew();
                    } else {
                        UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_SOMETHING_HAPPENED);
                    }
                }
            }
        });

        final int[] prevScrollYSaved = {0};
        final ScrollView main_SV_SavedPosts = findViewById(R.id.main_SV_SavedPosts);
        main_SV_SavedPosts.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

                if(main_SV_SavedPosts.getVisibility() == View.INVISIBLE) return; // for some reason even tho the scrollview is invisible this shit fires...

                if(prevScrollYSaved[0] < main_SV_SavedPosts.getScrollY()) { // scrolling down
                    if(isBottomMenuShown) { // should hide the bottom menu
                        isBottomMenuShown = false;
                        findViewById(R.id.main_CL_MainMenu).setElevation(-1);
                    }
                } else if(prevScrollYSaved[0] >= main_SV_SavedPosts.getScrollY()) { // scrolling up
                    if(!isBottomMenuShown) { // should show the bottom menu
                        Animation anim_bottom_menu_show = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_bottom_menu_show);
                        findViewById(R.id.main_CL_MainMenu).startAnimation(anim_bottom_menu_show);
                        isBottomMenuShown = true;
                        findViewById(R.id.main_CL_MainMenu).setElevation(height/80);
                    }
                }

                prevScrollYSaved[0] = main_SV_SavedPosts.getScrollY();

                if (main_SV_SavedPosts.getHeight() == main_SV_SavedPosts.getChildAt(0).getHeight() - main_SV_SavedPosts.getScrollY()) {
                    //scroll view is at the bottom
                    if(!inProcessSavedOld){
                        inProcessSavedOld = true;
                        UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_START);
                        loadPostsSavedOld();
                    } else {
                        UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_SOMETHING_HAPPENED);
                    }
                }
            }
        });

        // Login
        findViewById(R.id.login_B_Login).setOnClickListener(loginClick);

    } // onCreate()
    private boolean isBottomMenuShown = true;

    // Open/Close Drawer
    public void openDrawer(){
        ((DrawerLayout) findViewById(R.id.drawerLayout)).openDrawer(findViewById(R.id.drawer));
    }
    public void closeDrawer(){
        ((DrawerLayout) findViewById(R.id.drawerLayout)).closeDrawer(findViewById(R.id.drawer));
    }

    /* MenuSwitch [ START ] */
    View.OnClickListener menuSwitcher = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.main_IB_Drawer_MainMenu:
                    openDrawer();
                    break;
                case R.id.main_IB_Saved_MainMenu:
                    if(findViewById(R.id.main_SV_SavedPosts).getVisibility() == View.VISIBLE)  return;

                    findViewById(R.id.main_SV_Posts).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Home_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_Notifications).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Notifications_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_SavedPosts).setVisibility(View.VISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Saved_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    SoundHelper.playSound(getApplicationContext(), SoundHelper.TYPE_MENU_SWITCH);
                    break;
                case R.id.main_IB_Home_MainMenu:
                    if(findViewById(R.id.main_SV_Posts).getVisibility() == View.VISIBLE)  return;

                    findViewById(R.id.main_SV_Notifications).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Notifications_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_SavedPosts).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Saved_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_Posts).setVisibility(View.VISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Home_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    SoundHelper.playSound(getApplicationContext(), SoundHelper.TYPE_MENU_SWITCH);
                    break;
                case R.id.main_IB_Notifications_MainMenu:
                    if(findViewById(R.id.main_SV_Notifications).getVisibility() == View.VISIBLE)  return;

                    findViewById(R.id.main_SV_SavedPosts).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Home_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_Posts).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Saved_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_Notifications).setVisibility(View.VISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Notifications_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    SoundHelper.playSound(getApplicationContext(), SoundHelper.TYPE_MENU_SWITCH);
                    break;
                case R.id.main_IB_Search_MainMenu:
                    // go to search activity
                    break;
            }
        }
    };
    /* MenuSwitch [  END  ] */

    // Notifications update
    public void updateNotificationsCount(){
        LinearLayout main_LL_Notifications = findViewById(R.id.main_LL_Notifications);
        int notificationCount = main_LL_Notifications.getChildCount();
        if(notificationCount != 0) {
            ((TextView)findViewById(R.id.main_TV_NotifCount_MainMenu)).setText(notificationCount + "");
            findViewById(R.id.main_CL_Wrap_NotifCount).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.main_CL_Wrap_NotifCount).setVisibility(View.INVISIBLE);
        }
    } // updateNotificationsCount()


    private void loadPostsHomeOld(){ // down
        dbRootReference.child("posts").orderByChild("date").endAt(lastPostHome-1).limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot DATA) {
                if(DATA.exists()){
                    UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_NEW);
                    final int prePostsCount = ((LinearLayout)findViewById(R.id.main_LL_Posts)).getChildCount();
                    List<Post> posts = new ArrayList<>();
                    List<String> keys = new ArrayList<>();
                    for(DataSnapshot dataSnapshot : DATA.getChildren()){
                        posts.add(dataSnapshot.getValue(Post.class));
                        keys.add(dataSnapshot.getKey());
                    }
                    for(int i = posts.size()-1; i > -1; i--){ // we get them in ascending order (old to new) but we need to add each one of them at the end of the layout descending (new to old)
                        UIHelper.addNewPost(MainActivity.this, ((LinearLayout) findViewById(R.id.main_LL_Posts)), UIHelper.POSITION_BOTTOM, posts.get(i), keys.get(i), true, true);
                    }
                    lastPostHome = posts.get(0).getDate();
                    if(prePostsCount != 0) {
                        final ScrollView main_SV_Posts = findViewById(R.id.main_SV_Posts);
                        main_SV_Posts.post(new Runnable() {
                            @Override
                            public void run() {
                                main_SV_Posts.arrowScroll(View.FOCUS_DOWN);
                            }
                        });
                    }
                } else {
                    UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_NO_RESULT);
                }
                inProcessPostsOld = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                inProcessPostsOld = false;
                UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_SOMETHING_HAPPENED);
            }
        });
    } // loadPostsHomeOld{}
    private void loadPostsHomeNew(){ // up
        dbRootReference.child("posts").orderByChild("date").startAt(firstPostHome+1).limitToFirst(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot DATA) {
                if(DATA.exists()){
                    UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_NEW);
                    for(DataSnapshot dataSnapshot : DATA.getChildren()){
                        Post post = dataSnapshot.getValue(Post.class);
                        UIHelper.addNewPost(MainActivity.this, ((LinearLayout) findViewById(R.id.main_LL_Posts)), UIHelper.POSITION_TOP, post, dataSnapshot.getKey(), true, true);
                        firstPostHome = post.getDate();
                    }
                    final ScrollView main_SV_Posts = findViewById(R.id.main_SV_Posts);
                    main_SV_Posts.post(new Runnable() {
                        @Override
                        public void run() {
                            main_SV_Posts.arrowScroll(View.FOCUS_UP);
                        }
                    });
                } else {
                    UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_NO_RESULT);
                }
                inProcessPostsNew = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                inProcessPostsNew = false;
                UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_SOMETHING_HAPPENED);
            }
        });
    } // loadPostsHomeNew{}
    private void loadPostsSavedOld(){ // down
        dbRootReference.child("saved_posts/" + currentUser.getUid()).orderByValue().endAt(lastPostSaved-1).limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot DATA) {
                if(DATA.exists()){
                    UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_NEW);
                    final List<String> postsUUIDs = new ArrayList<>();
                    final List<Long> savedDates = new ArrayList<>();
                    for(DataSnapshot postData : DATA.getChildren()) {
                        postsUUIDs.add(postData.getKey());
                        savedDates.add(postData.getValue(Long.class));
                    }
                    lastPostSaved = savedDates.get(0);

                    for(int i=postsUUIDs.size()-1; i > -1; i--) {
                        dbRootReference.child("posts/" + postsUUIDs.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                UIHelper.addNewPost(MainActivity.this, ((LinearLayout) findViewById(R.id.main_LL_SavedPosts)), UIHelper.POSITION_BOTTOM, dataSnapshot.getValue(Post.class), dataSnapshot.getKey(), true, true);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                } else {
                    UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_NO_RESULT);
                }
                inProcessSavedOld = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                inProcessSavedOld = false;
                UIHelper.showLoadingAnimation(main_V_Loading, UIHelper.LOADING_SOMETHING_HAPPENED);
            }
        });
    } // loadPostsHSaveOld{}

    // ------------------------------------------------- Login
    View.OnClickListener loginClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ImageButton button = (ImageButton) v;
            button.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            final EditText ET_Email = findViewById(R.id.login_ET_Email), ET_Password = findViewById(R.id.login_ET_Password);
            final String email = ET_Email.getText().toString().trim(), password = ET_Password.getText().toString().trim();

            if(email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(getApplicationContext(), "This is not a valid email!", Toast.LENGTH_SHORT).show();
                button.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.login_light));
                return;
            }

            if(password == null || password.length() < 6) {
                Toast.makeText(getApplicationContext(), "This is not a valid password!", Toast.LENGTH_SHORT).show();
                button.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.login_light));
                return;
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        currentUser = auth.getCurrentUser();
                        UIHelper.currentUser = auth.getCurrentUser();
                        loadLoggedUI();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        button.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.login_light));
                    }
                }
            });
        }
    };

    private void loadLoggedUI(){
        findViewById(R.id.drawer_CL_Content).setVisibility(View.VISIBLE);
        findViewById(R.id.drawer_CL_Login).setVisibility(View.GONE);
        findViewById(R.id.main_IB_Saved_MainMenu).setVisibility(View.VISIBLE);
        findViewById(R.id.main_IB_Notifications_MainMenu).setVisibility(View.VISIBLE);
        findViewById(R.id.drawer_CL_SearchUser).getLayoutParams().height = height/16;

        /* Resizing the drawer [ START ] */
        findViewById(R.id.drawer_V_SavedCommBackrg).getLayoutParams().width = height/11;
        findViewById(R.id.drawer_CL_UserSettings).getLayoutParams().height = height/13;
        findViewById(R.id.drawer_CL_UserSettings).setPadding(height/53,height/80,height/53,height/80);

        ConstraintSet cs = new ConstraintSet();
        cs.clone((ConstraintLayout)findViewById(R.id.drawer_CL_UserSettings));
        cs.connect(R.id.drawer_TV_Username, ConstraintSet.START, R.id.drawer_IV_Avatar, ConstraintSet.END, height/53);
        cs.applyTo((ConstraintLayout)findViewById(R.id.drawer_CL_UserSettings));

        ((TextView)findViewById(R.id.drawer_TV_Username)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        ((TextView)findViewById(R.id.drawer_TV_Points)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);

        findViewById(R.id.drawer_SV_Communities).getLayoutParams().width = height/11;
        findViewById(R.id.drawer_IB_CreateCommunity).getLayoutParams().width = height/11;
        /* Resizing the drawer [  END  ] */

        loadUserData();
        loadPostsSavedOld();

        // Listening for new saved posts
        dbRootReference.child("saved_posts/" + currentUser.getUid()).orderByValue().startAt(firstPostSaved).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                dbRootReference.child("posts/" + dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UIHelper.addNewPost(MainActivity.this, ((LinearLayout) findViewById(R.id.main_LL_SavedPosts)), UIHelper.POSITION_TOP, dataSnapshot.getValue(Post.class), dataSnapshot.getKey(), true, true);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // Listening for new friend requests
        dbRootReference.child("friendRequests/" + currentUser.getUid()).orderByValue().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                UIHelper.addNewNotification(MainActivity.this, (LinearLayout)findViewById(R.id.main_LL_Notifications), dataSnapshot.getKey(), dataSnapshot.getValue(Long.class));
                updateNotificationsCount();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        loadSavedCommunities();

        loadFriends();

    } // loadLoggedUI()

    private void loadUserData(){
        dbRootReference.child("users/" + currentUser.getUid() + "/avatar").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Bitmap avatar = UIHelper.StringToBitMap(dataSnapshot.getValue(String.class));
                if(avatar != null) {
                    avatar = UIHelper.CropBitmapCenterCircle(avatar);
                    ((ImageView)findViewById(R.id.drawer_IV_Avatar)).setImageBitmap(avatar);
                    avatar = null;
                    System.gc();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        dbRootReference.child("users/" + currentUser.getUid() + "/points").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.drawer_TV_Points)).setText("Points " + dataSnapshot.getValue(Integer.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        dbRootReference.child("users/" + currentUser.getUid() + "/username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.drawer_TV_Username)).setText(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        dbRootReference.child("admins/" + currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    UIHelper.isAdmin = true;
                } else {
                    UIHelper.isAdmin = false;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                UIHelper.isAdmin = false; // if error occurs means the user doesn't have permission to read this node hence is not admin
            }
        });

        // Online status - when the user disconnects his online status will be removed, when he comes online and see the presense of his online state isn't there, he's going to update it back to online
        // The offline capabilities won't interfere, because onDisconnect is handled by the server and even tho he's going to update it to online immediately it won't update for his friends untill he comes online
        dbRootReference.child("online/" + currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    dbRootReference.child("online/" + currentUser.getUid()).setValue(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        dbRootReference.child("online/" + currentUser.getUid()).onDisconnect().removeValue();

        // Go to self profile
        View.OnClickListener goToSelfProfile = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("userUUID", currentUser.getUid());
                startActivity(intent);
            }
        };
        findViewById(R.id.drawer_IV_Avatar).setOnClickListener(goToSelfProfile);
        findViewById(R.id.drawer_TV_Username).setOnClickListener(goToSelfProfile);
        findViewById(R.id.drawer_TV_Points).setOnClickListener(goToSelfProfile);
        findViewById(R.id.drawer_IB_Settings).setOnClickListener(goToSelfProfile);
    } // loadUserData()

    private HashMap<String, ConstraintLayoutSavedCommunity> savedCommunitiesMap = new HashMap<>();
    private void loadSavedCommunities(){
        dbRootReference.child("saved_communities").child(currentUser.getUid()).orderByValue().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, height/11);
                lp.setMargins(height/160, height/800, height/160, height/800);
                ConstraintLayoutSavedCommunity savedCommunity = new ConstraintLayoutSavedCommunity(MainActivity.this, dataSnapshot.getKey());
                ((LinearLayout)findViewById(R.id.drawer_LL_Communities)).addView(savedCommunity, 0, lp);
                savedCommunitiesMap.put(dataSnapshot.getKey(), savedCommunity);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                savedCommunitiesMap.get(dataSnapshot.getKey()).clearRealtime();
                ((LinearLayout)findViewById(R.id.drawer_LL_Communities)).removeView(savedCommunitiesMap.get(dataSnapshot.getKey()));
                savedCommunitiesMap.remove(dataSnapshot.getKey());
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    } // loadSavedCommunities()

    private void loadFriends(){
        dbRootReference.child("friends/"+currentUser.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final String friendUUID = dataSnapshot.getKey();
                final String chatUUID = dataSnapshot.getValue(String.class);
                ConstraintLayoutFriend CL_Friend = new ConstraintLayoutFriend(MainActivity.this, friendUUID, chatUUID, (LinearLayout)findViewById(R.id.drawer_LL_Friends));
                UIHelper.friends.put(friendUUID, CL_Friend);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                ConstraintLayoutFriend CL_Friend = UIHelper.friends.get(dataSnapshot.getKey());
                CL_Friend.clearRealtime();
                ((LinearLayout) findViewById(R.id.drawer_LL_Friends)).removeView(CL_Friend);
                UIHelper.friends.remove(dataSnapshot.getKey());
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    } // loadFriends()

} // MainActivity{}
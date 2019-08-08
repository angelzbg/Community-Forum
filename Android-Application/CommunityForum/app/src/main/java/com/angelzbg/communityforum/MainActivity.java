package com.angelzbg.communityforum;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.angelzbg.communityforum.models.Post;
import com.angelzbg.communityforum.utils.SoundHelper;
import com.angelzbg.communityforum.utils.UIHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Firebase
    private DatabaseReference dbRootReference;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Display metrix
    private int width, height;

    // ScrollsInfo
    private long firstPostHome = 0L, lastPostHome = 0L, firstPostSaved = 0L, lastPostSaved = 0L;

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


        /* Resizing the Main Menu [ START ] */
        findViewById(R.id.main_CL_MainMenu).getLayoutParams().height = height / 16;
        findViewById(R.id.main_CL_MainMenu).setPadding(height/80, height/80, height/80, height/80);
        findViewById(R.id.main_CL_MainMenu).setElevation(height/80); // другите /160 !

        GradientDrawable gdNotifBox = new GradientDrawable();
        gdNotifBox.setColor(ContextCompat.getColor(getApplicationContext(), R.color.whiteBlue));
        gdNotifBox.setStroke(height/400, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
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

        // Load Posts
        firstPostHome = System.currentTimeMillis();
        lastPostHome = System.currentTimeMillis();
        firstPostSaved = System.currentTimeMillis();
        lastPostSaved = System.currentTimeMillis();
        loadPostsHomeOld();
        //ScrollListeners
        final int[] prevScrollYHome = {0};
        final boolean[] isBottomMenuShown = {true};
        final ScrollView main_SV_Posts = findViewById(R.id.main_SV_Posts);
        main_SV_Posts.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

                if(prevScrollYHome[0] < main_SV_Posts.getScrollY()) { // scrolling down
                    if(isBottomMenuShown[0]) { // should hide the bottom menu
                        Animation anim_bottom_menu_hide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_bottom_menu_hide);
                        findViewById(R.id.main_CL_MainMenu).startAnimation(anim_bottom_menu_hide);
                        isBottomMenuShown[0] = false;
                    }
                } else if(prevScrollYHome[0] >= main_SV_Posts.getScrollY()) { // scrolling up
                    if(!isBottomMenuShown[0]) { // should show the bottom menu
                        Animation anim_bottom_menu_show = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_bottom_menu_show);
                        findViewById(R.id.main_CL_MainMenu).startAnimation(anim_bottom_menu_show);
                        isBottomMenuShown[0] = true;
                    }
                }

                prevScrollYHome[0] = main_SV_Posts.getScrollY();

                if (main_SV_Posts.getHeight() == main_SV_Posts.getChildAt(0).getHeight() - main_SV_Posts.getScrollY()) {
                    //scroll view is at the bottom
                    loadPostsHomeOld();
                } else if(main_SV_Posts.getScrollY() == 0) {
                    //scroll view is at the top
                    loadPostsHomeNew();
                }
            }
        });

    } // onCreate()

    /* MenuSwitch [ START ] */
    View.OnClickListener menuSwitcher = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.main_IB_Drawer_MainMenu:
                    // open Drawer
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
    private void updateNotificationsCount(){
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
                    List<Post> posts = new ArrayList<>();
                    List<String> keys = new ArrayList<>();
                    for(DataSnapshot dataSnapshot : DATA.getChildren()){
                        posts.add(dataSnapshot.getValue(Post.class));
                        keys.add(dataSnapshot.getKey());
                    }
                    for(int i = posts.size()-1; i > -1; i--){ // we get them in ascending order (old to new) but we need to add each one of them at the end of the layout descending (new to old)
                        UIHelper.addNewPost(getApplicationContext(), ((LinearLayout) findViewById(R.id.main_LL_Posts)), UIHelper.POSITION_BOTTOM, posts.get(i), keys.get(i), true);
                    }
                    lastPostHome = posts.get(0).getDate();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    } // loadPostsHomeOld{}
    private void loadPostsHomeNew(){ // up
        dbRootReference.child("posts").orderByChild("date").startAt(firstPostHome+1).limitToFirst(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot DATA) {
                if(DATA.exists()){
                    for(DataSnapshot dataSnapshot : DATA.getChildren()){
                        Post post = dataSnapshot.getValue(Post.class);
                        UIHelper.addNewPost(getApplicationContext(), ((LinearLayout) findViewById(R.id.main_LL_Posts)), UIHelper.POSITION_TOP, post, dataSnapshot.getKey(), true);
                        firstPostHome = post.getDate();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    } // loadPostsHomeNew{}



} // MainActivity{}
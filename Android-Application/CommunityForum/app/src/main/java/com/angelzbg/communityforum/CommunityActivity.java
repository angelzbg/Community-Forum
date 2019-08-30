package com.angelzbg.communityforum;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.angelzbg.communityforum.models.Community;
import com.angelzbg.communityforum.uimodels.ConstraintLayoutPost;
import com.angelzbg.communityforum.utils.UIHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityActivity extends AppCompatActivity {

    private String communityUUID = "";
    private boolean isCommunitySaved = false;
    private int width = 0, height = 0;

    private FirebaseUser currentUser = null;
    private DatabaseReference dbRootReference;

    private final List<ConstraintLayoutPost> mapPosts = new ArrayList<>();
    private final HashMap<DatabaseReference, ValueEventListener> realtimeMap1 = new HashMap<>();
    private final HashMap<DatabaseReference, ChildEventListener> realtimeMap2 = new HashMap<>();
    private void clearRealtime() {
        for(Map.Entry<DatabaseReference, ValueEventListener> entry : realtimeMap1.entrySet()) { // removing the realtime listeners
            DatabaseReference reference = entry.getKey();
            ValueEventListener listener = entry.getValue();

            reference.removeEventListener(listener);
        }

        for(Map.Entry<DatabaseReference, ChildEventListener> entry : realtimeMap2.entrySet()) { // removing the realtime listeners
            DatabaseReference reference = entry.getKey();
            ChildEventListener listener = entry.getValue();

            reference.removeEventListener(listener);
        }

        for(ConstraintLayoutPost clp : mapPosts) {
            clp.clearRealtime();
        }
    } // clearRealtime()

    @Override
    public void onBackPressed() {
        clearRealtime();
        super.onBackPressed();
    }

    View.OnClickListener onClickBack = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CommunityActivity.this.onBackPressed();
        }
    };

    // Scroll info
    private long firstPost = 0L, lastPost = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        communityUUID = getIntent().getStringExtra("communityUUID");
        width = UIHelper.width;
        height = UIHelper.height;

        currentUser = UIHelper.currentUser;
        dbRootReference = UIHelper.dbRootReference;


        // ------------------------------ Resizing [ START ]
        findViewById(R.id.community_IV_Avatar).getLayoutParams().width = height/8;
        findViewById(R.id.community_IV_Avatar).getLayoutParams().height = height/8;
        findViewById(R.id.community_IB_Back).getLayoutParams().width = height/20;
        findViewById(R.id.community_IB_Back).getLayoutParams().height = height/20;
        findViewById(R.id.community_IB_Save).getLayoutParams().width = height/20;
        findViewById(R.id.community_IB_Save).getLayoutParams().height = height/20;

        ((TextView)findViewById(R.id.community_TV_Title)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        ((TextView)findViewById(R.id.community_TV_CreatedBy)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        ((TextView)findViewById(R.id.community_TV_Creator)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        ((TextView)findViewById(R.id.community_TV_Description)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        ((TextView)findViewById(R.id.community_TV_Name)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/40);
        ((TextView)findViewById(R.id.community_TV_Posts)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        ((TextView)findViewById(R.id.community_TV_Date)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        ((TextView)findViewById(R.id.community_TV_Users)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);

        findViewById(R.id.community_CL_InfoPostsWrap).getLayoutParams().width = (width-(height/16)*2)/3;
        findViewById(R.id.community_CL_InfoPostsWrap).getLayoutParams().height = height/16;
        findViewById(R.id.community_CL_InfoDateWrap).getLayoutParams().width = (width-(height/16)*2)/3;
        findViewById(R.id.community_CL_InfoDateWrap).getLayoutParams().height = height/16;
        findViewById(R.id.community_CL_InfoUsersWrap).getLayoutParams().width = (width-(height/16)*2)/3;
        findViewById(R.id.community_CL_InfoUsersWrap).getLayoutParams().height = height/16;

        ConstraintSet cs = new ConstraintSet();
        cs.clone((ConstraintLayout)findViewById(R.id.community_CL_Main));
        cs.connect(R.id.community_IB_Save, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, height/160);
        cs.applyTo((ConstraintLayout)findViewById(R.id.community_CL_Main));

        cs.clone((ConstraintLayout)findViewById(R.id.community_CL_Head));
        cs.connect(R.id.community_Space_Title, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, height/20);
        cs.connect(R.id.community_Space_Name, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, height/20);
        cs.connect(R.id.community_Space_Name, ConstraintSet.TOP, R.id.community_TV_Description, ConstraintSet.BOTTOM, height/160);

        cs.connect(R.id.community_CL_InfoDateWrap, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, height/16);
        cs.connect(R.id.community_CL_InfoDateWrap, ConstraintSet.TOP, R.id.community_IV_Avatar, ConstraintSet.BOTTOM, height/80);
        cs.connect(R.id.community_CL_InfoUsersWrap, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, height/16);

        cs.connect(R.id.community_TV_CreatedBy, ConstraintSet.TOP, R.id.community_CL_InfoDateWrap, ConstraintSet.BOTTOM, height/160);

        cs.applyTo((ConstraintLayout)findViewById(R.id.community_CL_Head));
        // ------------------------------ Resizing [  END  ]

        // Data
        // Community Name
        dbRootReference.child("communities/" + communityUUID + "/name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.community_TV_Name)).setText(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // COmmunity date of creation
        dbRootReference.child("communities/" + communityUUID + "/date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.community_TV_Date)).setText(new SimpleDateFormat("dd/MM/yy").format(new Date(dataSnapshot.getValue(Long.class))));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // Community description
        dbRootReference.child("communities/" + communityUUID + "/description").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.community_TV_Description)).setText(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // Community description
        dbRootReference.child("communities/" + communityUUID + "/avatar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Bitmap avatar = UIHelper.StringToBitMap(dataSnapshot.getValue(String.class));
                if(avatar != null){
                    avatar = UIHelper.CropBitmapCenterCircle(avatar);
                    ((ImageView)findViewById(R.id.community_IV_Avatar)).setImageBitmap(avatar);
                    avatar = null;
                    System.gc();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // Community creator username
        dbRootReference.child("communities/" + communityUUID + "/creator").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                dbRootReference.child("users/" + dataSnapshot.getValue(String.class) + "/username").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ((TextView)findViewById(R.id.community_TV_Creator)).setText(dataSnapshot.getValue(String.class));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // Posts count
        final DatabaseReference dbRefCommunityPostsCount = dbRootReference.child("communities/" + communityUUID + "/posts_count");
        ValueEventListener listenerPostsCount = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.community_TV_Posts)).setText(dataSnapshot.getValue(Integer.class) + "");
                Animation anim_pop = AnimationUtils.loadAnimation(CommunityActivity.this, R.anim.anim_pop_votes_comments);
                findViewById(R.id.community_TV_Posts).startAnimation(anim_pop);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        dbRefCommunityPostsCount.addValueEventListener(listenerPostsCount);
        realtimeMap1.put(dbRefCommunityPostsCount, listenerPostsCount);

        // Users count
        DatabaseReference dbRefCommunityUsersCount = dbRootReference.child("communities/" + communityUUID + "/users_count");
        ValueEventListener listenerUsersCount = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.community_TV_Users)).setText(dataSnapshot.getValue(Integer.class) + "");
                Animation anim_pop = AnimationUtils.loadAnimation(CommunityActivity.this, R.anim.anim_pop_votes_comments);
                findViewById(R.id.community_TV_Users).startAnimation(anim_pop);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        dbRefCommunityUsersCount.addValueEventListener(listenerUsersCount);
        realtimeMap1.put(dbRefCommunityUsersCount, listenerUsersCount);

        if(currentUser != null) { // user is logged in

            findViewById(R.id.community_IB_Save).setVisibility(View.VISIBLE);

            DatabaseReference dbRefSaved = dbRootReference.child("saved_communities/" + currentUser.getUid() + "/" + communityUUID);
            ValueEventListener listenerSaved = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        ((ImageButton)findViewById(R.id.community_IB_Save)).setColorFilter(ContextCompat.getColor(CommunityActivity.this, R.color.white));
                        isCommunitySaved = true;
                    } else {
                        ((ImageButton)findViewById(R.id.community_IB_Save)).setColorFilter(ContextCompat.getColor(CommunityActivity.this, R.color.saved_transparent));
                        isCommunitySaved = false;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            };
            dbRefSaved.addValueEventListener(listenerSaved);
            realtimeMap1.put(dbRefSaved, listenerSaved);

            findViewById(R.id.community_IB_Save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isCommunitySaved){
                        dbRootReference.child("saved_communities/" + currentUser.getUid() + "/" + communityUUID).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Toast.makeText(getApplicationContext(), "COMMUNITY REMOVED FROM SAVED", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        dbRootReference.child("saved_communities/" + currentUser.getUid() + "/" + communityUUID).setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Toast.makeText(getApplicationContext(), "COMMUNITY SAVED", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });

        }



        // OnClickListeners
        findViewById(R.id.community_IB_Back).setOnClickListener(onClickBack);


    } // onCreate()

} // CommunityActivity{}
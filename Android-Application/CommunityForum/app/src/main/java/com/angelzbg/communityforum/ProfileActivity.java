package com.angelzbg.communityforum;

import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.angelzbg.communityforum.models.Post;
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

public class ProfileActivity extends AppCompatActivity {

    private String userUUID = "";
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
            ProfileActivity.this.onBackPressed();
        }
    };

    // Scroll info
    private long firstPost = 0L, lastPost = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userUUID = getIntent().getStringExtra("userUUID");
        width = UIHelper.width;
        height = UIHelper.height;

        currentUser = UIHelper.currentUser;
        dbRootReference = UIHelper.dbRootReference;


        // ------------------------------ Resizing [ START ]
        findViewById(R.id.profile_CL_InfoTop).getLayoutParams().height = height/4;
        findViewById(R.id.profile_IB_Back).getLayoutParams().width = height/20;
        findViewById(R.id.profile_IB_Back).getLayoutParams().height = height/20;
        findViewById(R.id.profile_IV_Avatar).getLayoutParams().width = height/8;
        findViewById(R.id.profile_IV_Avatar).getLayoutParams().height = height/8;

        ((TextView)findViewById(R.id.profile_TV_Title)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        ((TextView)findViewById(R.id.profile_TV_Username)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/40);

        findViewById(R.id.profile_CL_InfoBottom).getLayoutParams().height = (int)(height/10.66);

        ConstraintSet cs = new ConstraintSet();
        cs.clone((ConstraintLayout)findViewById(R.id.profile_CL_InfoBottom));
        cs.connect(R.id.profile_CL_MemberWrap, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, (int)(height/26.66));
        cs.connect(R.id.profile_CL_MemberWrap, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, height/80);
        cs.connect(R.id.profile_CL_MemberWrap, ConstraintSet.END, R.id.profile_Space_MiddleBot, ConstraintSet.START, (int)(height/26.66));
        cs.connect(R.id.profile_CL_MemberWrap, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, height/80);
        cs.connect(R.id.profile_CL_PointsWrap, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, (int)(height/26.66));
        cs.connect(R.id.profile_CL_PointsWrap, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, height/80);
        cs.connect(R.id.profile_CL_PointsWrap, ConstraintSet.START, R.id.profile_Space_MiddleBot, ConstraintSet.END, (int)(height/26.66));
        cs.connect(R.id.profile_CL_PointsWrap, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, height/80);
        cs.applyTo((ConstraintLayout)findViewById(R.id.profile_CL_InfoBottom));

        findViewById(R.id.profile_CL_MemberWrap).setPadding(height/53,height/160,height/53,height/160);
        findViewById(R.id.profile_CL_PointsWrap).setPadding(height/53,height/160,height/53,height/160);

        ((TextView)findViewById(R.id.profile_TV_Date)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        ((TextView)findViewById(R.id.profile_TV_ExactDate)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        ((TextView)findViewById(R.id.profile_TV_Points)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        ((TextView)findViewById(R.id.profile_TV_ExactPoints)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);

        GradientDrawable gdWrapper = new GradientDrawable();
        gdWrapper.setColor(ContextCompat.getColor(this, R.color.whiteBlue));
        gdWrapper.setStroke(height/800, ContextCompat.getColor(this, R.color.shadowGray));
        gdWrapper.setShape(GradientDrawable.RECTANGLE);
        gdWrapper.setCornerRadius(height/160);
        findViewById(R.id.profile_CL_MemberWrap).setBackground(gdWrapper);
        findViewById(R.id.profile_CL_PointsWrap).setBackground(gdWrapper);

        //Top bar resizing
        findViewById(R.id.profile_CL_TopBar).getLayoutParams().height = height/20;
        ((TextView)findViewById(R.id.profile_TV_TopBar_Username)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/40);
        // ------------------------------ Resizing [  END  ]

        // Back buttons
        findViewById(R.id.profile_IB_Back).setOnClickListener(onClickBack);
        findViewById(R.id.profile_IB_TopBar_Back).setOnClickListener(onClickBack);

        // Loading data
        if(currentUser != null) { // logged
            if(userUUID.equals(currentUser.getUid())) { // observing self profile

            } else { // observing another user profile
                findViewById(R.id.profile_IB_AddRemove).setVisibility(View.VISIBLE);
                findViewById(R.id.profile_IB_Block).setVisibility(View.VISIBLE);

                findViewById(R.id.profile_IB_Block).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbRootReference.child("blocks/" + currentUser.getUid() + "/" + userUUID).setValue(true, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Toast.makeText(getApplicationContext(), "USER BLOCKED", Toast.LENGTH_SHORT).show();
                                findViewById(R.id.profile_IB_Block).setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });

                final boolean[] isFriend = { false };
                findViewById(R.id.profile_IB_AddRemove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isFriend[0]){
                            dbRootReference.child("friends/" + currentUser.getUid() + "/" + userUUID).removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    Toast.makeText(getApplicationContext(), "USER UNFRIENDED", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            dbRootReference.child("friendRequests/" + userUUID + "/" + currentUser.getUid()).setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if(databaseError != null) {
                                        Toast.makeText(getApplicationContext(), "FRIEND REQUEST WASN'T SENT", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "FRIEND REQUEST HAS BEEN SENT", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
                final DatabaseReference dbRefFriend = dbRootReference.child("friends/" + currentUser.getUid() + "/" + userUUID);
                final ValueEventListener listenerFriend = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            isFriend[0] = true;
                            ((ImageButton)findViewById(R.id.profile_IB_AddRemove)).setImageResource(R.drawable.icon_user_remove);
                            ((ImageButton)findViewById(R.id.profile_IB_AddRemove)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.color_decline));
                        } else {
                            isFriend[0] = false;
                            ((ImageButton)findViewById(R.id.profile_IB_AddRemove)).setImageResource(R.drawable.icon_user_add);
                            ((ImageButton)findViewById(R.id.profile_IB_AddRemove)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.color_accept));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                };
                dbRefFriend.addValueEventListener(listenerFriend);
                realtimeMap1.put(dbRefFriend, listenerFriend);


                // ---------- Admin
                if(UIHelper.isAdmin) { // the user is admin
                    findViewById(R.id.profile_IB_Restrict).setVisibility(View.VISIBLE);
                    final boolean[] isRestricted = { false };
                    findViewById(R.id.profile_IB_Restrict).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(isRestricted[0]) {
                                dbRootReference.child("restricted/" + userUUID).removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        Toast.makeText(getApplicationContext(), "RESTRICTION REMOVED", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                dbRootReference.child("restricted/" + userUUID).setValue(true, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        Toast.makeText(getApplicationContext(), "USER RESTRICTED", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                    final DatabaseReference dbRefRestricted = dbRootReference.child("restricted/" + userUUID);
                    final ValueEventListener listenerRestricted = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                isRestricted[0] = true;
                                ((ImageButton)findViewById(R.id.profile_IB_Restrict)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.color_block));
                            } else {
                                isRestricted[0] = false;
                                ((ImageButton)findViewById(R.id.profile_IB_Restrict)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.color_accept));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    };
                    dbRefRestricted.addValueEventListener(listenerRestricted);
                    realtimeMap1.put(dbRefRestricted, listenerRestricted);
                }
            }
        } else { // not logged

        }

        // User data
        final DatabaseReference dbRefAvatar =  dbRootReference.child("users/" + userUUID + "/avatar");
        final ValueEventListener listenerAvatar = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String avatarString = dataSnapshot.getValue(String.class);
                Bitmap avatar = UIHelper.StringToBitMap(avatarString);
                if(avatar != null){
                    avatar = UIHelper.CropBitmapCenterCircle(avatar);
                    ((ImageView)findViewById(R.id.profile_IV_Avatar)).setImageBitmap(avatar);
                    avatar = null;
                    System.gc();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        dbRefAvatar.addListenerForSingleValueEvent(listenerAvatar);

        final DatabaseReference dbRefUsername =  dbRootReference.child("users/" + userUUID + "/username");
        final ValueEventListener listenerUsername = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String username = dataSnapshot.getValue(String.class);
                ((TextView)findViewById(R.id.profile_TV_Username)).setText(username);
                ((TextView)findViewById(R.id.profile_TV_TopBar_Username)).setText(username);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        dbRefUsername.addListenerForSingleValueEvent(listenerUsername);

        final DatabaseReference dbRefDate =  dbRootReference.child("users/" + userUUID + "/date");
        final ValueEventListener listenerDate = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.profile_TV_ExactDate)).setText(new SimpleDateFormat("dd/MM/yy").format(new Date(dataSnapshot.getValue(Long.class))));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        dbRefDate.addListenerForSingleValueEvent(listenerDate);

        final DatabaseReference dbRefPoints =  dbRootReference.child("users/" + userUUID + "/points");
        final ValueEventListener listenerPoints = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.profile_TV_ExactPoints)).setText(dataSnapshot.getValue(Integer.class) + "");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        dbRefPoints.addValueEventListener(listenerPoints);
        realtimeMap1.put(dbRefPoints, listenerPoints);

        // Posts/Scrolls
        firstPost = System.currentTimeMillis();
        lastPost = System.currentTimeMillis();
        loadPostsOld();
        final ScrollView profile_SV_Main = findViewById(R.id.profile_SV_Main);
        profile_SV_Main.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

                int scrollY = profile_SV_Main.getScrollY();

                if(scrollY < height/4-height/20) { // should hide the top bar
                    if(findViewById(R.id.profile_CL_TopBar).getVisibility() == View.VISIBLE) {
                        findViewById(R.id.profile_CL_TopBar).setVisibility(View.INVISIBLE);
                        profile_SV_Main.post(new Runnable() {
                            @Override
                            public void run() {
                                profile_SV_Main.fullScroll(View.FOCUS_UP); // little polish, works almost always
                            }
                        });
                    }
                } else if(scrollY >= height/4-height/20) { // should show the top bar
                    if(findViewById(R.id.profile_CL_TopBar).getVisibility() == View.INVISIBLE) findViewById(R.id.profile_CL_TopBar).setVisibility(View.VISIBLE);
                }

                if (profile_SV_Main.getHeight() == profile_SV_Main.getChildAt(0).getHeight() - profile_SV_Main.getScrollY()) {
                    //scroll view is at the bottom
                    loadPostsOld();
                }
            }
        });

        DatabaseReference dbRefNewPostsByUser = dbRootReference.child("posts_by_user/" + userUUID);
        ChildEventListener childEventListenerNewPostsByUser = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                dbRootReference.child("posts/" + dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mapPosts.add(UIHelper.addNewPost(ProfileActivity.this, ((LinearLayout) findViewById(R.id.profile_LL_Posts)), UIHelper.POSITION_TOP, dataSnapshot.getValue(Post.class), dataSnapshot.getKey(), true, false));
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
        };
        dbRefNewPostsByUser.orderByValue().startAt(firstPost+1).addChildEventListener(childEventListenerNewPostsByUser);
        realtimeMap2.put(dbRefNewPostsByUser, childEventListenerNewPostsByUser);

    } // onCreate()

    private void loadPostsOld(){ // down
        dbRootReference.child("posts_by_user/" + userUUID).orderByValue().endAt(lastPost-1).limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot DATA) {
                if(DATA.exists()){
                    final List<String> postsUUIDs = new ArrayList<>();
                    final List<Long> postsDates = new ArrayList<>();
                    for(DataSnapshot postData : DATA.getChildren()) {
                        postsUUIDs.add(postData.getKey());
                        postsDates.add(postData.getValue(Long.class));
                    }
                    lastPost = postsDates.get(0);

                    for(int i=postsUUIDs.size()-1; i > -1; i--) {
                        dbRootReference.child("posts/" + postsUUIDs.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mapPosts.add(UIHelper.addNewPost(ProfileActivity.this, ((LinearLayout) findViewById(R.id.profile_LL_Posts)), UIHelper.POSITION_BOTTOM, dataSnapshot.getValue(Post.class), dataSnapshot.getKey(), true, false));
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    } // loadPostsOld()

} // ProfileActivity{}
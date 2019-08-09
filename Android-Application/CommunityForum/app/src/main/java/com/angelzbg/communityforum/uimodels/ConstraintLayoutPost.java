package com.angelzbg.communityforum.uimodels;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.angelzbg.communityforum.MainActivity;
import com.angelzbg.communityforum.R;
import com.angelzbg.communityforum.models.Community;
import com.angelzbg.communityforum.models.Post;
import com.angelzbg.communityforum.utils.UIHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ConstraintLayoutPost extends ConstraintLayout {
    public ConstraintLayoutPost(Context context) { // DO NOT USE
        super(context);
    }

    public final HashMap<DatabaseReference, ValueEventListener> realtimeMap = new HashMap<>();

    private boolean isPostSaved = false, isPostVoted = false;

    public ConstraintLayoutPost(final Context context, final LinearLayout parent, final int position, final Post post, final String postUUID, boolean showCommunity) {
        super(context);

        final int height = UIHelper.height;
        final DatabaseReference dbRootReference = UIHelper.dbRootReference;

        this.setId(View.generateViewId());
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(height/160, height/160, height/160, height/160);
        if(position == UIHelper.POSITION_TOP) {
            parent.addView(this, 0, lp);
        } else {
            parent.addView(this, lp);
        }

        GradientDrawable gdWrapper = new GradientDrawable();
        gdWrapper.setColor(ContextCompat.getColor(context, R.color.white));
        gdWrapper.setStroke(height/800, ContextCompat.getColor(context, R.color.shadowGray));
        gdWrapper.setShape(GradientDrawable.RECTANGLE);
        gdWrapper.setCornerRadius(height/320);
        this.setBackground(gdWrapper);


        final ImageView IV_avatar = new ImageView(context);
        IV_avatar.setId(View.generateViewId());
        this.addView(IV_avatar);
        IV_avatar.getLayoutParams().width = height/18;
        IV_avatar.getLayoutParams().height = height/18;
        IV_avatar.setImageResource(R.drawable.icon_avatar_question);

        final TextView TV_communityName = new TextView(context);
        TV_communityName.setId(View.generateViewId());
        this.addView(TV_communityName);
        TV_communityName.setTextColor(ContextCompat.getColor(context, R.color.titleBlackish));
        TV_communityName.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/50);
        TV_communityName.setText(post.getCommunity());
        TV_communityName.setTypeface(UIHelper.font_roboto_medium);

        final TextView TV_userName = new TextView(context);
        TV_userName.setId(View.generateViewId());
        this.addView(TV_userName);
        TV_userName.setTextColor(ContextCompat.getColor(context, R.color.textBlackish));
        TV_userName.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/50);
        TV_userName.setText("Posted by " + post.getAuthor() + " \u00b7 " + new SimpleDateFormat("dd/MM/yy HH:mm").format(new Date(post.getDate())));
        TV_userName.setTypeface(UIHelper.font_roboto_regular);

        final TextView TV_title = new TextView(context);
        TV_title.setId(View.generateViewId());
        this.addView(TV_title);
        TV_title.setText(post.getTitle());
        TV_title.setTextColor(ContextCompat.getColor(context, R.color.titleBlackish));
        TV_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/32);
        TV_title.getLayoutParams().width = 0;
        TV_title.setTypeface(UIHelper.font_roboto_medium);

        final ImageView IV_Image = new ImageView(context);
        IV_Image.setId(View.generateViewId());
        this.addView(IV_Image);
        IV_Image.getLayoutParams().width = 0;
        IV_Image.setAdjustViewBounds(true);
        Bitmap postImage = UIHelper.StringToBitMap(post.getImage());
        if(postImage != null) {
            postImage = UIHelper.createRoundedRectBitmapPosts(postImage);
            IV_Image.setImageBitmap(postImage);
            postImage = null;
            System.gc();
        }

        final TextView TV_text = new TextView(context);
        TV_text.setId(View.generateViewId());
        this.addView(TV_text);
        TV_text.setTextColor(ContextCompat.getColor(context, R.color.textBlackish));
        TV_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/50);
        TV_text.setMaxLines(3);
        TV_text.getLayoutParams().width = 0;
        TV_text.setText(post.getText());
        TV_text.setTypeface(UIHelper.font_roboto_regular);
        TV_text.setEllipsize(TextUtils.TruncateAt.END);
        TV_text.setLinksClickable(true);
        Linkify.addLinks(TV_text, Linkify.WEB_URLS);

        final ImageView IV_Comments = new ImageView(context);
        IV_Comments.setId(View.generateViewId());
        this.addView(IV_Comments);
        IV_Comments.getLayoutParams().width = height/27;
        IV_Comments.getLayoutParams().height = height/27;
        IV_Comments.setImageResource(R.drawable.icon_comments);
        IV_Comments.setColorFilter(ContextCompat.getColor(context, R.color.textBlackish));

        final TextView TV_Comments = new TextView(context);
        TV_Comments.setId(View.generateViewId());
        this.addView(TV_Comments);
        TV_Comments.setTextColor(ContextCompat.getColor(context, R.color.textBlackish));
        TV_Comments.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/50);
        TV_Comments.setText(post.getComments_count() + "");
        TV_Comments.setTypeface(UIHelper.font_roboto_regular);


        final ImageView IV_Votes = new ImageView(context);
        IV_Votes.setId(View.generateViewId());
        this.addView(IV_Votes);
        IV_Votes.getLayoutParams().width = height/27;
        IV_Votes.getLayoutParams().height = height/27;
        IV_Votes.setImageResource(R.drawable.icon_vote);
        IV_Votes.setColorFilter(ContextCompat.getColor(context, R.color.textBlackish));


        final TextView TV_Votes = new TextView(context);
        TV_Votes.setId(View.generateViewId());
        this.addView(TV_Votes);
        TV_Votes.setTextColor(ContextCompat.getColor(context, R.color.textBlackish));
        TV_Votes.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/50);
        TV_Votes.setText(post.getVotes() + "");
        TV_Votes.setTypeface(UIHelper.font_roboto_regular);

        final ImageView IV_Share = new ImageView(context);
        IV_Share.setId(View.generateViewId());
        this.addView(IV_Share);
        IV_Share.getLayoutParams().width = height/27;
        IV_Share.getLayoutParams().height = height/27;
        IV_Share.setImageResource(R.drawable.icon_share);
        IV_Share.setColorFilter(ContextCompat.getColor(context, R.color.textBlackish));

        final ImageView IV_Save = new ImageView(context);
        IV_Save.setId(View.generateViewId());
        this.addView(IV_Save);
        IV_Save.setImageResource(R.drawable.icon_save_post);
        IV_Save.getLayoutParams().height = height/20;
        IV_Save.getLayoutParams().width = height/20;
        IV_Save.setColorFilter(ContextCompat.getColor(context, R.color.textBlackish));



        ConstraintSet cs = new ConstraintSet();
        cs.clone(this);
        cs.connect(IV_avatar.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, height/160);
        cs.connect(IV_avatar.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, height/80);

        cs.connect(TV_communityName.getId(), ConstraintSet.START, IV_avatar.getId(), ConstraintSet.END, height/160);
        cs.connect(TV_communityName.getId(), ConstraintSet.TOP, IV_avatar.getId(), ConstraintSet.TOP, height/320);

        cs.connect(TV_userName.getId(), ConstraintSet.START, IV_avatar.getId(), ConstraintSet.END, height/160);
        cs.connect(TV_userName.getId(), ConstraintSet.BOTTOM, IV_avatar.getId(), ConstraintSet.BOTTOM, height/320);

        cs.connect(TV_title.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, height/80);
        cs.connect(TV_title.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, height/80);
        cs.connect(TV_title.getId(), ConstraintSet.TOP, IV_avatar.getId(), ConstraintSet.BOTTOM, height/80);

        cs.connect(IV_Image.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, height/80);
        cs.connect(IV_Image.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, height/80);
        cs.connect(IV_Image.getId(), ConstraintSet.TOP, TV_title.getId(), ConstraintSet.BOTTOM, height/80);

        cs.connect(TV_text.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, height/80);
        cs.connect(TV_text.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, height/80);
        cs.connect(TV_text.getId(), ConstraintSet.TOP, IV_Image.getId(), ConstraintSet.BOTTOM, 0);


        cs.connect(IV_Comments.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
        cs.connect(IV_Comments.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
        cs.connect(IV_Comments.getId(), ConstraintSet.TOP, TV_text.getId(), ConstraintSet.BOTTOM, height/80);
        cs.connect(IV_Comments.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, height/80);

        cs.connect(TV_Comments.getId(), ConstraintSet.START, IV_Comments.getId(), ConstraintSet.END, 0);
        cs.connect(TV_Comments.getId(), ConstraintSet.TOP, IV_Comments.getId(), ConstraintSet.TOP, 0);
        cs.connect(TV_Comments.getId(), ConstraintSet.BOTTOM, IV_Comments.getId(), ConstraintSet.BOTTOM, 0);

        cs.connect(IV_Votes.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, height/80);
        cs.connect(IV_Votes.getId(), ConstraintSet.TOP, IV_Comments.getId(), ConstraintSet.TOP, 0);
        cs.connect(IV_Votes.getId(), ConstraintSet.BOTTOM, IV_Comments.getId(), ConstraintSet.BOTTOM, 0);

        cs.connect(TV_Votes.getId(), ConstraintSet.START, IV_Votes.getId(), ConstraintSet.END, 0);
        cs.connect(TV_Votes.getId(), ConstraintSet.TOP, IV_Votes.getId(), ConstraintSet.TOP, 0);
        cs.connect(TV_Votes.getId(), ConstraintSet.BOTTOM, IV_Votes.getId(), ConstraintSet.BOTTOM, 0);

        cs.connect(IV_Share.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, height/80);
        cs.connect(IV_Share.getId(), ConstraintSet.TOP, IV_Comments.getId(), ConstraintSet.TOP, 0);
        cs.connect(IV_Share.getId(), ConstraintSet.BOTTOM, IV_Comments.getId(), ConstraintSet.BOTTOM, 0);

        cs.connect(IV_Save.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, height/80);
        cs.connect(IV_Save.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);

        cs.applyTo(this);

        dbRootReference.child("users/" + post.getAuthor() + "/username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.getValue(String.class);
                TV_userName.setText("Posted by " + username + " \u00b7 " + new SimpleDateFormat("dd/MM/yy HH:mm").format(new Date(post.getDate())));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        if(!showCommunity) { // don't show the community -> show only the user
            TV_communityName.setVisibility(View.INVISIBLE);
            dbRootReference.child("users/" + post.getAuthor() + "/avatar").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String avatarString = dataSnapshot.getValue(String.class);
                    Bitmap avatar = UIHelper.StringToBitMap(avatarString);
                    if(avatar != null){
                        avatar = UIHelper.CropBitmapCenterCircle(avatar);
                        IV_avatar.setImageBitmap(avatar);
                        avatar = null;
                        System.gc();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

        } else { // don't sho the user avatar -> show the community
            dbRootReference.child("communities/" + post.getCommunity()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        Community community = dataSnapshot.getValue(Community.class);
                        TV_communityName.setText(community.getName());

                        Bitmap avatar = UIHelper.StringToBitMap(community.getAvatar());
                        if(avatar != null){
                            avatar = UIHelper.CropBitmapCenterCircle(avatar);
                            IV_avatar.setImageBitmap(avatar);
                            avatar = null;
                            System.gc();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

        }

        // Realtime Data
        final DatabaseReference refVotes = dbRootReference.child("posts/" + postUUID + "/votes");
        final ValueEventListener listenerVotes = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int votes = dataSnapshot.getValue(Integer.class);
                if(post.getVotes() != votes) {
                    TV_Votes.setText(votes + "");
                    post.setVotes(votes);
                    Animation anim_pop = AnimationUtils.loadAnimation(context, R.anim.anim_pop_votes_comments);
                    TV_Votes.startAnimation(anim_pop);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        refVotes.addValueEventListener(listenerVotes);
        realtimeMap.put(refVotes, listenerVotes);

        final DatabaseReference refPosts = dbRootReference.child("posts/" + postUUID + "/comments_count");
        final ValueEventListener listenerPosts = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int comments_count = dataSnapshot.getValue(Integer.class);
                if(comments_count != post.getComments_count()) {
                    TV_Comments.setText(comments_count + "");
                    IV_Comments.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                    TV_Comments.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    post.setComments_count(comments_count);
                    Animation anim_pop = AnimationUtils.loadAnimation(context, R.anim.anim_pop_votes_comments);
                    TV_Comments.startAnimation(anim_pop);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        refPosts.addValueEventListener(listenerPosts);
        realtimeMap.put(refPosts, listenerPosts);

        if(UIHelper.currentUser != null){ // user must be logged for these functionalities
            final DatabaseReference refSelfVote = dbRootReference.child("votes/" + postUUID + "/" + UIHelper.currentUser.getUid());
            final ValueEventListener listenerSelfVote = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        IV_Votes.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                        TV_Votes.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        isPostVoted = true;
                    } else {
                        IV_Votes.setColorFilter(ContextCompat.getColor(context, R.color.textBlackish));
                        TV_Votes.setTextColor(ContextCompat.getColor(context, R.color.textBlackish));
                        isPostVoted = false;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            };
            refSelfVote.addValueEventListener(listenerSelfVote);
            realtimeMap.put(refSelfVote, listenerSelfVote);

            final DatabaseReference refSavedPost = dbRootReference.child("saved_posts/" + UIHelper.currentUser.getUid() + "/" + postUUID);
            final ValueEventListener listenerSavedPost = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        IV_Save.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                        isPostSaved = true;
                    } else {
                        IV_Save.setColorFilter(ContextCompat.getColor(context, R.color.textBlackish));
                        isPostSaved = false;

                        if(parent.getId() == R.id.main_LL_SavedPosts){ // the post is located in the saved posts layout -> clear it
                            clearRealtime();
                            parent.removeView(ConstraintLayoutPost.this); // removing the view
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            };
            refSavedPost.addValueEventListener(listenerSavedPost);
            realtimeMap.put(refSavedPost, listenerSavedPost);

        }

        IV_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(UIHelper.currentUser != null) {
                    if(isPostSaved){
                        dbRootReference.child("saved_posts/" + UIHelper.currentUser.getUid() + "/" + postUUID).removeValue();
                        IV_Save.setColorFilter(ContextCompat.getColor(context, R.color.textBlackish));
                        isPostSaved = false;
                    } else {
                        dbRootReference.child("saved_posts/" + UIHelper.currentUser.getUid() + "/" + postUUID).setValue(ServerValue.TIMESTAMP);
                        IV_Save.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                        isPostSaved = true;
                    }
                } else {
                    if ( context instanceof MainActivity) {
                        ((MainActivity)context).openDrawer();
                    } else {
                        Toast.makeText(context, "You must be logged in in order to save posts!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        IV_Votes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(UIHelper.currentUser != null) {
                    if(isPostVoted){
                        dbRootReference.child("votes/" + postUUID + "/" + UIHelper.currentUser.getUid()).removeValue();
                        IV_Votes.setColorFilter(ContextCompat.getColor(context, R.color.textBlackish));
                        TV_Votes.setTextColor(ContextCompat.getColor(context, R.color.textBlackish));
                        isPostVoted = false;
                    } else {
                        dbRootReference.child("votes/" + postUUID + "/" + UIHelper.currentUser.getUid()).setValue(true);
                        IV_Votes.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                        TV_Votes.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        isPostVoted = true;
                    }
                } else {
                    if ( context instanceof MainActivity) {
                        ((MainActivity)context).openDrawer();
                    } else {
                        Toast.makeText(context, "You must be logged in in order to vote!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    } // constructor [ END ]

    public void clearRealtime() {
        for(Map.Entry<DatabaseReference, ValueEventListener> entry : realtimeMap.entrySet()) { // removing the realtime listeners
            DatabaseReference reference = entry.getKey();
            ValueEventListener listener = entry.getValue();

            reference.removeEventListener(listener);
        }
    }

} // ConstraintLayoutPost{}
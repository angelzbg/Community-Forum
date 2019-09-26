package com.angelzbg.communityforum.uimodels;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.angelzbg.communityforum.CommunityActivity;
import com.angelzbg.communityforum.R;
import com.angelzbg.communityforum.utils.UIHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ConstraintLayoutSavedCommunity extends ConstraintLayout {

    public final HashMap<DatabaseReference, ValueEventListener> realtimeMap = new HashMap<>();

    public ConstraintLayoutSavedCommunity(final Context context, final String communityUUID) {
        super(context);

        final ImageView IV_Avatar = new ImageView(context);
        IV_Avatar.setId(View.generateViewId());
        this.addView(IV_Avatar);
        IV_Avatar.getLayoutParams().width = UIHelper.height/11-UIHelper.height/80;
        IV_Avatar.getLayoutParams().height = IV_Avatar.getLayoutParams().width;

        ConstraintLayout CL_PostsWrap = new ConstraintLayout(context);
        CL_PostsWrap.setId(View.generateViewId());
        this.addView(CL_PostsWrap);
        GradientDrawable gdBoxWrap = new GradientDrawable();
        gdBoxWrap.setColor(ContextCompat.getColor(context, R.color.content_communities_backgr));
        gdBoxWrap.setShape(GradientDrawable.RECTANGLE);
        gdBoxWrap.setCornerRadius(UIHelper.height/65);
        CL_PostsWrap.setBackground(gdBoxWrap);
        final int paddingWrapper = UIHelper.height/400;
        CL_PostsWrap.setPadding(paddingWrapper,paddingWrapper,paddingWrapper,paddingWrapper);

        final TextView TV_Posts = new TextView(context);
        TV_Posts.setId(View.generateViewId());
        CL_PostsWrap.addView(TV_Posts);
        TV_Posts.setTextColor(ContextCompat.getColor(context, R.color.white));
        TV_Posts.setTextSize(TypedValue.COMPLEX_UNIT_PX, UIHelper.height/50);
        TV_Posts.setTypeface(UIHelper.font_roboto_medium);
        final GradientDrawable gdTextBack = new GradientDrawable();
        gdTextBack.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        gdTextBack.setShape(GradientDrawable.RECTANGLE);
        gdTextBack.setCornerRadius(UIHelper.height/80);
        TV_Posts.setBackground(gdTextBack);
        TV_Posts.setPadding(paddingWrapper*3,0,(int) (paddingWrapper*3.5),0);
        TV_Posts.setText("?");

        ConstraintSet cs = new ConstraintSet();
        cs.clone(this);
        cs.connect(IV_Avatar.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        cs.connect(IV_Avatar.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cs.connect(IV_Avatar.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        cs.connect(IV_Avatar.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        cs.connect(CL_PostsWrap.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        cs.connect(CL_PostsWrap.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        cs.applyTo(this);

        DatabaseReference dbRefAvatar = UIHelper.dbRootReference.child("communities/"+communityUUID+"/avatar");
        ValueEventListener listenerAvatar = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Bitmap avatarImage = UIHelper.StringToBitMap(dataSnapshot.getValue(String.class));
                if(avatarImage != null) {
                    avatarImage = UIHelper.CropBitmapCenterCircle(avatarImage);
                    IV_Avatar.setImageBitmap(avatarImage);
                    avatarImage = null;
                    System.gc();
                } else {
                    IV_Avatar.setImageResource(R.drawable.icon_avatar_question);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        dbRefAvatar.addValueEventListener(listenerAvatar);
        realtimeMap.put(dbRefAvatar, listenerAvatar);

        final boolean[] initialCount = {true};
        DatabaseReference dbRefPosts = UIHelper.dbRootReference.child("communities/"+communityUUID+"/posts_count");
        ValueEventListener listenerPosts = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!initialCount[0]) {
                    gdTextBack.setColor(ContextCompat.getColor(context, R.color.content_community_new_post));
                    TV_Posts.setBackground(gdTextBack);
                }
                initialCount[0] = false;
                TV_Posts.setText(dataSnapshot.getValue(Integer.class)+"");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        dbRefPosts.addValueEventListener(listenerPosts);
        realtimeMap.put(dbRefPosts, listenerPosts);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommunityActivity.class);
                intent.putExtra("communityUUID", communityUUID);
                context.startActivity(intent);
                gdTextBack.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
                TV_Posts.setBackground(gdTextBack);
            }
        });
    } // constructor()

    public void clearRealtime() {
        for(Map.Entry<DatabaseReference, ValueEventListener> entry : realtimeMap.entrySet()) { // removing the realtime listeners
            DatabaseReference reference = entry.getKey();
            ValueEventListener listener = entry.getValue();

            reference.removeEventListener(listener);
        }
    }

} // ConstraintLayoutSavedCommunity()
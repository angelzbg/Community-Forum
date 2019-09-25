package com.angelzbg.communityforum.uimodels;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.angelzbg.communityforum.R;
import com.angelzbg.communityforum.utils.UIHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConstraintLayoutFriend extends ConstraintLayout {

    public final HashMap<DatabaseReference, ValueEventListener> realtimeMap = new HashMap<>();

    public static final ArrayList<String> messages = new ArrayList<>();

    public ConstraintLayoutFriend(final Context context, final String friendUUID, final String chatUUID, final LinearLayout parent){
        super(context);

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(UIHelper.height/80, UIHelper.height/160, 0, UIHelper.height/160);
        parent.addView(this, lp);

        final ImageView IV_Avatar = new ImageView(context);
        IV_Avatar.setId(View.generateViewId());
        this.addView(IV_Avatar);
        IV_Avatar.getLayoutParams().width = UIHelper.height/20;
        IV_Avatar.getLayoutParams().height = IV_Avatar.getLayoutParams().width;


        final TextView TV_Username = new TextView(context);
        TV_Username.setId(View.generateViewId());
        this.addView(TV_Username);
        TV_Username.setTextColor(ContextCompat.getColor(context, R.color.textBlackish));
        TV_Username.setTextSize(TypedValue.COMPLEX_UNIT_PX, UIHelper.height/48);
        TV_Username.getLayoutParams().width = 0;
        TV_Username.setTypeface(UIHelper.font_roboto_medium);

        View V_Line = new View(context);
        V_Line.setId(View.generateViewId());
        this.addView(V_Line);
        V_Line.setBackgroundColor(ContextCompat.getColor(context, R.color.lightGray));
        V_Line.getLayoutParams().width = 0;
        V_Line.getLayoutParams().height = UIHelper.height/800;


        ConstraintSet cs = new ConstraintSet();
        cs.clone(this);
        cs.connect(IV_Avatar.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cs.connect(IV_Avatar.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, UIHelper.height/160);
        cs.connect(IV_Avatar.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, UIHelper.height/80);

        cs.connect(TV_Username.getId(), ConstraintSet.TOP, IV_Avatar.getId(), ConstraintSet.TOP);
        cs.connect(TV_Username.getId(), ConstraintSet.BOTTOM, IV_Avatar.getId(), ConstraintSet.BOTTOM);
        cs.connect(TV_Username.getId(), ConstraintSet.START, IV_Avatar.getId(), ConstraintSet.END, UIHelper.height/80);

        cs.connect(V_Line.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cs.connect(V_Line.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        cs.connect(V_Line.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        cs.applyTo(this);











        UIHelper.dbRootReference.child("users/"+friendUUID+"/username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TV_Username.setText(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        DatabaseReference dbRefAvatar = UIHelper.dbRootReference.child("users/"+friendUUID+"/avatar");
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

    }

    public void clearRealtime() {
        for(Map.Entry<DatabaseReference, ValueEventListener> entry : realtimeMap.entrySet()) { // removing the realtime listeners
            DatabaseReference reference = entry.getKey();
            ValueEventListener listener = entry.getValue();
            reference.removeEventListener(listener);
        }
    }

} // ConstraintLayoutFriend{}
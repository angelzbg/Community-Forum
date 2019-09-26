package com.angelzbg.communityforum.uimodels;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.angelzbg.communityforum.R;
import com.angelzbg.communityforum.models.Message;
import com.angelzbg.communityforum.utils.UIHelper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConstraintLayoutFriend extends ConstraintLayout {

    private final HashMap<DatabaseReference, ValueEventListener> realtimeMap = new HashMap<>();
    private final HashMap<Query, ChildEventListener> realtimeMap2 = new HashMap<>();

    public final ArrayList<Message> messages = new ArrayList<>();

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

        final ConstraintLayout CL_StateWrap = new ConstraintLayout(context);
        CL_StateWrap.setId(View.generateViewId());
        this.addView(CL_StateWrap);
        CL_StateWrap.getLayoutParams().width = IV_Avatar.getLayoutParams().width/3;
        CL_StateWrap.getLayoutParams().height = CL_StateWrap.getLayoutParams().width;
        GradientDrawable gdBoxWrap = new GradientDrawable();
        gdBoxWrap.setColor(ContextCompat.getColor(context, R.color.login_background));
        gdBoxWrap.setShape(GradientDrawable.OVAL);
        CL_StateWrap.setBackground(gdBoxWrap);
        CL_StateWrap.setVisibility(INVISIBLE);

        final View V_State = new View(context);
        V_State.setId(View.generateViewId());
        final int marginsStateWrap = UIHelper.height/400;
        ConstraintLayout.LayoutParams lp2 = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
        CL_StateWrap.addView(V_State, lp2);
        CL_StateWrap.setPadding(marginsStateWrap,marginsStateWrap,marginsStateWrap,marginsStateWrap);
        final GradientDrawable gdStateBack = new GradientDrawable();
        gdStateBack.setColor(ContextCompat.getColor(context, R.color.state_offline));
        gdStateBack.setShape(GradientDrawable.OVAL);
        //V_State.setBackground(gdStateBack);

        // Add view for the animation when new message is received


        ConstraintSet cs = new ConstraintSet();
        cs.clone(this);
        cs.connect(IV_Avatar.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cs.connect(IV_Avatar.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, UIHelper.height/160);
        cs.connect(IV_Avatar.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, UIHelper.height/80);

        cs.connect(CL_StateWrap.getId(), ConstraintSet.BOTTOM, IV_Avatar.getId(), ConstraintSet.BOTTOM);
        cs.connect(CL_StateWrap.getId(), ConstraintSet.END, IV_Avatar.getId(), ConstraintSet.END);

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

        DatabaseReference dbRefOnline = UIHelper.dbRootReference.child("online/"+friendUUID);
        ValueEventListener listenerOnline = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CL_StateWrap.setVisibility(VISIBLE);
                if(dataSnapshot.exists()){
                    gdStateBack.setColor(ContextCompat.getColor(context, R.color.state_online));
                } else {
                    gdStateBack.setColor(ContextCompat.getColor(context, R.color.state_offline));
                }
                V_State.setBackground(gdStateBack);
                // if chat is open with this friendUUID push the status there too
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        dbRefOnline.addValueEventListener(listenerOnline);
        realtimeMap.put(dbRefOnline, listenerOnline);

        // Messages
        Query queryChat = UIHelper.dbRootReference.child("chats/"+chatUUID+"/messages").orderByChild("date").startAt(System.currentTimeMillis());
        ChildEventListener listenerChat = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final Message msg = dataSnapshot.getValue(Message.class);
                messages.add(msg);
                // update animation and text color
                // when clicked should color back the text and remove the animation
                // if chat is open with this friendUUID push the message there and dont show these things
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
        queryChat.addChildEventListener(listenerChat);
        realtimeMap2.put(queryChat, listenerChat);
    }

    public void clearRealtime() {
        for(Map.Entry<DatabaseReference, ValueEventListener> entry : realtimeMap.entrySet()) { // removing the realtime listeners
            DatabaseReference reference = entry.getKey();
            ValueEventListener listener = entry.getValue();
            reference.removeEventListener(listener);
        }
        for(Map.Entry<Query, ChildEventListener> entry : realtimeMap2.entrySet()) { // removing the realtime listeners
            Query query = entry.getKey();
            ChildEventListener listener = entry.getValue();
            query.removeEventListener(listener);
        }
    }

} // ConstraintLayoutFriend{}
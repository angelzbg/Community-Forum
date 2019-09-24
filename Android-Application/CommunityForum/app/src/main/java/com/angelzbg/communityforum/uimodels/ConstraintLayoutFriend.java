package com.angelzbg.communityforum.uimodels;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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

import java.util.HashMap;
import java.util.Map;

public class ConstraintLayoutFriend extends ConstraintLayout {

    public final HashMap<DatabaseReference, ValueEventListener> realtimeMap = new HashMap<>();

    public ConstraintLayoutFriend(final Context context, final String friendUUID, final String chatUUID, final LinearLayout parent){
        super(context);

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        final int margins = UIHelper.height/160;
        lp.setMargins(margins, margins, margins, margins);
        parent.addView(this, lp);

        final ImageView IV_Avatar = new ImageView(context);
        IV_Avatar.setId(View.generateViewId());
        this.addView(IV_Avatar);
        IV_Avatar.getLayoutParams().width = UIHelper.height/16;
        IV_Avatar.getLayoutParams().height = IV_Avatar.getLayoutParams().width;


















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
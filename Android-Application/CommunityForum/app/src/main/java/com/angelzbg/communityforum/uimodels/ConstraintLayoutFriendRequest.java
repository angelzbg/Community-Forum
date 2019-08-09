package com.angelzbg.communityforum.uimodels;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.angelzbg.communityforum.R;
import com.angelzbg.communityforum.models.User;
import com.angelzbg.communityforum.utils.UIHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConstraintLayoutFriendRequest extends ConstraintLayout {

    public ConstraintLayoutFriendRequest(Context context) { // DO NOT USE
        super(context);
    }

    public ConstraintLayoutFriendRequest(Context context, String sender, long date) {
        super(context);

        final int height = UIHelper.height;
        final DatabaseReference dbRootReference = UIHelper.dbRootReference;

        this.setId(View.generateViewId());

        GradientDrawable gdWrapper = new GradientDrawable();
        gdWrapper.setColor(ContextCompat.getColor(context, R.color.white));
        gdWrapper.setStroke(height/800, ContextCompat.getColor(context, R.color.shadowGray));
        gdWrapper.setShape(GradientDrawable.RECTANGLE);
        gdWrapper.setCornerRadius(height/320);
        this.setBackground(gdWrapper);

        final ImageView IV_Avatar = new ImageView(context);
        IV_Avatar.setId(View.generateViewId());
        this.addView(IV_Avatar);
        IV_Avatar.getLayoutParams().width = height/14;
        IV_Avatar.getLayoutParams().height = height/14;
        IV_Avatar.setImageResource(R.drawable.icon_avatar_question);

        final TextView TV_Username = new TextView(context);
        TV_Username.setId(View.generateViewId());
        this.addView(TV_Username);
        TV_Username.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        TV_Username.setTextColor(ContextCompat.getColor(context, R.color.titleBlackish));
        TV_Username.setTypeface(UIHelper.font_roboto_medium);
        TV_Username.setText(sender);

        TextView TV_Date = new TextView(context);
        TV_Date.setId(View.generateViewId());
        this.addView(TV_Date);
        TV_Date.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        TV_Date.setTextColor(ContextCompat.getColor(context, R.color.textBlackish));
        TV_Date.setTypeface(UIHelper.font_roboto_regular);
        TV_Date.setText(new SimpleDateFormat("dd/MM/yy HH:mm").format(new Date(date)));


        TextView TV_Accept = new TextView(context);
        TV_Accept.setId(View.generateViewId());
        this.addView(TV_Accept);
        TV_Accept.setTextColor(Color.GREEN);
        TV_Accept.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        TV_Accept.setText("Accept");

        TextView TV_Decline = new TextView(context);
        TV_Decline.setId(View.generateViewId());
        this.addView(TV_Decline);
        TV_Decline.setTextColor(Color.BLUE);
        TV_Decline.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        TV_Decline.setText("Decline");

        TextView TV_Block = new TextView(context);
        TV_Block.setId(View.generateViewId());
        this.addView(TV_Block);
        TV_Block.setTextColor(Color.RED);
        TV_Block.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        TV_Block.setText("Block");

        ConstraintSet cs =  new ConstraintSet();
        cs.clone(this);
        cs.connect(IV_Avatar.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cs.connect(IV_Avatar.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        cs.connect(IV_Avatar.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        cs.connect(TV_Username.getId(), ConstraintSet.START, IV_Avatar.getId(), ConstraintSet.END, height/80);
        cs.connect(TV_Username.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

        cs.connect(TV_Date.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        cs.connect(TV_Username.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

        cs.connect(TV_Decline.getId(), ConstraintSet.START, IV_Avatar.getId(), ConstraintSet.END);
        cs.connect(TV_Decline.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        cs.connect(TV_Decline.getId(), ConstraintSet.BOTTOM, IV_Avatar.getId(), ConstraintSet.BOTTOM);

        cs.connect(TV_Accept.getId(), ConstraintSet.START, TV_Decline.getId(), ConstraintSet.END);
        cs.connect(TV_Accept.getId(), ConstraintSet.BOTTOM, TV_Decline.getId(), ConstraintSet.BOTTOM);
        cs.connect(TV_Accept.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        cs.connect(TV_Block.getId(), ConstraintSet.START, IV_Avatar.getId(), ConstraintSet.END);
        cs.connect(TV_Block.getId(), ConstraintSet.BOTTOM, TV_Decline.getId(), ConstraintSet.BOTTOM);
        cs.connect(TV_Block.getId(), ConstraintSet.END, TV_Decline.getId(), ConstraintSet.START);
        cs.applyTo(this);

        this.setPadding(height/80, height/80, height/80, height/80);

        dbRootReference.child("users/" + sender).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                TV_Username.setText(user.getUsername());
                Bitmap avatar = UIHelper.StringToBitMap(user.getAvatar());
                if(avatar != null) {
                    avatar = UIHelper.CropBitmapCenterCircle(avatar);
                    IV_Avatar.setImageBitmap(avatar);
                    avatar = null;
                    System.gc();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


    } // constructor()

} // ConstraintLayoutFriendRequest{}
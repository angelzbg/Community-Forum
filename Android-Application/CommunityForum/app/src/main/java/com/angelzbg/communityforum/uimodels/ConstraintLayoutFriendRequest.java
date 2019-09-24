package com.angelzbg.communityforum.uimodels;

import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.angelzbg.communityforum.MainActivity;
import com.angelzbg.communityforum.ProfileActivity;
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
    public ConstraintLayoutFriendRequest(final Context context, final LinearLayout parent, final String sender, long date) {
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
        IV_Avatar.getLayoutParams().width = height/12;
        IV_Avatar.getLayoutParams().height = height/12;
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


        final TextView TV_Accept = new TextView(context);
        TV_Accept.setId(View.generateViewId());
        this.addView(TV_Accept);
        TV_Accept.setTextColor(ContextCompat.getColor(context, R.color.color_accept));
        TV_Accept.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        TV_Accept.setText("Accept");
        GradientDrawable gdBackgrAccept = new GradientDrawable();
        gdBackgrAccept.setColor(ContextCompat.getColor(context, R.color.login_background));
        gdBackgrAccept.setStroke(height/400, ContextCompat.getColor(context, R.color.color_accept));
        gdBackgrAccept.setShape(GradientDrawable.RECTANGLE);
        gdBackgrAccept.setCornerRadius(height/320);
        TV_Accept.setBackground(gdBackgrAccept);
        TV_Accept.setPadding(height/50,height/200,height/50,height/200);
        TV_Accept.getLayoutParams().width = height/8;
        TV_Accept.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        final TextView TV_Decline = new TextView(context);
        TV_Decline.setId(View.generateViewId());
        this.addView(TV_Decline);
        TV_Decline.setTextColor(ContextCompat.getColor(context, R.color.color_decline));
        TV_Decline.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        TV_Decline.setText("Decline");
        GradientDrawable gdBackgrDecline = new GradientDrawable();
        gdBackgrDecline.setColor(ContextCompat.getColor(context, R.color.login_background));
        gdBackgrDecline.setStroke(height/400, ContextCompat.getColor(context, R.color.color_decline));
        gdBackgrDecline.setShape(GradientDrawable.RECTANGLE);
        gdBackgrDecline.setCornerRadius(height/320);
        TV_Decline.setBackground(gdBackgrDecline);
        TV_Decline.setPadding(height/50,height/200,height/50,height/200);
        TV_Decline.getLayoutParams().width = height/8;
        TV_Decline.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        final TextView TV_Block = new TextView(context);
        TV_Block.setId(View.generateViewId());
        this.addView(TV_Block);
        TV_Block.setTextColor(ContextCompat.getColor(context, R.color.color_block));
        TV_Block.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);
        TV_Block.setText("Block");
        GradientDrawable gdBackgrBlock = new GradientDrawable();
        gdBackgrBlock.setColor(ContextCompat.getColor(context, R.color.login_background));
        gdBackgrBlock.setStroke(height/400, ContextCompat.getColor(context, R.color.color_block));
        gdBackgrBlock.setShape(GradientDrawable.RECTANGLE);
        gdBackgrBlock.setCornerRadius(height/320);
        TV_Block.setBackground(gdBackgrBlock);
        TV_Block.setPadding(height/50,height/200,height/50,height/200);
        TV_Block.getLayoutParams().width = height/8;
        TV_Block.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        ConstraintSet cs =  new ConstraintSet();
        cs.clone(this);
        cs.connect(IV_Avatar.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cs.connect(IV_Avatar.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        cs.connect(IV_Avatar.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        cs.connect(TV_Username.getId(), ConstraintSet.START, IV_Avatar.getId(), ConstraintSet.END, height/80);
        cs.connect(TV_Username.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

        cs.connect(TV_Date.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        cs.connect(TV_Username.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

        cs.connect(TV_Decline.getId(), ConstraintSet.START, IV_Avatar.getId(), ConstraintSet.END, height/80);
        cs.connect(TV_Decline.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        cs.connect(TV_Decline.getId(), ConstraintSet.BOTTOM, IV_Avatar.getId(), ConstraintSet.BOTTOM);

        cs.connect(TV_Accept.getId(), ConstraintSet.BOTTOM, TV_Decline.getId(), ConstraintSet.BOTTOM);
        cs.connect(TV_Accept.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        cs.connect(TV_Block.getId(), ConstraintSet.START, IV_Avatar.getId(), ConstraintSet.END, height/80);
        cs.connect(TV_Block.getId(), ConstraintSet.BOTTOM, TV_Decline.getId(), ConstraintSet.BOTTOM);
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

        OnClickListener goToProfile = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("userUUID", sender);
                context.startActivity(intent);
            }
        };
        IV_Avatar.setOnClickListener(goToProfile);
        TV_Username.setOnClickListener(goToProfile);

        OnClickListener clickChoice = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == TV_Accept.getId()){
                    dbRootReference.child("acceptedFriendRequests/" + UIHelper.currentUser.getUid() + "/" + sender).setValue(true);
                }
                else if(v.getId() == TV_Decline.getId()){
                    dbRootReference.child("acceptedFriendRequests/" + UIHelper.currentUser.getUid() + "/" + sender).setValue(false);
                }
                else if(v.getId() == TV_Block.getId()){
                    dbRootReference.child("blocks/" + UIHelper.currentUser.getUid() + "/" + sender).setValue(true); // trigger will delete the friend request
                }
                parent.removeView(ConstraintLayoutFriendRequest.this);
                ((MainActivity)context).updateNotificationsCount();
            }
        };
        TV_Accept.setOnClickListener(clickChoice);
        TV_Decline.setOnClickListener(clickChoice);
        TV_Block.setOnClickListener(clickChoice);


    } // constructor()

} // ConstraintLayoutFriendRequest{}
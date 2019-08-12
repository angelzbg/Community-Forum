package com.angelzbg.communityforum.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.angelzbg.communityforum.R;
import com.angelzbg.communityforum.models.Community;
import com.angelzbg.communityforum.models.Post;
import com.angelzbg.communityforum.uimodels.ConstraintLayoutFriendRequest;
import com.angelzbg.communityforum.uimodels.ConstraintLayoutPost;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class UIHelper {

    // Fonts
    public static Typeface font_roboto_regular;
    public static Typeface font_roboto_light;
    public static Typeface font_roboto_medium;
    public static Typeface font_roboto_bold;
    public static Typeface font_roboto_black;

    public static FirebaseUser currentUser = null;
    public static DatabaseReference dbRootReference = null;

    public static int width = 0, height = 0;

    public static final int POSITION_TOP = 1, POSITION_BOTTOM = 2;
    public static ConstraintLayoutPost addNewPost(final Context context, final LinearLayout parent, final int position, final Post post, final String postUUID, boolean showCommunity) {
        final ConstraintLayoutPost CL_postWrapper = new ConstraintLayoutPost(context, parent, position, post, postUUID, showCommunity);
        return CL_postWrapper;
    } // addNewPost()


    public static void addNewNotification(Context context, LinearLayout parent, String sender, Long date) {
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(height/160, height/160, height/160, height/160);
        parent.addView(new ConstraintLayoutFriendRequest(context, parent, sender, date), 0, lp); // at the top
    } // addNewNotification()

    public static Bitmap CropBitmapCenterCircle(Bitmap b){
        Bitmap bitmap;
        if (b.getWidth() >= b.getHeight()){
            bitmap = Bitmap.createBitmap(
                    b,
                    b.getWidth()/2 - b.getHeight()/2,
                    0,
                    b.getHeight(),
                    b.getHeight()
            );
        }else{
            bitmap = Bitmap.createBitmap(
                    b,
                    0,
                    b.getHeight()/2 - b.getWidth()/2,
                    b.getWidth(),
                    b.getWidth()
            );
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, 250, 250, true);

        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader (bitmap,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);
        Canvas c = new Canvas(circleBitmap);
        c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);

        return circleBitmap;
    }

    public static String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.NO_WRAP);
        return imageEncoded;
    }
    public static Bitmap StringToBitMap(String encodedString){
        byte[] decodedByte;
        try {
            decodedByte = Base64.decode(encodedString, 0);
        } catch(Exception e) {
            return null;
        }
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static Bitmap resizeBitmapTo1024pxMax(Bitmap bitmap){
        if(bitmap==null) return null;

        // Оразмеряване преди изпращане
        final int maxWidth=1024, maxHeight=1024;
        int finalWidth = 1024, finalHeight = 1024;
        int bitmapWidth = bitmap.getWidth(), bitmapHeight = bitmap.getHeight();

        if(bitmapWidth > maxWidth || bitmapHeight > maxHeight) { // трябва оразмеряване

            if(bitmapWidth > maxWidth && bitmapHeight <= maxHeight) { // само ширината излиза извън размера
                //Toast.makeText(getApplicationContext(), "Само ширината излиза извън размера", Toast.LENGTH_LONG).show();
                finalWidth = 1024;
                double scale = (bitmapWidth*1.00) / (bitmapHeight*1.00);
                finalHeight = (int) (finalWidth / scale);

            } else if (bitmapHeight > maxHeight && bitmapWidth <= maxWidth) { // само височината излиза извън размера
                //Toast.makeText(getApplicationContext(), "Само височината излиза извън размера", Toast.LENGTH_LONG).show();
                finalHeight = 1024;
                double scale = (bitmapHeight*1.00)/(bitmapWidth*1.00);
                finalWidth = (int)(finalHeight/scale);

            } else { // и двете излизат извън размера
                //Toast.makeText(getApplicationContext(), "И двете излизат извън размера", Toast.LENGTH_LONG).show();

                if(bitmapWidth > bitmapHeight) { // ширината е по-голяма
                    //Toast.makeText(getApplicationContext(), "И двете излизат извън размера -> ширината е по-голяма", Toast.LENGTH_LONG).show();
                    finalWidth = 1024;
                    double scale = (bitmapWidth*1.00) / (bitmapHeight*1.00);
                    finalHeight = (int) (finalWidth / scale);

                } else if (bitmapHeight > bitmapWidth) { // височината е по-голяма
                    //Toast.makeText(getApplicationContext(), "И двете излизат извън размера -> височината е по-голяма", Toast.LENGTH_LONG).show();
                    finalHeight = 1024;
                    double scale = (bitmapHeight*1.00)/(bitmapWidth*1.00);
                    finalWidth = (int)(finalHeight/scale);

                } else if(bitmapHeight == bitmapWidth) { // равни са -> квадрат
                    //Toast.makeText(getApplicationContext(), "И двете излизат извън размера -> квадрат", Toast.LENGTH_LONG).show();
                    finalWidth = 1024;
                    finalHeight = 1024;
                }

            }

        } else { // не трябва оразмряване
            return bitmap;
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }//end of resizeBitmapTo1024pxMax()

    public static Bitmap createRoundedRectBitmapPosts(@NonNull Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = Color.WHITE;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        Path path = new Path();
        final int edgeSize = height/160;
        float[] radii = new float[]{
                edgeSize, edgeSize,
                edgeSize, edgeSize,
                edgeSize, edgeSize,
                edgeSize, edgeSize
        };

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        path.addRoundRect(rectF, radii, Path.Direction.CW);
        canvas.drawPath(path, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

} // UIHelper{}
package com.angelzbg.communityforum;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private long topDate = 1564591148286L, bottomDate = 1564591148286L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("test");


        final ScrollView sv_test = findViewById(R.id.sv_test);
        final LinearLayout ll_test = findViewById(R.id.ll_test);

        myRef.orderByChild("date").endAt(bottomDate).limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot DATA) {
                if(DATA.exists()){

                    List<TestData> list = new ArrayList<>();

                    for(DataSnapshot dataSnapshot : DATA.getChildren()){
                        list.add(dataSnapshot.getValue(TestData.class));
                    }

                    Collections.reverse(list);

                    for(TestData td : list){
                        TextView TV = new TextView(getApplicationContext());
                        TV.setId(View.generateViewId());
                        TV.setTextColor(Color.BLACK);
                        TV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 100);
                        TV.setText(td.getName());
                        ll_test.addView(TV);
                        bottomDate = td.getDate();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        sv_test.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (sv_test.getHeight() == sv_test.getChildAt(0).getHeight() - sv_test.getScrollY()) {
                    //scroll view is at the bottom
                    myRef.orderByChild("date").endAt(bottomDate-1).limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot DATA) {
                            if(DATA.exists()){

                                List<TestData> list = new ArrayList<>();

                                for(DataSnapshot dataSnapshot : DATA.getChildren()){
                                    list.add(dataSnapshot.getValue(TestData.class));
                                }

                                Collections.reverse(list);

                                for(TestData td : list){
                                    TextView TV = new TextView(getApplicationContext());
                                    TV.setId(View.generateViewId());
                                    TV.setTextColor(Color.BLACK);
                                    TV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 100);
                                    TV.setText(td.getName());
                                    ll_test.addView(TV);
                                    bottomDate = td.getDate();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else if(sv_test.getScrollY() == 0) {
                    //scroll view is at the top
                    myRef.orderByChild("date").startAt(topDate+1).limitToFirst(5).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot DATA) {
                            if(DATA.exists()){
                                for (DataSnapshot dataSnapshot : DATA.getChildren()) {
                                    TestData td = dataSnapshot.getValue(TestData.class);
                                    TextView TV = new TextView(getApplicationContext());
                                    TV.setId(View.generateViewId());
                                    TV.setTextColor(Color.BLACK);
                                    TV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 100);
                                    TV.setText(td.getName());
                                    ll_test.addView(TV, 0);
                                    topDate = td.getDate();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    } // onCreate()



} // MainActivity{}
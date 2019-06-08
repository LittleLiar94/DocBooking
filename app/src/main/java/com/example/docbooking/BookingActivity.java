package com.example.docbooking;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CpuUsageInfo;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.example.docbooking.Adapter.MyViewPagerAdapter;
import com.example.docbooking.Common.Common;
import com.example.docbooking.Common.NonSwipeViewPager;
import com.example.docbooking.Model.Barber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shuhart.stepview.StepView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class BookingActivity extends AppCompatActivity {

    LocalBroadcastManager localBroadcastManager;
    AlertDialog dialog;
    CollectionReference barberRef;


    @BindView(R.id.step_view)
    StepView stepView;
    @BindView(R.id.view_pager)
    NonSwipeViewPager viewPager;
    @BindView(R.id.btn_previous_step)
    Button btn_previous_step;
    @BindView(R.id.btn_next_step)
    Button btn_next_step;

    //Event
    @OnClick(R.id.btn_previous_step)
    void previousClick(){
        if (Common.step == 3 || Common.step > 0){
            Common.step--;
            viewPager.setCurrentItem(Common.step);

            if(Common.step < 3) { // Always enable NEXT when step <3
                btn_next_step.setEnabled(true);
                setColorButton();
            }
        }

    }

    @OnClick(R.id.btn_next_step)
    void nextClick(){

        //Debug line
        // Toast.makeText(this,""+Common.currentSaloon.getSaloonID(),Toast.LENGTH_SHORT).show();

        if(Common.step < 3 || Common.step == 0){

            Common.step++; //Increment

            if(Common.step == 1){ // After saloon is selected
                if(Common.currentSaloon != null)
                    loadBarberOfSaloon(Common.currentSaloon.getSaloonID());
            }
            else if(Common.step == 2) { // Pick time slot
                if(Common.currentBarber != null)
                    loadTimeSlotOfBarber(Common.currentBarber.getBarberID());

            }
            else if(Common.step == 3) { // Confirm
                if(Common.currentTimeSlot != -1)
                    confirmBooking();


            }
            viewPager.setCurrentItem(Common.step);

        }
    }

    // Send broadcast to fragment step 4
    private void confirmBooking() {
        Intent intent = new Intent(Common.KEY_CONFIRM_BOOKING);
        localBroadcastManager.sendBroadcast(intent);

    }

    // Send Local BroadCast to BookingStep3Fragment after we receive selected barber from
    // BookingStep2Fragment
    private void loadTimeSlotOfBarber(String barberID) {
        Intent intent = new Intent(Common.KEY_DISPLAY_TIME_SLOT);
        localBroadcastManager.sendBroadcast(intent);

    }

    // Send Local Broadcast to BookingStep2Fragment to load Recycler
    private void loadBarberOfSaloon(String saloonID) {
        dialog.show();

        // Select all the barber of Selected saloon.
        // /AllSaloon/NewYork/Branch/gcn5ulYdj0IuYKRkBw8Y/Barber
        if(!TextUtils.isEmpty(Common.city)){
            barberRef = FirebaseFirestore.getInstance()
                    .collection("AllSaloon")
                    .document(Common.city)
                    .collection("Branch")
                    .document(saloonID)
                    .collection("Barber");
            barberRef.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            ArrayList<Barber> barbers = new ArrayList<>();
                            for(QueryDocumentSnapshot barberSnapShot:task.getResult()){

                                Barber barber = barberSnapShot.toObject(Barber.class);
                                barber.setPassword(""); // Remove password because this is client app
                                barber.setBarberID(barberSnapShot.getId()); // Get barber ID

                                barbers.add(barber);
                            }
                            // Send Broadcast to Bookingstep2Fragment to load Recycler
                            Intent intent = new Intent(Common.KEY_BARBER_LOAD_DONE);
                            intent.putParcelableArrayListExtra(Common.KEY_BARBER_LOAD_DONE, barbers);
                            localBroadcastManager.sendBroadcast(intent);
                            dialog.dismiss();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                        }
                    });
        }
    }



    //Broadcast receiver
    private BroadcastReceiver buttonNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int step = intent.getIntExtra(Common.KEY_STEP,0);

            if(step == 1)
                Common.currentSaloon = intent.getParcelableExtra(Common.KEY_SALOON_STORE);
            else if(step == 2)
                Common.currentBarber = intent.getParcelableExtra(Common.KEY_BARBER_SELECTED);
            else if(step == 3)
                Common.currentTimeSlot = intent.getIntExtra(Common.KEY_TIME_SLOT, -1);

            btn_next_step.setEnabled(true);
            setColorButton();
        }
    };

    @Override
    protected void onDestroy() {
        localBroadcastManager.unregisterReceiver(buttonNextReceiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        ButterKnife.bind(BookingActivity.this);

        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver((buttonNextReceiver), new IntentFilter(Common.KEY_ENABLE_BUTTON_NEXT));

        setupStepView();
        setColorButton();

        //View
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4); // We have 4 fragment so we need to keep the state these pages
        viewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

                // Show step at top
                stepView.go(i, true);

                if(i==0)
                    btn_previous_step.setEnabled(false);
                else
                    btn_previous_step.setEnabled(true);

                //Disable button next here
                btn_next_step.setEnabled(false);

                setColorButton();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private void setColorButton() {
        if (btn_next_step.isEnabled()) {
            btn_next_step.setBackgroundResource(R.color.colorButton);
        } else {
            btn_next_step.setBackgroundResource(android.R.color.darker_gray);
        }
        if (btn_previous_step.isEnabled()) {
            btn_previous_step.setBackgroundResource(R.color.colorButton);
        } else {
            btn_previous_step.setBackgroundResource(android.R.color.darker_gray);

        }
    }

    private void setupStepView() {

        List<String>stepList = new ArrayList<>();
        stepList.add("Saloon");
        stepList.add("Barber");
        stepList.add("Time");
        stepList.add("Confirm");
        stepView.setSteps(stepList);
    }
}

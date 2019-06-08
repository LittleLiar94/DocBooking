package com.example.docbooking.Fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.docbooking.Common.Common;
import com.example.docbooking.Model.BookingInformation;
import com.example.docbooking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class BookingStep4Fragment extends Fragment {

    SimpleDateFormat simpleDateFormat;

    LocalBroadcastManager localBroadcastManager;

    AlertDialog dialog;

    @BindView(R.id.txt_booking_barber_text)
    TextView txt_booking_barber_text;
    @BindView(R.id.txt_booking_time_next)
    TextView txt_booking_time_next;
    @BindView(R.id.txt_saloon_address)
    TextView txt_saloon_address;
    @BindView(R.id.txt_saloon_name)
    TextView txt_saloon_name;
    @BindView(R.id.txt_saloon_open_hours)
    TextView txt_saloon_open_hours;
    @BindView(R.id.txt_saloon_phone)
    TextView txt_saloon_phone;
    @BindView(R.id.txt_saloon_website)
    TextView txt_saloon_website;

    @OnClick(R.id.btn_confirm)
    void confirmBooking(){

        dialog.show();

        // Process Timestamp
        // We will use Timestamp to filter all booking with date is greater than today
        // This is to display all the future booking
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-"); //Split (i.e 9:00 - 10:00)
        //Get start time : get 9:00
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); // we get 9
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // we get 00

        Calendar bookingDateWithOurHouse = Calendar.getInstance();
        bookingDateWithOurHouse.setTimeInMillis(Common.bookingDate.getTimeInMillis());
        bookingDateWithOurHouse.set(Calendar.HOUR_OF_DAY, startHourInt);
        bookingDateWithOurHouse.set(Calendar.MINUTE, startMinInt);

        // Create timestamp object and apply it to BookingInformation
        Timestamp timestamp = new Timestamp(bookingDateWithOurHouse.getTime());

        // Create booking information
        final BookingInformation bookingInformation = new BookingInformation();

        bookingInformation.setCityBook(Common.city);
        bookingInformation.setTimestamp(timestamp);
        bookingInformation.setDone(false); //Always FALSE we will use this filed to filter for display on user
        bookingInformation.setBarberID(Common.currentBarber.getBarberID());
        bookingInformation.setBarberName(Common.currentBarber.getName());
        bookingInformation.setCustomerName(Common.currentUser.getName());
        bookingInformation.setCustomerPhone(Common.currentUser.getPhoneNumber());
        bookingInformation.setSaloonID(Common.currentSaloon.getSaloonID());
        bookingInformation.setSaloonAddress(Common.currentSaloon.getAddress());
        bookingInformation.setSaloonName(Common.currentSaloon.getName());
        bookingInformation.setTime(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(bookingDateWithOurHouse.getTime())).toString());
        bookingInformation.setSlot(Long.valueOf(Common.currentTimeSlot));

        // Submit to Barber document
        final DocumentReference bookingDate = FirebaseFirestore.getInstance()
                .collection("AllSaloon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSaloon.getSaloonID())
                .collection("Barber")
                .document(Common.currentBarber.getBarberID())
                .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                .document(String.valueOf(Common.currentTimeSlot));

        // Write data
        bookingDate.set(bookingInformation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addToUserBooking(bookingInformation);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToUserBooking(final BookingInformation bookingInformation) {

        final CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        // Get Current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,0);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);

        Timestamp todayTimeStamp = new Timestamp(calendar.getTime());

        // Check if the document exist in this collection
        userBooking
                .whereGreaterThanOrEqualTo("timestamp", todayTimeStamp)
                .whereEqualTo("done", false)
                .limit(1) // Only take 1
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().isEmpty()){
                    //Set data
                    userBooking.document()
                            .set(bookingInformation)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    if(dialog.isShowing())
                                        dialog.dismiss();

                                    addToCalendar(Common.bookingDate,
                                            Common.convertTimeSlotToString(Common.currentTimeSlot));
                                    

                                    resetStaticData(); // Reset activity for next booking.
                                    getActivity().finish(); // Close activity
                                    Toast.makeText(getContext(),"Success", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else{

                    if(dialog.isShowing())
                        dialog.dismiss();

                    resetStaticData(); // Reset activity for next booking.
                    getActivity().finish(); // Close activity
                    Toast.makeText(getContext(),"Success", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addToCalendar(Calendar bookingDate, String startDate) {
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-"); //Split (i.e 9:00 - 10:00)
        //Get start time : get 9:00
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); // we get 9
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // we get 00

        String[] endTimeConvert = convertTime[1].split(":");
        int endHourInt = Integer.parseInt(endTimeConvert[0].trim()); // we get 10
        int endMinInt = Integer.parseInt(endTimeConvert[1].trim()); // we get 00

        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY, startHourInt); // set event start hour
        startEvent.set(Calendar.MINUTE, startMinInt);  // set even start min

        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY, endHourInt);
        endEvent.set(Calendar.MINUTE, endMinInt);

        // After we have startEvent and endEvent, convert it to string format
        SimpleDateFormat calanderDateFormat = new SimpleDateFormat("dd_MM_yyyy HH:mm");
        String startEventTime = calanderDateFormat.format(startEvent.getTime());
        String endEventTime = calanderDateFormat.format(endEvent.getTime());
        addToDeviceCalendar(startEventTime, endEventTime, "Haircut Booking",
                new StringBuilder("Haircut from ")
            .append(startTime)
            .append(" with ")
            .append(Common.currentBarber.getName())
            .append(" at ")
            .append(Common.currentSaloon.getName()).toString(),
                    new StringBuilder("Address: ")
                            .append(Common.currentSaloon.getAddress()).toString());

    }

    private void addToDeviceCalendar(String startEventTime, String endEventTime, String title, String description, String location) {

        SimpleDateFormat calanderDateFormat = new SimpleDateFormat("dd_MM_yyyy HH:mm");

        try{
            Date start = calanderDateFormat.parse(startEventTime);
            Date end   = calanderDateFormat.parse(endEventTime);

            ContentValues event = new ContentValues();

            //put
            event.put(CalendarContract.Events.CALENDAR_ID, 1);
            event.put(CalendarContract.Events.CALENDAR_ID, getCalendar(getContext()));
            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.DESCRIPTION, description);
            event.put(CalendarContract.Events.EVENT_LOCATION, location);

            // Time
            event.put(CalendarContract.Events.DTSTART, start.getTime());
            event.put(CalendarContract.Events.DTEND, end.getTime());
            event.put(CalendarContract.Events.ALL_DAY, 0);
            event.put(CalendarContract.Events.HAS_ALARM, 1);

            String timeZone = TimeZone.getDefault().getID();
            event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);

            Uri calendars;
            if( Build.VERSION.SDK_INT >= 8)
                calendars =  Uri.parse("content://com.android.calendar/events");
            else
                calendars =  Uri.parse("content://calendar/events");

            Uri uri_save = getActivity().getContentResolver().insert(calendars,event);
            // Save to cache
            Paper.init(getActivity());
            Paper.book().write(Common.EVENT_URI_CACHE, uri_save.toString());



        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    //  Calendar of Gmail
    private String getCalendar(Context context) {
        // Get default calendar ID of Calendar of Gmail
        String gmailIdCalendar = "";
        String projection[] = {"_id", "calendar_displayName"};

        Uri calendars;
        if( Build.VERSION.SDK_INT >= 8)
            calendars =  Uri.parse("content://com.android.calendar/events");
        else
            calendars =  Uri.parse("content://calendar/events");
        ContentResolver contentResolver = context.getContentResolver();

        // Select all calendar
        Cursor managedCursor = contentResolver.query(calendars, projection,null,null,null);

        if(managedCursor.moveToFirst()){
            String calName;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);

            do {
                calName = managedCursor.getString(nameCol);
                if(calName.contains("@gmail.com")){
                    gmailIdCalendar = managedCursor.getString(idCol);
                    break; // Exist as soon as we have the ID
                }
            }while (managedCursor.moveToNext());
            managedCursor.close();

        }
        return gmailIdCalendar;
    }


    private void resetStaticData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentSaloon = null;
        Common.currentBarber = null;
        Common.bookingDate.add(Calendar.DATE, 0); // Current date added

    }


    Unbinder unbinder;

    BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setData();

        }
    };

    private void setData() {
        txt_booking_barber_text.setText(Common.currentBarber.getName());
        txt_booking_time_next.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
        .append(" at ")
        .append(simpleDateFormat.format(Common.bookingDate.getTime())));

        txt_saloon_address.setText(Common.currentSaloon.getAddress());
        txt_saloon_website.setText(Common.currentSaloon.getWebsite());
        txt_saloon_name.setText(Common.currentSaloon.getName());
        txt_saloon_phone.setText(Common.currentSaloon.getPhone());
        txt_saloon_open_hours.setText(Common.currentSaloon.getOpenHours());


    }

    static BookingStep4Fragment instance;
    public static BookingStep4Fragment getInstance(){
        if(instance == null)
            instance = new BookingStep4Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply format for date display on CONFIRM
        simpleDateFormat  = new SimpleDateFormat("dd/MM/yyy");

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));

        // init Dialog
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext())
                .build();


    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step4,container,false);
        unbinder = ButterKnife.bind(this, itemView);


        return itemView;
    }
}

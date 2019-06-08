package com.example.docbooking.Interface;

import com.example.docbooking.Model.BookingInformation;

public interface IBookingInfoLoadListener {
    void onBookingInfoLoadEmpty();
    void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String documentID);
    void onBookingInfoLoadFailed(String message);


}

package com.example.docbooking.Model;

import android.icu.text.Replaceable;
import android.os.Parcel;
import android.os.Parcelable;

public class Saloon implements Parcelable {
    private String name, address, website, phone, openHours, saloonID;

    public Saloon() {
    }

    protected Saloon(Parcel in) {
        name = in.readString();
        address = in.readString();
        website = in.readString();
        phone = in.readString();
        openHours = in.readString();
        saloonID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(website);
        dest.writeString(phone);
        dest.writeString(openHours);
        dest.writeString(saloonID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Saloon> CREATOR = new Creator<Saloon>() {
        @Override
        public Saloon createFromParcel(Parcel in) {
            return new Saloon(in);
        }

        @Override
        public Saloon[] newArray(int size) {
            return new Saloon[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOpenHours() {
        return openHours;
    }

    public void setOpenHours(String openHours) {
        this.openHours = openHours;
    }

    public String getSaloonID() {
        return saloonID;
    }

    public void setSaloonID(String saloonID) {
        this.saloonID = saloonID;
    }
}

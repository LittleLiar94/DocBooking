package com.example.docbooking.Interface;

import com.example.docbooking.Model.Saloon;

import java.util.List;

public interface IBranchLoadListener {

    void onBranchLoadSuccess(List<Saloon> saloonList);
    void onBranchLoadFailed(String message);

}

package com.example.docbooking.Interface;

import java.util.List;

public interface IAllSaloonLoadListener {

    void onAllSaloonLoadSuccess(List<String> areaNameList);
    void onAllSaloonLoadFailed(String message);

}

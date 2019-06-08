package com.example.docbooking.Interface;

import com.example.docbooking.Model.ShoppingItem;

import java.util.List;

public interface IShoppingDataLoadListener {

    void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList);
    void onShoppingDataLoadFailed(String message);

}

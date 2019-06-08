package com.example.docbooking.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.docbooking.Adapter.MySaloonAdapter;
import com.example.docbooking.Common.Common;
import com.example.docbooking.Common.SpacesItemDecoration;
import com.example.docbooking.Interface.IAllSaloonLoadListener;
import com.example.docbooking.Interface.IBranchLoadListener;
import com.example.docbooking.Model.Saloon;
import com.example.docbooking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class BookingStep1Fragment extends Fragment implements IAllSaloonLoadListener, IBranchLoadListener {

    // Variable
    CollectionReference allSaloonRef;
    CollectionReference branchRef;

    IAllSaloonLoadListener iAllSaloonLoadListener;
    IBranchLoadListener iBranchLoadListener;

    // spinner - ID for the area dropdown menu
    @BindView(R.id.spinner)
    MaterialSpinner spinner;
    // recycler_saloon - ID for area below the spinner
    @BindView(R.id.recycler_saloon)
    RecyclerView recycler_saloon;

    Unbinder unbinder;

    AlertDialog dialog; // This shows the loading ...

    static BookingStep1Fragment instance;
    public static BookingStep1Fragment getInstance(){
        if(instance == null)
            instance = new BookingStep1Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allSaloonRef = FirebaseFirestore.getInstance().collection("AllSaloon");

        iAllSaloonLoadListener = this;
        iBranchLoadListener = this;

        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getActivity()).build();
        
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step1,container,false);
        unbinder = ButterKnife.bind(this,itemView);

        initView();
        
        loadAllSaloon();
        
        return itemView;
    }

    private void initView() {
        recycler_saloon.setHasFixedSize(true);
        recycler_saloon.setLayoutManager(new GridLayoutManager(getActivity(),2));
        recycler_saloon.addItemDecoration(new SpacesItemDecoration(4));
    }

    private void loadAllSaloon() {

        allSaloonRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            List<String> list = new ArrayList<>();
                            list.add("Please choose city");
                            for (QueryDocumentSnapshot documentSnapshot:task.getResult())
                                list.add(documentSnapshot.getId());
                            iAllSaloonLoadListener.onAllSaloonLoadSuccess(list);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iAllSaloonLoadListener.onAllSaloonLoadFailed(e.getMessage());
            }
        });
    }

    // After loading success, load the branch in the areaNameList selected
    @Override
    public void onAllSaloonLoadSuccess(List<String> areaNameList) {
        //Load all the item in areaNameList
        spinner.setItems(areaNameList);

        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if(position > 0){
                    loadBranchOfCity(item.toString());
                }
                else // If "Please Choose City" is selected, all saloon gone
                    recycler_saloon.setVisibility(View.GONE);
            }
        });
    }

    // Grab all the branch from firebase store
    private void loadBranchOfCity(String cityName) {

        dialog.show();

        Common.city = cityName;

        branchRef = FirebaseFirestore.getInstance()
                .collection("AllSaloon")
                .document(cityName)
                .collection("Branch");

        branchRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Saloon> list = new ArrayList<>();
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult()){
                        Saloon saloon = documentSnapshot.toObject(Saloon.class);
                        saloon.setSaloonID(documentSnapshot.getId());
                        list.add(saloon);
                    }

                    iBranchLoadListener.onBranchLoadSuccess(list);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iBranchLoadListener.onBranchLoadFailed(e.getMessage());
            }
        });

    }

    @Override
    public void onAllSaloonLoadFailed(String message) {
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }

    // Once success, generate the saloon name in cardview through MySaloonAdapter before set it
    // visible to user.
    @Override
    public void onBranchLoadSuccess(List<Saloon> saloonList) {

        MySaloonAdapter adapter = new MySaloonAdapter(getActivity(),saloonList);
        recycler_saloon.setAdapter(adapter);
        recycler_saloon.setVisibility(View.VISIBLE);
        dialog.dismiss();

    }

    @Override
    public void onBranchLoadFailed(String message) {
        Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }
}

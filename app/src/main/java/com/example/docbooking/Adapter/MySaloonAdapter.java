package com.example.docbooking.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.docbooking.Common.Common;
import com.example.docbooking.Interface.IRecyclerItemSelectedListener;
import com.example.docbooking.Model.Saloon;
import com.example.docbooking.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Inflate layout_saloon and substitute the content (i.k.a name & addr) with the one in FireBase
public class MySaloonAdapter extends RecyclerView.Adapter<MySaloonAdapter.MyViewHolder> {

    Context context;
    List<Saloon> saloonList;  // This list came from Model.Saloon
    List<CardView> cardViewList;

    // When a saloon is selected, we need to tell the BookingActivity to enable the NEXT button
    LocalBroadcastManager localBroadcastManager;


    public MySaloonAdapter(Context context, List<Saloon> saloonList) {
        this.context = context;
        this.saloonList = saloonList;
        cardViewList = new ArrayList<>();
        localBroadcastManager = LocalBroadcastManager.getInstance(context);

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_saloon,viewGroup,false);
        return new MyViewHolder(itemView);
    }

    //Replace the name in myViewHolder with the one in saloonList
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        // getName() and getAddress() came from Model.Saloon
        myViewHolder.txt_saloon_name.setText(saloonList.get(i).getName());
        myViewHolder.txt_saloon_address.setText(saloonList.get(i).getAddress());


        if(!cardViewList.contains(myViewHolder.card_saloon))
            cardViewList.add(myViewHolder.card_saloon);

        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int pos) {
                //Set white background for all card not been selected
                for (CardView cardView:cardViewList)
                    cardView.setBackgroundColor(context.getResources().getColor(android.R.color.white));

                //Set selected BG for only selected item
                myViewHolder.card_saloon.setBackgroundColor(context.getResources()
                .getColor(android.R.color.holo_orange_dark));

                //Send Broadcast to tell BookingActivity to enable Button NEXT
                Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                intent.putExtra(Common.KEY_SALOON_STORE, saloonList.get(pos));
                intent.putExtra(Common.KEY_STEP, 1);
                localBroadcastManager.sendBroadcast(intent);



            }
        });
    }

    @Override
    public int getItemCount() {
        return saloonList.size();
    }

    // Find the nane and addr field in layout_saloon
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_saloon_name, txt_saloon_address;
        CardView card_saloon;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);


            txt_saloon_name = (TextView)itemView.findViewById(R.id.txt_saloon_name);
            txt_saloon_address = (TextView) itemView.findViewById(R.id.txt_saloon_address);

            card_saloon = (CardView)itemView.findViewById(R.id.card_saloon);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }
}

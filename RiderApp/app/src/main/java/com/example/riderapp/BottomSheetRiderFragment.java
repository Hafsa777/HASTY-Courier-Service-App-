package com.example.riderapp;

import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.riderapp.Common.Common;
import com.example.riderapp.RetrofitController.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {

    String mLocation,mDestination;

    IGoogleAPI mService;
    TextView Calculation;

public static BottomSheetRiderFragment newInstance(String location,String destination) {


    BottomSheetRiderFragment b = new BottomSheetRiderFragment();
    Bundle args = new Bundle();
    args.putString("location",location );
    args.putString("destination",destination );
    b.setArguments(args);
    return b;
}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       mLocation = getArguments().getString("location");
       mDestination = getArguments().getString("destination");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout,container,false);
        TextView location = (TextView)view.findViewById(R.id.txtLocation);
        TextView destination = (TextView)view.findViewById(R.id.txtDestination);
        Calculation = (TextView)view.findViewById(R.id.txtcCalc);

        mService = Common.getGoogleService();

        getPrice(mLocation,mDestination);

        //set data

        location.setText(mLocation);
        destination.setText(mDestination);

        return view;
    }

    private void getPrice(String mLocation, String mDestination) {
    String requestUrl=null;
    try
    {
        requestUrl="https://maps.googleapis.com/maps/api/directions/json?"+
                "mode=driving&"
                +"transit_routing_preference=less_driving&"
                +"origin="+mLocation+"&"
                +"destination="+mDestination+"&"
                +"key="+getResources().getString(R.string.google_maps_api);

        Log.e("LINK",requestUrl);
        mService.getPath(requestUrl).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject=new JSONObject(response.body().toString());
                    JSONArray routes =jsonObject.getJSONArray("routes");

                    JSONObject object =routes.getJSONObject(0);
                    JSONArray legs=object.getJSONArray("legs");

                    JSONObject legsObject =legs.getJSONObject(0);

                    //get DIstance

                    JSONObject distance= legsObject.getJSONObject("distance");
                    String distance_text=distance.getString("text");

                    //to double from string

                    Double distance_value=Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));

                    //get time

                    JSONObject time= legsObject.getJSONObject("duration");
                    String time_text=time.getString("text");

                    Integer time_value=Integer.parseInt(time_text.replaceAll("\\D+",""));

                    String final_calculate=String.format("%s+%s=RS%.2f",distance_text,time_text,Common.getPrice(distance_value,time_value));

                    Calculation.setText(final_calculate);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("ERROR",t.getMessage());
            }
        });
    }
    catch (Exception ex)
    {
        ex.printStackTrace();
    }
    }
}

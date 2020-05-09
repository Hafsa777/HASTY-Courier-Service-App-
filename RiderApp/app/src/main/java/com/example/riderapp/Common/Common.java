package com.example.riderapp.Common;

import com.example.riderapp.RetrofitController.GoogleMapAPI;
import com.example.riderapp.RetrofitController.IFCMClient;
import com.example.riderapp.RetrofitController.IFCMService;
import com.example.riderapp.RetrofitController.IGoogleAPI;

public class Common {
    public static final String driver_table_info = "DriverInformation";
    public static final String rider_table_info = "RiderInformation";
    public static final String diver_location_table = "DriverLocation";
    public static final String pickup_request_table = "PickupRequestRider";
    public static final String driver_vehicles_table = "VehicleInformation";
    public static final String token_table = "Tokeninfo";

    public static final String fcmURl= "https://fcm.googleapis.com/";
    public static final String googleAPIUrl= "https://maps.googleapis.com";

    private static double base_fare= 20.00;
    private static double time_rate=1.50;
    private static double distance_rate=30.00;

    public static double getPrice(double km,int min){
        return (base_fare+(time_rate*min)+(distance_rate*km));
    }

    public static IFCMService getfcmService(){

        return IFCMClient.getClient(fcmURl).create(IFCMService.class);
    }

    public static IGoogleAPI getGoogleService(){

        return GoogleMapAPI.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }

}

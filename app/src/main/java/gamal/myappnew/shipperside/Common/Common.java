package gamal.myappnew.shipperside.Common;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import gamal.myappnew.shipperside.Moduel.Request;
import gamal.myappnew.shipperside.Moduel.Shipper;
import gamal.myappnew.shipperside.Moduel.ShippingInformation;

public class Common {
    public static final String SHIPPERS_TABLE="Shippers";
    public static final String ORDERNEEDSHIP_TABLE="OrderNeedShip";
    public static final String ShipperInfo_TABLE="ShippingOrders";
    public static  Location CURRENTLOCATION ;

    public static  Request CURRENTREQUEST;

    public static  Shipper CURRENTSHIPER;
    public static String CurrentKey;

    public static String ConvertStatusts(String status) {
        if (status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On My Way";
        else if (status.equals("2"))
            return "Shipping";
        else
            return "Shipped";
    }
    public static String getDate(long time)
    {
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date=new StringBuilder(android.text.format.DateFormat.format("dd-MM-yyy HH:MM",
                calendar).toString());
        return date.toString();


    }

    public static void CreateShipperOrder(String key, String phone, Location mLastloaction) {
        ShippingInformation shippingInformation=new ShippingInformation();
        shippingInformation.setLat(mLastloaction.getLatitude());
        shippingInformation.setLng(mLastloaction.getLongitude());
        shippingInformation.setOrderId(key);
        shippingInformation.setShipperPhone(phone);
        shippingInformation.setName(Common.CURRENTSHIPER.getName());
        FirebaseDatabase.
                getInstance().getReference(Common.ShipperInfo_TABLE)
                .child(key)
                .setValue(shippingInformation)
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ERRROR",e.getMessage());
            }
        });
    }

    public static void Updateshippinginfo(String currentKey, Location shipper) {
        Map<String,Object> updatelocation=new HashMap<>();
        updatelocation.put("lat",shipper.getLatitude());
        updatelocation.put("lng",shipper.getLongitude());
        FirebaseDatabase.
                getInstance().getReference(Common.ShipperInfo_TABLE)
                .child(currentKey)
                .updateChildren(updatelocation)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ERRROR",e.getMessage());
                    }
                });

    }
}

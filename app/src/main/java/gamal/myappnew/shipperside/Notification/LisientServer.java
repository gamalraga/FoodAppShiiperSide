package gamal.myappnew.shipperside.Notification;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import gamal.myappnew.shipperside.Common.Common;
import gamal.myappnew.shipperside.HomeActivity;
import gamal.myappnew.shipperside.Moduel.Request;

public class LisientServer extends Service implements ChildEventListener {
    DatabaseReference reference;
    public LisientServer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
         reference= FirebaseDatabase.getInstance().getReference(Common.ORDERNEEDSHIP_TABLE)
         .child(Common.CURRENTSHIPER.getPhone());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        reference.addChildEventListener(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Request request=snapshot.getValue(Request.class);
        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());
        notificationHelper.sendhightproirityNotification("new order ","to "+request.getAdress(), HomeActivity.class);

    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }


    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

    }


    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }


    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}

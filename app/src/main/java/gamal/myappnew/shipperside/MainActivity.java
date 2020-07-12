package gamal.myappnew.shipperside;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import gamal.myappnew.shipperside.Common.Common;
import gamal.myappnew.shipperside.Moduel.Shipper;
import info.hoang8f.widget.FButton;

public class MainActivity extends AppCompatActivity {
FButton singin;
EditText  edpassword,edphone;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        singin=findViewById(R.id.btnsingin);
        edphone=findViewById(R.id.ed_phone);
        edpassword=findViewById(R.id.ed_password);
         progressDialog=new ProgressDialog(MainActivity.this);
         progressDialog.setTitle("Shipper");
         progressDialog.setMessage("Waiting....");
         progressDialog.setCancelable(false);
        //Shippers
        singin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (edpassword.getText().toString().isEmpty()||edphone.getText().toString().isEmpty())
              {
                  Toast.makeText(MainActivity.this, "All Fileds is requried", Toast.LENGTH_SHORT).show();
              }else {
                  login(edphone.getText().toString(),edpassword.getText().toString());
              }
            }
        });
    }

    private void login(String phone, final String password) {
        progressDialog.show();
        FirebaseDatabase.getInstance().getReference(Common.SHIPPERS_TABLE)
                .child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    Shipper shipper=snapshot.getValue(Shipper.class);
                    if (shipper.getPassword().equals(password))
                    {
                         Common.CURRENTSHIPER=shipper;
                         startActivity(new Intent(MainActivity.this,HomeActivity.class));
                         finish();
                         progressDialog.dismiss();

                    }else {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Password incorrect!!", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "your shipper's not exixts!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

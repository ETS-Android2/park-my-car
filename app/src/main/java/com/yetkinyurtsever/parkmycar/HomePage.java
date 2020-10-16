package com.yetkinyurtsever.parkmycar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class HomePage extends AppCompatActivity {

    ImageView iv;
    Button b1;
    EditText e1;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.5F);
    final String android_id = Settings.Secure.getString(HomePage.this.getContentResolver(), Settings.Secure.ANDROID_ID);
    private final int REQUEST_LOCATION_PERMISSION = 1;
    private boolean isLocated = false;
    private FusedLocationProviderClient fusedLocationClient;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        iv = (ImageView) findViewById(R.id.imageView);
        b1 = (Button) findViewById(R.id.button2);
        e1 = (EditText) findViewById(R.id.editText2);

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.popup, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        DrawerLayout.LayoutParams.WRAP_CONTENT,
                        DrawerLayout.LayoutParams.WRAP_CONTENT);

                popupWindow.setFocusable(true);
                popupWindow.showAtLocation(popupView,Gravity.CENTER, 0, 0);
                popupWindow.setOutsideTouchable(false);
                dimBehind(popupWindow);

                Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                Button btnAccept = (Button) popupView.findViewById(R.id.accept);

                btnAccept.setOnClickListener(new Button.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        // TODO Auto-generated method stub

                        iv.setImageResource(R.drawable.car_saved);

                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(HomePage.this);

                        requestLocationPermission();
                        fusedLocationClient.getLastLocation().addOnSuccessListener(HomePage.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef1 = database.getReference("users/" + android_id + "/latitude");
                                    DatabaseReference myRef2 = database.getReference("users/" + android_id + "/longitude");

                                    myRef1.setValue(location.getLatitude());
                                    myRef2.setValue(location.getLongitude());
                                    isLocated = true;
                                }
                            }
                        });

                        popupWindow.dismiss();
                    }
                });

                btnDismiss.setOnClickListener(new Button.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        // TODO Auto-generated method stub
                        popupWindow.dismiss();
                    }
                });

                popupWindow.showAsDropDown(iv, 50, 50);
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isLocated) {
                    String s = "" + e1.getText().toString();

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users/" + android_id + "/note");

                    myRef.setValue(s);

                    v.startAnimation(buttonClick);
                    startActivity(new Intent(HomePage.this, MapsActivity.class));
                    overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom);
                } else{
                    Toast.makeText(getApplicationContext(),"Please select location !",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static void dimBehind(PopupWindow popupWindow) {
        View container = popupWindow.getContentView().getRootView();
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.8f;
        wm.updateViewLayout(container, p);
    }

    /******************     PERMISSION FUNCTIONS    *************************/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(!EasyPermissions.hasPermissions(this, perms))
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
    }
}

package com.yetkinyurtsever.parkmycar;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    TextView textView;
    Button b;
    double latitude = 0.0f, longitude = 0.0f;
    final String android_id = Settings.Secure.getString(MapsActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        textView = (TextView) findViewById(R.id.textView);
        b = (Button) findViewById(R.id.button2);


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.popup2, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        DrawerLayout.LayoutParams.WRAP_CONTENT,
                        DrawerLayout.LayoutParams.WRAP_CONTENT);

                popupWindow.setFocusable(true);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
                popupWindow.setOutsideTouchable(false);
                dimBehind(popupWindow);

                Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss1);
                Button btnAccept = (Button) popupView.findViewById(R.id.accept1);

                btnAccept.setOnClickListener(new Button.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        // TODO Auto-generated method stub

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("users/" + android_id);

                        myRef.removeValue();

                        startActivity(new Intent(MapsActivity.this, MainActivity.class));

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

                popupWindow.showAsDropDown(b, 50, 50);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Disables back button.
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/" + android_id);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                latitude = dataSnapshot.child("latitude").getValue(Double.class);
                longitude = dataSnapshot.child("longitude").getValue(Double.class);

                if(dataSnapshot.child("note").exists())
                    textView.setText(dataSnapshot.child("note").getValue(String.class));


                try {
                    LatLng car = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions()
                            .position(car)
                            .title("Marker on your Car!"));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(car,15));
                } catch (Exception ex){
                    Toast.makeText(getApplicationContext(),"Error while retrieving data !",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        myRef.addListenerForSingleValueEvent(valueEventListener);
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
}

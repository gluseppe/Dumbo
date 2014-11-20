package com.elephant.proga.dumbo;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private TextView mGeneralInfo;
    private LinearLayout mLabel;

    //i need to retain them in order to place a view to an specific position
    //it will be the position of the selected aircraft (more or less)
    private FrameLayout mFrameLayout;
    private FrameLayout.LayoutParams mLayoutParams;

    private LinearLayout mLabelLayout;
    private boolean labelAdded = false;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
        setUpComponents();

        mFrameLayout = (FrameLayout) findViewById(R.id.MainFrameLayout);
        mLabel = null;


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setOnCameraChangeListener(this);
            mMap.setOnMarkerClickListener(this);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(10, 10))
                .title("Hello motherfucker"));

    }

    private void setUpComponents() {

        mGeneralInfo = (TextView) findViewById(R.id.GeneralInformationText);
        mGeneralInfo.setText("Hello MotherFucker");

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Projection proj = mMap.getProjection();
        LatLng markerPos = marker.getPosition();
        Point screenPoint = proj.toScreenLocation(markerPos);
        int x = screenPoint.x;
        int y = screenPoint.y;
        mGeneralInfo.setText(String.format("x:%d - y:%d", x,y));





        if (mLabel == null) {
            LayoutInflater inflater = getLayoutInflater();
            mLabel = (LinearLayout) inflater.inflate(R.layout.label_layout, null);

            //int width = aircraftID.getWidth();
            //int height = aircraftID.getHeight();
            mLayoutParams = new FrameLayout.LayoutParams(50,50);
            //mLayoutParams.leftMargin = x;
            //mLayoutParams.topMargin = y;
            mFrameLayout.addView(mLabel,mLayoutParams);
        }



        mLabel.setTranslationX(x);
        mLabel.setTranslationY(y);
        TextView aircraftID = (TextView) mLabel.findViewById(R.id.AircraftID);
        aircraftID.setText(String.format("x:%d - y:%d", x,y));







        //we only have one marker for the moment
        //i return true to avoid map centering automatically on the marker
        return true;
    }
}

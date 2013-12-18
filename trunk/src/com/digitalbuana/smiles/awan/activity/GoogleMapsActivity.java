package com.digitalbuana.smiles.awan.activity;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.digitalbuana.smiles.R;
import com.digitalbuana.smiles.activity.ChatViewActivity;
import com.digitalbuana.smiles.awan.helper.MapItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GoogleMapsActivity extends MapActivity implements OnClickListener{
    public static final String TAG = "GoogleMapsActivity";
    private MapView mapView;
    private LocationManager locationManager;
    Geocoder geocoder;
    Location location;
    LocationListener locationListener;
    CountDownTimer locationtimer;
    MapController mapController;
    MapOverlay mapOverlay = new MapOverlay();
    Button cancelButton, sendButton;
    double lat, lng;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.map_dialog);        
        cancelButton = (Button)findViewById(R.id.cancelMapButton);
        sendButton = (Button)findViewById(R.id.sendMapButton);                
        sendButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);        
        initComponents();
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        mapController.setZoom(15);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager == null) {
            Toast.makeText(GoogleMapsActivity.this,
                    "Location Manager Not Available", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null)
            location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            GeoPoint point = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
            mapController.animateTo(point, new Message());
            mapOverlay.setPointToDraw(point);
            List<Overlay> listOfOverlays = mapView.getOverlays();
            Drawable drawable = this.getResources().getDrawable(R.drawable.img_add_item);            
            MapItemizedOverlay itemizedoverlay = new MapItemizedOverlay(drawable);            
            OverlayItem overlayitem = new OverlayItem(point, "Your Location", "Area:"+lat+", "+lng);
            itemizedoverlay.addOverlay(overlayitem);
            listOfOverlays.add(itemizedoverlay);
        }
        locationListener = new LocationListener() {
            @Override
            public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            }

            @Override
            public void onProviderEnabled(String arg0) {
            }

            @Override
            public void onProviderDisabled(String arg0) {
            }

            @Override
            public void onLocationChanged(Location l) {
                location = l;
                locationManager.removeUpdates(this);
                if (l.getLatitude() == 0 || l.getLongitude() == 0) {
                } else {
                    lat = l.getLatitude();
                    lng = l.getLongitude();
                }
            }
        };
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000, 10f, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000, 10f, locationListener);
        locationtimer = new CountDownTimer(9000, 3000) {
            @SuppressLint("NewApi")
			@Override
            public void onTick(long millisUntilFinished) {
                if (location != null)
                    locationtimer.cancel();
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            }

            @Override
            public void onFinish() {
                if (location == null) {
                }
            }
        };
        locationtimer.start();
    }

    public MapView getMapView() {
        return this.mapView;
    }

    private void initComponents() {
        mapView = (MapView) findViewById(R.id.mapview);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    class MapOverlay extends Overlay {
        private GeoPoint pointToDraw;

        public void setPointToDraw(GeoPoint point) {
            pointToDraw = point;
        }

        public GeoPoint getPointToDraw() {
            return pointToDraw;
        }

        @Override
        public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
                long when) {
            super.draw(canvas, mapView, shadow);

            Point screenPts = new Point();
            mapView.getProjection().toPixels(pointToDraw, screenPts);

            Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                    R.drawable.img_add_item);
            canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 24, null);
            return true;
        }
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==cancelButton){
			finish();
		}else if(v == sendButton){
			
			String tmpLng = String.valueOf(lng);
			tmpLng = tmpLng.substring(0, 4);
			
			String tmpLat = String.valueOf(lat);
			tmpLat = tmpLat.substring(0, 4);
			
			Intent i = new Intent();
			i.putExtra("long", ""+lng);
			i.putExtra("lati", ""+lat);
			setResult(ChatViewActivity.GET_MAP_CODE, i);
			finish();
			
		}
	}

}

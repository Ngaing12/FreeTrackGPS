package com.example.freetrackgps;

import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.location.*;
import android.widget.TextView;

import java.util.List;

import android.os.Bundle;


public class GPSListner implements LocationListener  {
	private TextView gpsPosition, gpsStatus, workoutDistance;
	private RouteManager localRoute;
    private boolean gpsCurrentStatus;
	public GPSListner(List<TextView> E, RouteManager Rt, boolean gpsCurrentStatus){
		this.gpsPosition = E.get(0);
		this.gpsStatus = E.get(1);
		this.workoutDistance = E.get(2);
		localRoute = Rt;
        this.gpsCurrentStatus  = gpsCurrentStatus;
	}
    public void onLocationChanged(Location location) {
        String message = String.format( " %1$s %2$s %3$s",String.format( "%.2f", location.getLongitude()), String.format( "%.2f",location.getLatitude()), String.format( "%.2f",location.getAltitude()));
        if (localRoute != null && location != null && location.hasAltitude() == true ){
        	localRoute.addPoint(location);
        	this.workoutDistance.setText(String.format("%.2f", localRoute.getDistance()) + " m");
        }
        this.gpsPosition.setText(message);
    }
    public void onStatusChanged(String s, int i, Bundle b) {
    	switch(i){
    	case LocationProvider.AVAILABLE:
    		this.gpsStatus.setText("On");
            gpsCurrentStatus = true;
    		if (localRoute.getStatus() == 1)
    			localRoute.unpause();
    		break;
    	case LocationProvider.OUT_OF_SERVICE:
    		this.gpsStatus.setText("Off");
            gpsCurrentStatus = false;
    		if (localRoute.getStatus() == 2)
    			localRoute.pause();
    		break;
    	case LocationProvider.TEMPORARILY_UNAVAILABLE:
    		this.gpsStatus.setText("Off");
            gpsCurrentStatus = false;
    		if (localRoute.getStatus() == 2)
    			localRoute.pause();
    		break;
    	}
    }
    public void onProviderDisabled(String s) {
    	this.gpsStatus.setText("Off");
        gpsCurrentStatus = false;
    	if (localRoute.getStatus() == 2)
    		localRoute.pause();
    }
    public void onProviderEnabled(String s) {
    	this.gpsStatus.setText("On");
        gpsCurrentStatus = true;
    	if (localRoute.getStatus() == 1)
    		localRoute.unpause();
    }		
}
    
    
    


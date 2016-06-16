package com.example.br3athe_in.easyTrip;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;

public class CityPicker extends FragmentActivity implements OnMapReadyCallback {

	private GoogleMap mMap;
	private Location myLocation;
	private int mc = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city_picker);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
						.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		// TODO: Consider calling
		//    ActivityCompat#requestPermissions
		// here to request the missing permissions, and then overriding
		//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
		//                                          int[] grantResults)
		// to handle the case where the user grants the permission. See the documentation
		// for ActivityCompat#requestPermissions for more details.
		// SecurityException is very possible to occur
		if (ActivityCompat.checkSelfPermission(
						this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {

			return;
		}
		mMap.setMyLocationEnabled(true);

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(50.44847278765969, 30.52297968417406),
						17)); // dafuq, Google
		// mMap.
		mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng) {
				// TODO: 30.05.2016 extract confirmation to this listener via context menu
				++mc;
				mMap.addMarker(new MarkerOptions()
								.position(latLng)
								.title("Marker " + mc)
								.snippet("no snippet lel"));
				Log.d("Custom", String.format("Marker %d - coordinates: %s, zoom: %f", mc, latLng.toString(), mMap.getCameraPosition().zoom));
			}
		});
	}

	public void commit(View view) {
		Intent answer = getIntent();
		answer.putExtra("chosen", mMap.getCameraPosition().target);
		setResult(RESULT_OK, answer);
		finish();
	}
}

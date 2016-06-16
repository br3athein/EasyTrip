package com.example.br3athe_in.easyTrip;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Locale;

public class CityPicker extends FragmentActivity implements OnMapReadyCallback, IntentionExtraKeys {

	private static final String LOG_TAG = "Custom";
	private static final double INITIAL_LATITUDE = 50.44847278765969;
	private static final double INITIAL_LONGITUDE = 30.52297968417406;
	private static final LatLng INITIAL_POS = new LatLng(INITIAL_LATITUDE, INITIAL_LONGITUDE);
	private GoogleMap mMap;
	private Marker markedLocation;
	private Address lastResolvedAddress = new Address(Locale.getDefault());

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
		// Log.d(LOG_TAG, "Map loaded correctly with key " + getString(R.string.google_maps_key));
		// "D/Custom: Map loaded correctly with key YOUR_KEY_HERE". wtf?
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

		lastResolvedAddress.setLocality("Киев");
		lastResolvedAddress.setCountryName("Украина");
		lastResolvedAddress.setLatitude(INITIAL_LATITUDE);
		lastResolvedAddress.setLongitude(INITIAL_LONGITUDE);

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(INITIAL_POS, 13)); // dafuq, Google
		markedLocation = mMap.addMarker(new MarkerOptions()
						.position(INITIAL_POS)
						.title("Киев")
						.snippet("Украина")
						.draggable(true));
		markedLocation.showInfoWindow();

		mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				// TODO: 30.05.2016 extract confirmation to this listener via context menu

				float[] bearing = new float[] {0};
				Location.distanceBetween(
								markedLocation.getPosition().latitude, markedLocation.getPosition().longitude,
								latLng.latitude, latLng.longitude, bearing
				);

				if (bearing[0] >= 3000) {
					resolveAddress(latLng);
				}
				markedLocation.remove();
				markedLocation = mMap.addMarker(new MarkerOptions()
								.position(latLng)
								.title(lastResolvedAddress.getLocality())
								.snippet(lastResolvedAddress.getCountryName())
								.draggable(true)
				);
				markedLocation.showInfoWindow();
			}
		});
	}

	public void commit(View view) {
		Intent answer = getIntent();
		setResult(RESULT_OK, answer);

		resolveAddress(markedLocation.getPosition());

		answer.putExtra(EXTRA_COORDINATES, markedLocation.getPosition());
		answer.putExtra(
						EXTRA_VERBOSE_LOCATION,
						lastResolvedAddress.getLocality() + ", " + lastResolvedAddress.getCountryName());

		finish();
	}

	/** This transforms a place's coordinates into verbal address
	 * and then sets it to <code>lastResolvedAddress</code>.
	 * Uses <code>Geocoder</code>, so consider minimizing calls count.
	 */
	private void resolveAddress(LatLng cPos) {
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		Address selected = new Address(Locale.getDefault());
		selected.setCountryName(getString(R.string.picker_unresolved_country));
		selected.setLocality(getString(R.string.picker_unresolved_city));
		try {
			selected = geocoder.getFromLocation(cPos.latitude, cPos.longitude, 1).get(0);
			Log.d(LOG_TAG,
							"Reverse geocoding executed, found " + selected.getLocality()
											+ ", " + selected.getCountryName()
											+ " at " + selected.getLatitude() + ", " + selected.getLongitude());
		} catch (IOException e) {
			Log.e(LOG_TAG, "An I/O exception occured", e);
			Toast.makeText(this, "Geocoding error. " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		lastResolvedAddress = selected;
	}
}

package com.example.br3athe_in.easyTrip;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.br3athe_in.easyTrip.Util.City;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class WorldMap extends FragmentActivity implements OnMapReadyCallback, IntentionExtraKeys {

	private static final String LOG_TAG = "Custom";
	private static final double INITIAL_LATITUDE = 50.44847278765969;
	private static final double INITIAL_LONGITUDE = 30.52297968417406;
	private static final LatLng INITIAL_POS = new LatLng(INITIAL_LATITUDE, INITIAL_LONGITUDE);
	private GoogleMap mMap;
	private Address lastResolvedAddress = new Address(Locale.getDefault());

	private ArrayList<Marker> markedLocations = new ArrayList<>();

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
		ArrayList<City> savedState = (ArrayList<City>) getIntent().getExtras().get(EXTRA_CITIES_TO_VISIT);
		if (savedState.size() == 0) {
			initOnBlankIntent();
		} else {
			initAgain(savedState);
		}

		mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				// TODO: 30.05.2016 extract confirmation to this listener via context menu
				// region Call economy attempts.
					// Slip between an old position and an updated one
					//	ArrayList<Float> distances = new ArrayList<>();
					//	for(Marker m : markedLocations) {
					//		float[] slip = new float[1];
					//
					//		Location.distanceBetween(
					//				m.getPosition().latitude, m.getPosition().longitude,
					//				latLng.latitude, latLng.longitude, slip
					//		);
					//		distances.add(slip[0]);
					//	}
					//	// Should be minimal distance from current latLng to markedLocations' content
					//
					//	if (Collections.min(distances) <= 10000) { // the slip is lesser than 10 km
					//		return;
					//	}
				// endregion
				if (resolveAddress(latLng)) {
					addSignedMarkerHere(latLng);
				}
			}
		});
	}

	private void initOnBlankIntent() {
		lastResolvedAddress.setLocality("Киев");
		lastResolvedAddress.setCountryName("Украина");
		lastResolvedAddress.setLatitude(INITIAL_LATITUDE);
		lastResolvedAddress.setLongitude(INITIAL_LONGITUDE);

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(INITIAL_POS, 8)); // dafuq, Google
		Marker initialMarker = mMap.addMarker(
				new MarkerOptions()
						.position(INITIAL_POS)
						.title("Киев")
						.snippet("Украина")
		);
		initialMarker.showInfoWindow();
		markedLocations.add(initialMarker);
	}

	private void initAgain(ArrayList<City> savedState) {
		for(City city : savedState) {
			Marker marker = mMap.addMarker(
					new MarkerOptions()
					.position(new LatLng(city.getPosition().latitude, city.getPosition().longitude))
					.title(city.getCityName())
					.snippet(city.getCountryName())
			);
			markedLocations.add(marker);
		}

		mMap.moveCamera(
				CameraUpdateFactory.newLatLngZoom(
						markedLocations.get(markedLocations.size() - 1).getPosition(), 8
				)
		);
	}

	private void addSignedMarkerHere(LatLng latLng) {
		String nullableCityName = lastResolvedAddress.getLocality();
		if (nullableCityName == null) {
			nullableCityName = getString(R.string.placeholder_city_unknown);
		}
		String nullableCountryName = lastResolvedAddress.getCountryName();
		if (nullableCountryName == null) {
			nullableCityName = getString(R.string.placeholder_country_unknown);
		}

		Marker marker = mMap.addMarker(new MarkerOptions()
				.position(latLng)
				.title(nullableCityName)
				.snippet(nullableCountryName)
		);
		marker.showInfoWindow();
		markedLocations.add(marker);
	}

	public void commit(View view) {
		// TODO: 17.06.2016
		Intent answer = getIntent();
		setResult(RESULT_OK, answer);
		// Crashes on attempt to marshal Marker value.
		ArrayList<City> response = new ArrayList<>();

		for (Marker m : markedLocations) {
			response.add(new City(
					m.getTitle(),
					m.getSnippet(),
					m.getPosition().latitude,
					m.getPosition().longitude
			));
		}

		answer.putExtra(EXTRA_CITIES_TO_VISIT, response);

		finish();
	}

	/** This transforms a place's coordinates into verbal address
	 * and then sets it to <code>lastResolvedAddress</code>.
	 * Uses <code>Geocoder</code>, so consider minimizing calls count.
	 */
	private boolean resolveAddress(LatLng cPos) {
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
			lastResolvedAddress = selected;
			return true;
		} catch (IndexOutOfBoundsException e) {
			Toast.makeText(this, "No cities here lol", Toast.LENGTH_SHORT).show();
			return false;
		}
		catch (IOException e) {
			Log.e(LOG_TAG, "An I/O exception occured", e);
			Toast.makeText(this, "Geocoding error. " + e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
	}
}

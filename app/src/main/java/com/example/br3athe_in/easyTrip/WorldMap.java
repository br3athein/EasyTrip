package com.example.br3athe_in.easyTrip;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.br3athe_in.easyTrip.Util.City;
import com.example.br3athe_in.easyTrip.Util.IntentionExtraKeys;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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
		setContentView(R.layout.activity_world_map);
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
		// region Final init, nothing to look for.
		mMap = googleMap;

		// notTODO: Consider calling
		//    ActivityCompat#requestPermissions
		// here to request the missing permissions, and then overriding
		//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
		//                                          int[] grantResults)
		// to handle the case where the user grants the permission. See the documentation
		// for ActivityCompat#requestPermissions for more details.
		// SecurityException is very possible to occur
		//
		// ...
		// but not on my watch, pal.
		if (ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			return;
		}
		// Relax, we're just fooling around with a debug version on our own device here. :3

		mMap.setMyLocationEnabled(true);
		// endregion

		int scenario = getIntent().getIntExtra(EXTRA_SCENARIO, 0);
		switch (scenario) {
			case SCENARIO_PICK_CITIES:
				scenario_pickCities();
				break;
			case SCENARIO_DRAW_ROUTE:
				scenario_drawRoute();
				break;
			default:
				Log.d(LOG_TAG, "WorldMap.onMapReady: something is wrong with scenario keys.");
		}
	}

	private void scenario_pickCities() {
		Toast.makeText(this, getString(R.string.wmap_hints), Toast.LENGTH_LONG).show();
		reloadMapFromSelector();
		describeMarkerSetBehavior();
		describeMarkerRemovalBehavior();
	}

	private void reloadMapFromSelector() {
		ArrayList<City> savedState =
				(ArrayList<City>) getIntent().getExtras().get(EXTRA_CITIES_TO_VISIT);
		int focusToId = getIntent().getExtras().getInt(EXTRA_CITY_TO_FOCUS);
		if (savedState.size() == 0) {
			initOnBlankIntent();
		} else {
			initAgain(savedState, focusToId);
		}
	}

	private void describeMarkerSetBehavior() {
		mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				// Slip between an old position and an updated one
				float minDist = Float.MAX_VALUE;

				for(Marker m : markedLocations) {
					float[] slip = new float[1];

					Location.distanceBetween(
							m.getPosition().latitude, m.getPosition().longitude,
							latLng.latitude, latLng.longitude, slip
					);
					minDist = Math.min(minDist, slip[0]);
				}
				// Minimal distance from current latLng to anu of markedLocations' content
				if (minDist <= 15000) { // the slip is lesser than 15 km
					return;
				}
				if (resolveAddress(latLng)) {
					addSignedMarkerHere(latLng);
				}
			}
		});
	}

	private void describeMarkerRemovalBehavior() {
		mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
			@Override
			public void onInfoWindowLongClick(Marker marker) {
				showRemovalPrompt(marker);
			}

			private void showRemovalPrompt(final Marker marker) {
				new AlertDialog.Builder(WorldMap.this)
						.setTitle(
								!marker.getTitle().equals(getString(R.string.placeholder_city_unknown))?
										String.format(
												getString(R.string.wmap_remove_known_prompt_title), marker.getTitle()
										)
										:
										getString(R.string.wmap_remove_unknown_prompt_title)
						)
						.setPositiveButton(
								getString(R.string.prompt_positive),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										markedLocations.remove(marker);
										marker.remove();
									}
								}
						)
						.setNegativeButton(
								getString(R.string.prompt_negative),
								null
						)
						.show();
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
		markedLocations.add(initialMarker);
		initialMarker.showInfoWindow();
	}

	private void initAgain(ArrayList<City> savedState, int focusToId) {
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
						markedLocations.get(focusToId).getPosition(), 8
				)
		);
		markedLocations.get(focusToId).showInfoWindow();
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
		int scenario = getIntent().getIntExtra(EXTRA_SCENARIO, 0);

		switch (scenario) {
			case SCENARIO_PICK_CITIES:
				Intent answer = getIntent();
				setResult(RESULT_OK, answer);

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
				break;
			case SCENARIO_DRAW_ROUTE:
				finish();
				break;
			default:
				Log.d(LOG_TAG, "WorldMap.commit: something is wrong with scenario keys.");
		}
	}

	/** This transforms a place's coordinates into verbal address
	 * and then sets it to <code>lastResolvedAddress</code>.
	 * Uses <code>Geocoder</code>, so consider minimizing calls count.
	 */
	private boolean resolveAddress(LatLng cPos) {
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		Address selected = new Address(Locale.getDefault());
		selected.setCountryName(getString(R.string.wmap_unresolved_country));
		selected.setLocality(getString(R.string.wmap_unresolved_city));

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

	private void scenario_drawRoute() {
		initAgain((ArrayList<City>) getIntent().getExtras().get(EXTRA_CITIES_TO_VISIT), 0);
		PolylineOptions po = (PolylineOptions) getIntent().getExtras().get(EXTRA_DECODED_POLYLINE);
		mMap.addPolyline(po);
	}
}

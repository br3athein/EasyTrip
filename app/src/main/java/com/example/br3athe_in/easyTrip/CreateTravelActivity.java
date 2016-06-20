package com.example.br3athe_in.easyTrip;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.br3athe_in.easyTrip.Util.City;
import com.example.br3athe_in.easyTrip.Util.IntentionExtraKeys;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class CreateTravelActivity extends AppCompatActivity implements IntentionExtraKeys {
	private static final String LOG_TAG = "Custom";
	private static final String LIST_ITEM_TITLE = "TITLE";
	private static final String LIST_ITEM_DETAILS = "COORDINATES";

	private ArrayList<HashMap<String, String>> lvFillContent = new ArrayList<>();

	private ListView lv;
	private ArrayList<City> cities;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_create_travel);
		getIntent().getExtras().get(EXTRA_CITIES_TO_VISIT);
		cities = (ArrayList<City>) getIntent().getExtras().get(EXTRA_CITIES_TO_VISIT);

		loadViews();
	}

	private void loadViews() {
		lv = (ListView) findViewById(R.id.lvCitiesSelected);
		lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
		lvFillContent.clear();

		lvFillContent.clear();

		for(City c : cities) {
			HashMap<String, String> incomingCity = new HashMap<>();
			String nullableCityName = c.getCityName();
			if (nullableCityName == null) {
				nullableCityName = getString(R.string.placeholder_city_unknown);
			}
			String nullableCountryName = c.getCountryName();
			if (nullableCountryName == null) {
				nullableCityName = getString(R.string.placeholder_country_unknown);
			}

			incomingCity.put(LIST_ITEM_TITLE, nullableCityName + ", " + nullableCountryName);
			incomingCity.put(LIST_ITEM_DETAILS,
					String.format(Locale.US, getString(R.string.slctr_cntxt_coords_template),
							c.getPosition().latitude, c.getPosition().longitude));
			lvFillContent.add(incomingCity);
		}

		lv.setAdapter(
				new SimpleAdapter(this, lvFillContent,
						android.R.layout.simple_list_item_2,
						new String[]{LIST_ITEM_TITLE, LIST_ITEM_DETAILS},
						new int[]{android.R.id.text1, android.R.id.text2}
				)
		);
	}

	public void optimize(View view) {
		new GetDirections().execute();
	}

	//@Override
	//public void onDirectionsGotten() {
	//	Intent intent = new Intent(CreateTravelActivity.this, WorldMap.class);
	//	intent.putExtra(
	//			EXTRA_DECODED_POLYLINE,
	//			asd.getOverviewPolyline()
	//	);
	//	intent.putExtra(EXTRA_CITIES_TO_VISIT, cities);
	//	intent.putExtra(EXTRA_SCENARIO, SCENARIO_DRAW_ROUTE);
	//
	//	startActivity(intent);
	//}

	private class GetDirections extends AsyncTask<Void, Void, String> {
		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;
		String jsonResult = "";

		class ResponseValues {
			String encodedOverviewPolyline;
			int totalRouteLength;
		}

		ResponseValues responseValues = new ResponseValues();

		@Override
		protected String doInBackground(Void... params) {
			// TODO: extract query building
			try {
				String query = buildRoutingQuery(cities);
				Log.d(LOG_TAG, "Query built, got " + query);

				URL url = new URL(query);

				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				InputStream inputStream = urlConnection.getInputStream();
				StringBuilder responseBuilder = new StringBuilder();

				reader = new BufferedReader(new InputStreamReader(inputStream));

				String line;
				while ((line = reader.readLine()) != null) {
					responseBuilder.append(line);
				}

				jsonResult = responseBuilder.toString();

			} catch (Exception e) {
				Log.d(LOG_TAG,
						"CreateTravelActivity.GetDirections.doInBackground: Connection failed somehow.", e);
			}
			return jsonResult;
		}

		@Override
		protected void onPostExecute(String jsonString) {
			super.onPostExecute(jsonString);

			JSONObject dataJsonObject;

			try {
				dataJsonObject = new JSONObject(jsonString);

				initResponse(dataJsonObject);

				Intent intent = new Intent(CreateTravelActivity.this, WorldMap.class);
				intent.putExtra(
						EXTRA_DECODED_POLYLINE,
						decodePoly(responseValues.encodedOverviewPolyline)
				);
				intent.putExtra(EXTRA_CITIES_TO_VISIT, cities);
				intent.putExtra(EXTRA_SCENARIO, SCENARIO_DRAW_ROUTE);

				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@NonNull
		private String buildRoutingQuery(ArrayList<City> cities) {
			City homeCity = cities.get(0);

			StringBuilder queryBilder =
					new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
			queryBilder.append("origin=").append(homeCity.getPosition().toString());
			queryBilder.append("&destination=").append(homeCity.getPosition().toString());
			queryBilder.append("&waypoints=optimize:true");
			for (City c : cities.subList(1, cities.size())) {
				queryBilder.append("|").append(c.getPosition().toString());
			}
			queryBilder.append("&language=ru&unit=metric&api_key=") // goddamn ampersands
					.append(getString(R.string.google_maps_key));
			return queryBilder.toString();
		}

		private void initResponse(JSONObject dataJsonObject) throws JSONException {
			responseValues.encodedOverviewPolyline =
					dataJsonObject.getJSONArray("routes").getJSONObject(0)
							.getJSONObject("overview_polyline").getString("points");

			JSONArray legs =
					dataJsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");

			int length = 0;
			for(int i = 0; i < legs.length(); i++) {
				length += legs.getJSONObject(i).getJSONObject("distance").getInt("value");
			}

			responseValues.totalRouteLength = length;
		}

		private PolylineOptions decodePoly(String encoded) {
			ArrayList<LatLng> poly = new ArrayList<>();
			int index = 0, len = encoded.length();
			int lat = 0, lng = 0;

			while (index < len) {
				int b, shift = 0, result = 0;
				do {
					b = encoded.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lat += dlat;

				shift = 0;
				result = 0;
				do {
					b = encoded.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lng += dlng;

				LatLng p = new LatLng((((double) lat / 1E5)),(((double) lng / 1E5)));
				poly.add(p);
			}

			PolylineOptions polylineOptions = new PolylineOptions().addAll(poly);
			polylineOptions.color(Color.CYAN);
			return polylineOptions;
		}
	}

	public void commit(View view) {
		// stub
	}
}

package com.example.br3athe_in.easyTrip;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.br3athe_in.easyTrip.Util.City;
import com.example.br3athe_in.easyTrip.Util.DBAssistant;
import com.example.br3athe_in.easyTrip.Util.IntentionExtraKeys;
import com.example.br3athe_in.easyTrip.Util.Travel;

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
	private ArrayList<City> citiesOptimal;

	private ArrayList<String> currentEncodedRouteOverView = new ArrayList<>();
	private ArrayList<String> currentEncodedDetailedPolylines = new ArrayList<>();

	private int totalLength;
	private int unoptimizedLength = 0;

	private boolean optimized = false;

	@Override
	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_create_travel);
		cities = (ArrayList<City>) getIntent().getExtras().get(EXTRA_CITIES_TO_VISIT);
		enumerate(cities);

		new GetDirectionsInitial().execute();

		loadViews();
	}

	private void loadViews() {
		lv = (ListView) findViewById(R.id.lvCitiesSelected);
		assert lv != null;
		lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
		lvFillContent.clear();

		lvFillContent.clear();

		for (City c : cities) {
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
					String.format(Locale.US, getString(R.string.cslctr_cntxt_coords_template),
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

	private void enumerate(ArrayList<City> cities) {
		int i = 0;
		for (City c : cities) {
			c.setCityName(++i + " - " + c.getCityName());
		}
	}

	private ArrayList<City> denumerate(ArrayList<City> cities) {
		for (City c : cities) {
			c.denumerate();
		}
		return cities;
	}

	public void optimize(View view) {
		if (!optimized) {
			new GetDirectionsOptimal().execute();
		} else {
			Toast.makeText(this, "Маршрут уже оптимизирован!", Toast.LENGTH_SHORT).show();
		}
	}

	public void drawCurrentRoute(View view) {
		Log.d(LOG_TAG, "Intents to show the route on map");
		Intent intent = new Intent(CreateTravelActivity.this, WorldMap.class);
		if (optimized) {
			intent.putExtra(EXTRA_CITIES_TO_VISIT, citiesOptimal);
			intent.putExtra(EXTRA_ENCODED_POLYLINE, currentEncodedDetailedPolylines);
			intent.putExtra(EXTRA_SCENARIO, SCENARIO_DRAW_OPTIMAL_ROUTE);
			Toast.makeText(this, R.string.crttrvl_opt_route_drawn, Toast.LENGTH_SHORT).show();
		} else {
			intent.putExtra(EXTRA_CITIES_TO_VISIT, cities);
			intent.putExtra(EXTRA_ENCODED_POLYLINE, currentEncodedRouteOverView);
			intent.putExtra(EXTRA_SCENARIO, SCENARIO_DRAW_PLAIN_ROUTE);
			Toast.makeText(this, R.string.crttrvl_unopt_route_drawn, Toast.LENGTH_SHORT).show();
		}

		startActivity(intent);
	}

	public void commit(View view) {
		if (optimized) {
			final EditText travelName = new EditText(this);
			travelName.setPadding(50, 50, 50, 50);
			travelName.setHint(R.string.crttrvl_name_your_travel_prompt);

			new AlertDialog.Builder(this)
					.setView(travelName)
					.setPositiveButton(
							getString(R.string.crttrvl_save_travel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									denumerate(citiesOptimal);
									Travel travel = new Travel(
											travelName.getText().toString(),
											citiesOptimal,
											totalLength,
											currentEncodedDetailedPolylines
									);
									DBAssistant dbAssistant = new DBAssistant(CreateTravelActivity.this);
									Log.d(LOG_TAG, "Writing travel to DB...");
									dbAssistant.writeTravel(travel);
									Log.d(LOG_TAG, "Writing done.");
									setResult(RESULT_OK);
									finish();
								}
							}
					)
					.setNegativeButton(
							R.string.crttrvl_prompt_negative,
							null
					)
					.show();
		} else {
			Toast.makeText(this, "Optimize it first!", Toast.LENGTH_SHORT).show();
		}
	}

	private abstract class GetDirectionsAbstract extends AsyncTask<Void, Void, String> {
		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;
		String jsonResult = "";

		class ResponseValues {
			int totalRouteLength;
			private ArrayList<City> citiesOptimal = new ArrayList<>();
			String encodedOverviewPolyline;
			ArrayList<String> encodedDetailedPolylineLegs = new ArrayList<>();
		}

		ResponseValues responseValues = new ResponseValues();

		@Override
		protected abstract String doInBackground(Void... params);

		@Override
		protected void onPostExecute(String jsonString) {
			handleResponse(jsonString);
			contextActions();
		}

		protected abstract void contextActions();

		String getDirectionsResponse(boolean optimize) {
			try {
				String query = buildRoutingQuery(cities, optimize);
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
						"CreateTravelActivity.GetDirectionsInitial.doInBackground: Connection failed somehow.", e);
			}
			return jsonResult;
		}

		protected void handleResponse(String response) {
			try {
				parseResponse(new JSONObject(response));
			} catch (Exception e) {
				Log.d(LOG_TAG,
						"CreateTravelActivity.GetDirectionsInitial: Troubles in onPostExecute", e
				);
			}
		}

		private void parseResponse(JSONObject dataJsonObject) throws JSONException {
			Log.d(LOG_TAG, "parseResponse called");

			JSONArray legs =
					dataJsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");

			JSONArray waypointOrder =
					dataJsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order");

			int length = 0;

			for (int i = 0; i < legs.length(); i++) {
				JSONObject cLeg = legs.getJSONObject(i);
				length += cLeg.getJSONObject("distance").getInt("value");

				JSONArray cSteps = cLeg.getJSONArray("steps");
				for (int j = 0; j < cSteps.length(); j++) {
					responseValues.encodedDetailedPolylineLegs.add(
							cSteps.getJSONObject(j).getJSONObject("polyline").getString("points")
					);
				}
			}
			responseValues.totalRouteLength = length;
			responseValues.citiesOptimal.clear();
			responseValues.citiesOptimal.add(cities.get(0));
			//responseValues.un

			for (int i = 0; i < waypointOrder.length(); i++) {
				responseValues.citiesOptimal.add(new City(cities.get(waypointOrder.getInt(i) + 1)));
			}
		}

		@NonNull
		private String buildRoutingQuery(ArrayList<City> cities, boolean optimize) {
			City homeCity = cities.get(0);

			StringBuilder queryBuilder =
					new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
			queryBuilder.append("origin=").append(homeCity.getPosition().toString())
					.append("&destination=").append(homeCity.getPosition().toString())
					.append("&waypoints=optimize:").append(optimize);
			for (City c : cities.subList(1, cities.size())) {
				queryBuilder.append("|").append(c.getPosition().toString());
			}
			queryBuilder.append("&language=ru&unit=metric&api_key=")
					.append(getString(R.string.google_maps_key));
			return queryBuilder.toString();
		}

		void reloadContent(ArrayList<City> newContent) {
			lvFillContent.clear();

			for(City m : newContent) {
				HashMap<String, String> incomingCity = new HashMap<>();
				String nullableCityName = m.getCityName();
				if (nullableCityName == null) {
					nullableCityName = getString(R.string.placeholder_city_unknown);
				}
				String nullableCountryName = m.getCountryName();
				if (nullableCountryName == null) {
					nullableCityName = getString(R.string.placeholder_country_unknown);
				}

				incomingCity.put(LIST_ITEM_TITLE, nullableCityName + ", " + nullableCountryName);
				incomingCity.put(LIST_ITEM_DETAILS,
						String.format(Locale.US, getString(R.string.cslctr_cntxt_coords_template),
								m.getPosition().latitude, m.getPosition().longitude));
				lvFillContent.add(incomingCity);
			}
			reloadAdapter(lvFillContent);
		}

		private void reloadAdapter(ArrayList<HashMap<String, String>> to) {
			SimpleAdapter newAdapter = new SimpleAdapter(CreateTravelActivity.this, to,
					android.R.layout.simple_list_item_2,
					new String[] {LIST_ITEM_TITLE, LIST_ITEM_DETAILS},
					new int[] {android.R.id.text1, android.R.id.text2}
			);
			lv.setAdapter(newAdapter);
		}
	}

	private class GetDirectionsInitial extends GetDirectionsAbstract {
		@Override
		protected String doInBackground(Void... params) {
			return getDirectionsResponse(false);
		}

		@Override
		protected void handleResponse(String response) {
			super.handleResponse(response);
			unoptimizedLength = responseValues.totalRouteLength;
			currentEncodedRouteOverView.add(responseValues.encodedOverviewPolyline);
		}

		@Override
		protected void contextActions() {
			TextView tv = (TextView) findViewById(R.id.tvTravelStatus);
			assert tv != null;
			tv.setText(
					String.format(
							getString(R.string.crttrvl_length_unoptimized),
							((float) unoptimizedLength)/1000
					)
			);
		}
	}

	private class GetDirectionsOptimal extends GetDirectionsAbstract {
		@Override
		protected String doInBackground(Void... params) {
			return getDirectionsResponse(true);
		}

		@Override
		protected void handleResponse(String response) {
			super.handleResponse(response);

			citiesOptimal = responseValues.citiesOptimal;
			currentEncodedDetailedPolylines.addAll(responseValues.encodedDetailedPolylineLegs);
			totalLength = responseValues.totalRouteLength;
		}

		@Override
		protected void contextActions() {
			enumerate(denumerate(citiesOptimal));

			TextView tv = (TextView) findViewById(R.id.tvTravelStatus);
			assert tv != null;
			tv.setText(
					String.format(
							getString(R.string.crttrvl_length_optimized),
							(float) totalLength / 1000,
							(unoptimizedLength - totalLength) / 1000
					)
			);
			Toast.makeText(CreateTravelActivity.this,
					String.format(
							getString(R.string.crttrvl_length_optimized),
							(float) totalLength / 1000,
							(unoptimizedLength - totalLength) / 1000
					),
					Toast.LENGTH_SHORT).show();

			optimized = true;
			Log.d(LOG_TAG, "Considering optimized, about to reload content");
			reloadContent(citiesOptimal);
		}
	}
}

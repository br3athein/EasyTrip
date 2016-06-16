package com.example.br3athe_in.easyTrip;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CitySelector extends AppCompatActivity {

	public static final String LOG_TAG = "Custom";
	public static final int ASK_FOR_CITY = 1337;
	private static final String FIELD_1 = "latitude";
	private static final String FIELD_2 = "longitude";
	private static final int ACTION_SHOW_MAP = 1;
	private static final int ACTION_REQUEST_INFO = 2;
	private static final int ACTION_EDIT_NOTES = 3;
	private static final int ACTION_REMOVE_CITY = 4;
	private static final int PLACE_PICKER_RQ = 42;
	private HashMap<String, String> incomingCity;

	//private final HashMap<String, String> addCity = new HashMap<>().put(FIELD_1, "f1");

	//PlacePicker pp = new PlacePicker();
	//int addCityFingerprint

	private int addedNow = 0;
	// TODO: 29.05.2016 refactor, ffs
	ListView lv;
	Button btn;
	private ArrayList<HashMap<String, String>> citiesSelected = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city_selector);
		loadViews();
		lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
		//?lv.setClickable(true);

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
				if(pos == lv.getCount() - 1) {
					askForAddingCity();
				}
				// TODO: 29.05.2016 change to context menu, then implement getInfo(city)
				getInfo(v);
			}
		});

		// debug
		//ArrayAdapter<CharSequence> adapterDebug = ArrayAdapter.createFromResource(
		//				this, R.array.cs_initial1, android.R.layout.simple_list_item_2);
		//citiesSelected = new ArrayList<>();
		// end debug

		// release
		incomingCity = new HashMap<>();
		incomingCity.put(FIELD_1, getResources().getString(R.string.cs_last_list_item_1));
		incomingCity.put(FIELD_2, getResources().getString(R.string.cs_last_list_item_2));

		incomingCity.hashCode();

		citiesSelected.add(incomingCity);
		// end release

		// debug
		// String[] k1 = getResources().getStringArray(R.array.cs_initial1);
		// String[] k2 = getResources().getStringArray(R.array.cs_initial2);
		// for(int i = 0; i < k1.length; i++) {
		// 	incomingCity = new HashMap<>();
		// 	incomingCity.put(FIELD_1, k1[i]);
		// 	incomingCity.put(FIELD_2, k2[i]);
		// 	citiesSelected.add(incomingCity);
		// }
		//// release code clone below
		// incomingCity = new HashMap<>();
		// incomingCity.put(FIELD_1, getResources().getString(R.string.cs_last_list_item_1));
		// incomingCity.put(FIELD_2, getResources().getString(R.string.cs_last_list_item_2));
		// citiesSelected.add(incomingCity);
		// end debug

		SimpleAdapter adapter = new SimpleAdapter(this, citiesSelected,
						android.R.layout.simple_list_item_2,
						new String[] {FIELD_1, FIELD_2},
						new int[] {android.R.id.text1, android.R.id.text2}
		);

		lv.setAdapter(adapter);

		// TODO: extract&expand for the sake of a further usage. Now it works only in onCreate.
		// or shouldn't I?..
		registerForContextMenu(lv);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// ListView locLv = (ListView) v;
		//lv.getAdapter().getItem(lv.getAdapter().getCount()).equals()
		if (true) {
			super.onCreateContextMenu(menu, v, menuInfo);
			// TODO! Change to extracted city's name.
			menu.setHeaderTitle("Мадрид");

			menu.add(Menu.NONE, ACTION_SHOW_MAP, 1, R.string.slctr_cntxt_show_map);
			menu.add(Menu.NONE, ACTION_REQUEST_INFO, 2, R.string.slctr_cntxt_request_info);
			menu.add(Menu.NONE, ACTION_EDIT_NOTES, 3, R.string.slctr_cntxt_edit_notes);
			menu.add(Menu.NONE, ACTION_REMOVE_CITY, 4, R.string.slctr_cntxt_remove_city);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO: put some actions, bruh!
		AdapterView.AdapterContextMenuInfo menuInfo =
						(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
			case ACTION_SHOW_MAP:
				break;
			case ACTION_REQUEST_INFO:
				break;
			case ACTION_EDIT_NOTES:
				break;
			case ACTION_REMOVE_CITY:
				citiesSelected.remove(menuInfo.position);
				reloadAdapter(citiesSelected);
				break;
		}
		return super.onContextItemSelected(item);
	}

	private void loadViews() {
		btn = (Button) findViewById(R.id.bCommit);
		lv = (ListView) findViewById(R.id.lwCitiesSelected);
	}

	private void askForAddingCity() {
		Intent intent = new Intent(this, CityPicker.class);
		startActivityForResult(intent, ASK_FOR_CITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ASK_FOR_CITY:
				if(resultCode == RESULT_CANCELED) { return; }
				LatLng latLng = (LatLng) data.getExtras().get("chosen");
				incomingCity = new HashMap<>();
				// TODO: put some city name maybe? May require Places though.
				incomingCity.put(FIELD_1, "Placeholder " + ++addedNow);
				// TODO: redundant?
				incomingCity.put(FIELD_2,
								String.format(Locale.US, "at %3.3f, %3.3f", latLng.latitude, latLng.longitude));
				citiesSelected.add(citiesSelected.size() - 1, incomingCity);
				reloadAdapter(citiesSelected);
				Log.d("Custom", "City added at " + latLng.toString());

				Geocoder geocoder = new Geocoder(this, Locale.getDefault());
				try {
					List<Address> la = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 10);
					//la.get(0).get
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(this, "Ivan! " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
				break;
			case PLACE_PICKER_RQ:
				if (resultCode == RESULT_OK) {
					final Place place = PlacePicker.getPlace(this, data);

					Log.d(LOG_TAG, place.getAddress().toString() + place.getRating());
				}
				break;
		}
	}

	/** Sets <code>lv's</code> content to current <code>citiesSelected's</code>.
	 * @param to Specifies the incoming content to set to. Just in case.
	 *           May be useful after ABC optimizing.
	 */
	private void reloadAdapter(ArrayList<HashMap<String, String>> to) {
		SimpleAdapter newAdapter = new SimpleAdapter(this, to,
						android.R.layout.simple_list_item_2,
						new String[] {FIELD_1, FIELD_2},
						new int[] {android.R.id.text1, android.R.id.text2}
		);
		newAdapter.getItem(newAdapter.getCount() - 1);
		lv.setAdapter(newAdapter);
	}

	public void getCont(View view) {
		try {
			startActivityForResult(new PlacePicker.IntentBuilder().build(this), PLACE_PICKER_RQ);
		} catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
			e.printStackTrace();
			Toast.makeText(this, "Igor! " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}

		Toast.makeText(
						this, "This should optimize your input. But it's not time yet!", Toast.LENGTH_LONG)
						.show();
	}

	public void getInfo(View view) {
	}
}

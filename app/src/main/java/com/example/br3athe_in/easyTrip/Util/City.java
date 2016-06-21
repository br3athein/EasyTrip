package com.example.br3athe_in.easyTrip.Util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.br3athe_in.easyTrip.R;

import java.io.Serializable;

// TODO: should it extend HashMap <String, String>? Would be great to use it as a ListView element.
// Consider googling this shit up.
public final class City implements Serializable {
	public static class Position implements Serializable {
		public double latitude; public double longitude;
		Position(double x, double y) {this.latitude = x; this.longitude = y;}

		@Override
		public String toString() {
			return latitude + "," + longitude;
		}
	}

	private Position position;
	public Position getPosition() {
		return position;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public void denumerate() {
		Pattern p = Pattern.compile("^\\d+ - (.*)");
		Matcher m = p.matcher(cityName);
		if(m.matches()) {
			cityName = m.group(1);
		}
	}

	private String cityName;
	public String getCityName() {
		return cityName;
	}

	private String countryName;
	public String getCountryName() {
		return countryName;
	}

	private String notes;

	public void showNotes(final Context context) {
		final TextView tvContentBox = new TextView(context);
		tvContentBox.setText(notes);
		tvContentBox.setHint(R.string.city_notes_hint);
		tvContentBox.setPadding(50, 50, 50, 50);

		new AlertDialog.Builder(context)
				.setView(tvContentBox)
				.setNegativeButton(
						R.string.city_close_notes_dialog,
						null
				)
				.setPositiveButton(
						R.string.city_edit_notes_prompt,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								editNotes(context);
							}
						}
				)
				.show();
	}

	private void editNotes(final Context context) {
		final EditText etContentEditor = new EditText(context);
		etContentEditor.setHint(R.string.city_notes_hint);
		etContentEditor.setText(notes);
		etContentEditor.setPadding(50, 50, 50, 50);

		new AlertDialog.Builder(context)
				.setView(etContentEditor)
				.setNegativeButton(
						R.string.city_edit_notes_decline,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (!notes.equals(etContentEditor.getText().toString())) {
									new AlertDialog.Builder(context)
											.setTitle(R.string.city_unsaved_alert)
											.setPositiveButton(
													R.string.prompt_positive,
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int which) {
															showNotes(context);
														}
													}
											)
											.setNegativeButton(
													R.string.prompt_negative,
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int which) {
															editNotes(context);
														}
													}
											)
									.show();
								} else {
									showNotes(context);
								}
							}
						}
				)
				.setPositiveButton(
						R.string.city_edit_notes_commit,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								notes = etContentEditor.getText().toString();
								showNotes(context);
							}
						}
				)
				.show();
	}

	public City(String cityName, String countryName, double latitude, double longitude) {
		this.cityName = cityName;
		this.countryName = countryName;
		this.position = new Position(latitude, longitude);
		this.notes = "";
	}

	public City(City other) {
		this.cityName = other.getCityName();
		this.countryName = other.getCountryName();
		this.position = other.getPosition();
		this.notes = other.notes;
	}
}

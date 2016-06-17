package com.example.br3athe_in.easyTrip.Util;

import java.io.Serializable;

// TODO: should it extend HashMap <String, String>? Would be great to use it as a ListView element.
// Consider googling this shit up.
public class City implements Serializable {
	public static class Position implements Serializable {
		public double latitude; public double longitude;
		Position(double x, double y) {this.latitude = x; this.longitude = y;}
	}

	private Position position;
	public Position getPosition() {
		return position;
	}

	private String cityName;
	public String getCityName() {
		return cityName;
	}

	private String countryName;
	public String getCountryName() {
		return countryName;
	}

	public City(String cityName, String countryName, double latitude, double longitude) {
		this.cityName = cityName;
		this.countryName = countryName;
		this.position = new Position(latitude, longitude);
	}
}

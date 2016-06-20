package com.example.br3athe_in.easyTrip.Util;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.Language;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

// TODO: 18.06.2016 should I really explain this one?
public class Travel {
	ArrayList<City> citiesToVisit;

	Travel(ArrayList<City> toFill) {
		citiesToVisit.addAll(toFill);
	}

	public void optimize(String gMapsKey) {
	}
}

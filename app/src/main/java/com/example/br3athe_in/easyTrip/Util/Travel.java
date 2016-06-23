package com.example.br3athe_in.easyTrip.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Travel implements Serializable {
	private int lengthMeters;
	public ArrayList<City> citiesToVisit;
	public String title;
	public ArrayList<String> detailedRouteLegs;

	float getLengthKms() {
		return ((float) lengthMeters) / 1000;
	}

	public Travel(String travelTitle, ArrayList<City> toFill,
								int travelLength, ArrayList<String> encodedRoute) {
		citiesToVisit = new ArrayList<>();
		citiesToVisit.addAll(toFill);
		title = travelTitle;
		lengthMeters = travelLength;
		detailedRouteLegs = new ArrayList<>();
		detailedRouteLegs.addAll(encodedRoute);
	}

	static Travel deserialize(byte[] serializedTravel) {
		ByteArrayInputStream bis = new ByteArrayInputStream(serializedTravel);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bis.close();
			} catch (IOException ignored) {
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ignored) {
			}
		}
		try {
			assert in != null;
			return (Travel) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	byte[] serialize() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(this);
		} catch (IOException ignored) {
			// ignore
		} finally {
			try {
				if(out != null) {
					out.close();
				}
			} catch (IOException ignored) {
				// gtfo, bloody IOException!
			}
			return bos.toByteArray();
		}
	}

	public void optimize(String gMapsKey) {
	}
}

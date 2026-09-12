package com.samadhan.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GeoUtils {

	private static final Logger logger = LoggerFactory.getLogger(GeoUtils.class);

	private static final double EARTH_RADIUS_KM = 6371.0;

	private GeoUtils() {
	}

	public static Double parseCoord(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			logger.warn("Invalid coordinate value: {}", value);
			return null;
		}
	}

	public static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return EARTH_RADIUS_KM * c;
	}

	// Nearest distance from a point to any vertex of a decoded route polyline. Google's Routes
	// API returns points spaced closely enough along real roads that vertex-to-point distance is
	// a fine approximation of true point-to-route distance for a multi-km matching buffer.
	public static double minDistanceToPolylineKm(double lat, double lng, List<double[]> polylinePoints) {
		double min = Double.MAX_VALUE;
		for (double[] p : polylinePoints) {
			double d = haversineKm(lat, lng, p[0], p[1]);
			if (d < min) {
				min = d;
			}
		}
		return min;
	}

	// Standard Google encoded polyline algorithm (used by both the Routes API and the JS Maps SDK).
	public static List<double[]> decodePolyline(String encoded) {
		List<double[]> points = new ArrayList<>();
		if (encoded == null || encoded.isEmpty()) {
			return points;
		}

		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
			lng += dlng;

			points.add(new double[] { lat / 1e5, lng / 1e5 });
		}
		return points;
	}
}

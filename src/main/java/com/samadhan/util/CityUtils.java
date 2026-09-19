package com.samadhan.util;

import java.util.ArrayList;
import java.util.List;

public final class CityUtils {

	private CityUtils() {
	}

	// Heuristic, not a real geocoder: Google-formatted addresses for VendorAvailability's
	// fromLocation/toLocation consistently end "..., City, State, Country" (e.g. "Sector 62,
	// Noida, Uttar Pradesh, India"), so the city is the third segment from the end once trailing
	// empty segments are dropped. This is NOT safe to reuse against TransferRequestDetails.source/
	// destination — those addresses have extra trailing metadata segments appended by a different
	// form (e.g. "..., India, tt, "), which would shift this offset and return garbage.
	// Falls back to the raw trimmed address when there aren't enough segments to apply the
	// heuristic, rather than guessing at a shorter offset.
	public static String extractCity(String address) {
		if (address == null || address.trim().isEmpty()) {
			return null;
		}

		List<String> segments = new ArrayList<>();
		for (String part : address.split(",")) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) {
				segments.add(trimmed);
			}
		}

		if (segments.size() >= 3) {
			return segments.get(segments.size() - 3);
		}

		return address.trim();
	}
}

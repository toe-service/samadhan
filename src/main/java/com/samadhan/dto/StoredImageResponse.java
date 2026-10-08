package com.samadhan.dto;

// Carries both the raw bytes and the content type back to the controller for
// TransferVendorController's /public-profile/{slug}/logo and /cover endpoints -- the stored
// object key alone doesn't tell the HTTP response what Content-Type to send, which the browser
// needs to actually render it as an <img>.
public class StoredImageResponse {

	private final byte[] data;
	private final String contentType;

	public StoredImageResponse(byte[] data, String contentType) {
		this.data = data;
		this.contentType = contentType;
	}

	public byte[] getData() {
		return data;
	}

	public String getContentType() {
		return contentType;
	}
}

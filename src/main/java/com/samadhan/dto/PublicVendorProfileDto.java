package com.samadhan.dto;

import java.util.List;

public class PublicVendorProfileDto {

	private String vendorName;
	private String vendorCity;
	private String vendorAddress;
	private String vendorContactNumber;
	private String vendorEmail;
	private Boolean isIndividual;
	private Boolean verified;
	private List<String> services;
	private Double avgRating;
	private Integer ratingCount;
	private String businessTagline;
	private String aboutText;
	private String logoUrl;
	private String coverImageUrl;
	private Integer yearsInBusiness;
	private List<VendorTestimonialDto> testimonials;
	private List<String> galleryImageUrls;

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVendorCity() {
		return vendorCity;
	}

	public void setVendorCity(String vendorCity) {
		this.vendorCity = vendorCity;
	}

	public String getVendorAddress() {
		return vendorAddress;
	}

	public void setVendorAddress(String vendorAddress) {
		this.vendorAddress = vendorAddress;
	}

	public String getVendorContactNumber() {
		return vendorContactNumber;
	}

	public void setVendorContactNumber(String vendorContactNumber) {
		this.vendorContactNumber = vendorContactNumber;
	}

	public String getVendorEmail() {
		return vendorEmail;
	}

	public void setVendorEmail(String vendorEmail) {
		this.vendorEmail = vendorEmail;
	}

	public Boolean getIsIndividual() {
		return isIndividual;
	}

	public void setIsIndividual(Boolean isIndividual) {
		this.isIndividual = isIndividual;
	}

	public Boolean getVerified() {
		return verified;
	}

	public void setVerified(Boolean verified) {
		this.verified = verified;
	}

	public List<String> getServices() {
		return services;
	}

	public void setServices(List<String> services) {
		this.services = services;
	}

	public Double getAvgRating() {
		return avgRating;
	}

	public void setAvgRating(Double avgRating) {
		this.avgRating = avgRating;
	}

	public Integer getRatingCount() {
		return ratingCount;
	}

	public void setRatingCount(Integer ratingCount) {
		this.ratingCount = ratingCount;
	}

	public String getBusinessTagline() {
		return businessTagline;
	}

	public void setBusinessTagline(String businessTagline) {
		this.businessTagline = businessTagline;
	}

	public String getAboutText() {
		return aboutText;
	}

	public void setAboutText(String aboutText) {
		this.aboutText = aboutText;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getCoverImageUrl() {
		return coverImageUrl;
	}

	public void setCoverImageUrl(String coverImageUrl) {
		this.coverImageUrl = coverImageUrl;
	}

	public Integer getYearsInBusiness() {
		return yearsInBusiness;
	}

	public void setYearsInBusiness(Integer yearsInBusiness) {
		this.yearsInBusiness = yearsInBusiness;
	}

	public List<VendorTestimonialDto> getTestimonials() {
		return testimonials;
	}

	public void setTestimonials(List<VendorTestimonialDto> testimonials) {
		this.testimonials = testimonials;
	}

	public List<String> getGalleryImageUrls() {
		return galleryImageUrls;
	}

	public void setGalleryImageUrls(List<String> galleryImageUrls) {
		this.galleryImageUrls = galleryImageUrls;
	}
}

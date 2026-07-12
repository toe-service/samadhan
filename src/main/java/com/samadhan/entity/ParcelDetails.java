package com.samadhan.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.samadhan.enums.BikeModelEnum;
import com.samadhan.enums.CarModelEnum;
import com.samadhan.enums.DimensionUnit;
import com.samadhan.enums.ParcelTypeEnum;

@Entity
@Table(name="parcel_details")
public class ParcelDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="parcel_type")
	private ParcelTypeEnum ParcelType;
	
	@Column(name="car_model")
	private CarModelEnum carModel;
	
	@Column(name="car_number")
	private String carNumber;
	
	@Column(name="bike_model")
	private BikeModelEnum bikeModel;
	
	@Column(name="bike_number")
	private String bikeNumber;
	
	@Column(name="parcel_weight")
	private double parcelWeight;
	
	@Column(name="package_description")
	private String packageDescription;
	
	@Column(name="length")
	private Double length;
	
	@Column(name="width")
	private Double width;
	
	@Column(name="height")
	private Double height;
	
	@Enumerated(EnumType.STRING)
	 @Column(name="dimension_unit")
	 private DimensionUnit dimensionUnit;

	public Double getLength() {
		return length;
	}

	public void setLength(Double length) {
		this.length = length;
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public DimensionUnit getDimensionUnit() {
		return dimensionUnit;
	}

	public void setDimensionUnit(DimensionUnit dimensionUnit) {
		this.dimensionUnit = dimensionUnit;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ParcelTypeEnum getParcelType() {
		return ParcelType;
	}

	public void setParcelType(ParcelTypeEnum parcelType) {
		ParcelType = parcelType;
	}

	public CarModelEnum getCarModel() {
		return carModel;
	}

	public void setCarModel(CarModelEnum carModel) {
		this.carModel = carModel;
	}

	public String getCarNumber() {
		return carNumber;
	}

	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}

	public BikeModelEnum getBikeModel() {
		return bikeModel;
	}

	public void setBikeModel(BikeModelEnum bikeModel) {
		this.bikeModel = bikeModel;
	}

	public String getBikeNumber() {
		return bikeNumber;
	}

	public void setBikeNumber(String bikeNumber) {
		this.bikeNumber = bikeNumber;
	}

	public double getParcelWeight() {
		return parcelWeight;
	}

	public void setParcelWeight(double parcelWeight) {
		this.parcelWeight = parcelWeight;
	}

	public String getPackageDescription() {
		return packageDescription;
	}

	public void setPackageDescription(String packageDescription) {
		this.packageDescription = packageDescription;
	}
	
	

}

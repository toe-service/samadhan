package com.samadhan.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

// Precomputed "this request matches this vendor's posted availability" rows, replacing the old
// on-demand VendorAvailabilityServiceImpl#computeMatches (which recomputed the full match set —
// against every pending/unassigned request system-wide — on every cache-cold read, the source of
// the 20-30s "Matching Your Posting" dashboard stall). Kept current incrementally instead:
// recomputed for one vendor when their postings change (postAvailability/updateAvailability/
// cancelAvailability), and recomputed for one request when it enters/leaves the pending/
// unassigned pool (see TransferRequestServiceImpl's hook points). Reads are then a single
// indexed SELECT instead of a live geo computation.
//
// vendorId/transferRequestId are plain columns, not @ManyToOne relations — this table is a
// lightweight precomputed index, not a rich domain entity, and reading match rows should never
// eagerly pull in a full TransferVendor/TransferRequestDetails graph just to paginate. The actual
// TransferRequestDetails objects are fetched separately, only for the small page of matches
// actually being returned to the frontend.
//
// requestCreatedDate/pickupDate/instantBooking are denormalized from TransferRequestDetails so
// the read path (sorting latest-first, optional pickupDate filter) never needs a join.
@Entity
@Table(name = "vendor_availability_match",
		uniqueConstraints = @UniqueConstraint(name = "uq_vendor_availability_match_vendor_request",
				columnNames = { "vendor_id", "transfer_request_id" }),
		indexes = {
				@Index(name = "idx_vam_vendor_created", columnList = "vendor_id, request_created_date"),
				@Index(name = "idx_vam_transfer_request", columnList = "transfer_request_id")
		})
public class VendorAvailabilityMatch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vendor_id", nullable = false)
	private Long vendorId;

	@Column(name = "transfer_request_id", nullable = false)
	private Long transferRequestId;

	@Column(name = "match_type", nullable = false)
	private String matchType;

	@Column(name = "match_distance_km", nullable = false)
	private double matchDistanceKm;

	@Column(name = "match_score_percent", nullable = false)
	private int matchScorePercent;

	// How closely sized the posting's declared vehicle is to what this request needs — null when
	// the posting has no specific vehicleType, the request isn't a whole-vehicle service, or
	// either side's vehicle size isn't known. Already folded into matchScorePercent's overall
	// average; kept here too so it can be shown on its own.
	@Column(name = "vehicle_match_percent")
	private Integer vehicleMatchPercent;

	@Column(name = "request_created_date")
	private LocalDateTime requestCreatedDate;

	@Column(name = "pickup_date")
	private LocalDate pickupDate;

	@Column(name = "instant_booking")
	private Boolean instantBooking;

	@Column(name = "computed_at", nullable = false)
	private LocalDateTime computedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVendorId() {
		return vendorId;
	}

	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
	}

	public Long getTransferRequestId() {
		return transferRequestId;
	}

	public void setTransferRequestId(Long transferRequestId) {
		this.transferRequestId = transferRequestId;
	}

	public String getMatchType() {
		return matchType;
	}

	public void setMatchType(String matchType) {
		this.matchType = matchType;
	}

	public double getMatchDistanceKm() {
		return matchDistanceKm;
	}

	public void setMatchDistanceKm(double matchDistanceKm) {
		this.matchDistanceKm = matchDistanceKm;
	}

	public int getMatchScorePercent() {
		return matchScorePercent;
	}

	public void setMatchScorePercent(int matchScorePercent) {
		this.matchScorePercent = matchScorePercent;
	}

	public Integer getVehicleMatchPercent() {
		return vehicleMatchPercent;
	}

	public void setVehicleMatchPercent(Integer vehicleMatchPercent) {
		this.vehicleMatchPercent = vehicleMatchPercent;
	}

	public LocalDateTime getRequestCreatedDate() {
		return requestCreatedDate;
	}

	public void setRequestCreatedDate(LocalDateTime requestCreatedDate) {
		this.requestCreatedDate = requestCreatedDate;
	}

	public LocalDate getPickupDate() {
		return pickupDate;
	}

	public void setPickupDate(LocalDate pickupDate) {
		this.pickupDate = pickupDate;
	}

	public Boolean getInstantBooking() {
		return instantBooking;
	}

	public void setInstantBooking(Boolean instantBooking) {
		this.instantBooking = instantBooking;
	}

	public LocalDateTime getComputedAt() {
		return computedAt;
	}

	public void setComputedAt(LocalDateTime computedAt) {
		this.computedAt = computedAt;
	}
}

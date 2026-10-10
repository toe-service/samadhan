package com.samadhan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// One vendor-entered customer quote shown on the public vendor page. Used both as the
// request shape (VendorProfileContentRequest#testimonials) and the public response shape
// (PublicVendorProfileDto#testimonials) -- it's the same data either way.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorTestimonialDto {
    private String quote;
    private String name;
}

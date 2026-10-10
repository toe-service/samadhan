package com.samadhan.request;

import java.util.List;

import com.samadhan.dto.VendorTestimonialDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorProfileContentRequest {
    public String businessTagline;
    public String aboutText;
    public Integer yearsInBusiness;
    public List<VendorTestimonialDto> testimonials;
    public List<String> galleryImageUrls;
}

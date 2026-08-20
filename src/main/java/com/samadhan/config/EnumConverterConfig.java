package com.samadhan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.samadhan.enums.VehicleCategoryEnum;
import com.samadhan.enums.VendorPickupVehicleEnum;

/**
 * Spring's default String-to-enum conversion is Enum.valueOf, which only accepts the exact
 * constant name. The clients send display names too ("E Loader" rather than "E_LOADER"), so
 * those requests failed with a 500. These converters accept either form.
 */
@Configuration
public class EnumConverterConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, VendorPickupVehicleEnum.class,
                VendorPickupVehicleEnum::fromValue);
        registry.addConverter(String.class, VehicleCategoryEnum.class,
                VehicleCategoryEnum::fromValue);
    }
}

package com.samadhan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // prevent Jackson from serializing LocalDateTime as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // TransferRequestDetails' associations are now lazy (see entity + default_batch_fetch_size
        // in application.properties). FORCE_LAZY_LOADING makes Jackson initialize an uninitialized
        // proxy when it actually serializes that field instead of failing on the raw proxy object
        // — Hibernate's batch-fetch setting still applies to that initialization, so sibling rows'
        // same-type associations load via one batched IN(...) query, not one query per row.
        Hibernate5Module hibernate5Module = new Hibernate5Module();
        hibernate5Module.enable(Hibernate5Module.Feature.FORCE_LAZY_LOADING);
        mapper.registerModule(hibernate5Module);

        return mapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

package com.samadhan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // prevent Jackson from serializing LocalDateTime as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        // Plain `new RestTemplate()` has NO timeout by default (Java's HttpURLConnection defaults
        // to an infinite wait) — this is the RestTemplate RouteService uses for Google's Routes
        // API, called synchronously inside postAvailability/updateAvailability before the response
        // is returned. Without a bound here, a slow/hung Google API call made the whole
        // post/update-availability request hang indefinitely: the frontend/gateway would time out
        // and show a generic error, while the backend kept running and committed the save anyway
        // afterward — "it took forever, showed an error, but the edit actually went through."
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(10_000);
        return new RestTemplate(factory);
    }
}

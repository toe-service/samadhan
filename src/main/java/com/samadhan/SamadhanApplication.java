package com.samadhan;


import com.google.auth.oauth2.GoogleCredentials;
import java.util.TimeZone;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

@SpringBootApplication
@EnableCaching
public class SamadhanApplication {

//	@Bean
//	FirebaseMessaging firebaseMessaging() throws IOException {
//	    GoogleCredentials googleCredentials = GoogleCredentials.fromStream(new ClassPathResource("firebase.json").getInputStream());
//	    FirebaseOptions firebaseOptions = FirebaseOptions.builder().setCredentials(googleCredentials).build();
//	    FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, "my-app");
//	    return FirebaseMessaging.getInstance(app);
//	  }
	
	@Bean
	public FirebaseMessaging firebaseMessaging() throws IOException {

	    GoogleCredentials credentials =
	            GoogleCredentials.fromStream(
	                    new ClassPathResource("serviceAccountKey.json").getInputStream());

	    FirebaseOptions options = FirebaseOptions.builder()
	            .setCredentials(credentials)
	            .build();

	    FirebaseApp app;

//	    if (FirebaseApp.getApps().isEmpty()) {
//	        app = FirebaseApp.initializeApp(options, "my-app");
//	    } else {
//	        app = FirebaseApp.getApps().get(0);
//	    }
	    if (FirebaseApp.getApps().isEmpty()) {
	        app = FirebaseApp.initializeApp(options);
	    } else {
	        app = FirebaseApp.getInstance();
	    }

	    return FirebaseMessaging.getInstance(app);
	}

	public static void main(String[] args) throws IOException {
		 TimeZone.setDefault(
		            TimeZone.getTimeZone("Asia/Kolkata")
		        );
		
		SpringApplication.run(SamadhanApplication.class, args);
	}

}

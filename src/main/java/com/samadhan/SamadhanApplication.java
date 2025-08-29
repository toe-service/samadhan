package com.samadhan;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

@SpringBootApplication
public class SamadhanApplication {

	@Bean
	FirebaseMessaging firebaseMessaging() throws IOException {
	    GoogleCredentials googleCredentials = GoogleCredentials.fromStream(new ClassPathResource("firebase.json").getInputStream());
	    FirebaseOptions firebaseOptions = FirebaseOptions.builder().setCredentials(googleCredentials).build();
	    FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, "my-app");
	    return FirebaseMessaging.getInstance(app);
	  }

	public static void main(String[] args) throws IOException {
		SpringApplication.run(SamadhanApplication.class, args);
	}

}

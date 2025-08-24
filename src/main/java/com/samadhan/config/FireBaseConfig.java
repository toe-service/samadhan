//package com.samadhan.config;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import com.samadhan.SamadhanApplication;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.Objects;
//
//@Configuration
//public class FireBaseConfig {
//
//    @Value("${file.path.service.account.key}")
//    private String serviceAccountKeyPath;
//
//    private File getServiceAccountKeyFile() {
//        try {
//            ClassLoader classLoader = SamadhanApplication.class.getClassLoader();
//      return new File(serviceAccountKeyPath);
////      return new File(Objects.requireNonNull(classLoader.getResource("serviceAccountKey.json")).getFile());
//        } catch (Exception exp) {
//            return new File(serviceAccountKeyPath);
//        }
//    }
//
//    @Bean
//    public void loadFireBaseConfigs() throws IOException {
//       File file = getServiceAccountKeyFile();
//        System.out.println("file path is "+file.getAbsolutePath());
//        FileInputStream serviceAccount = new FileInputStream(file.getAbsolutePath());
//        FirebaseOptions options =  new FirebaseOptions.Builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .build();
//        FirebaseApp.initializeApp(options);
//    }
//}

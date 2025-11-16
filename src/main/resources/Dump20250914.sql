-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: samadhan
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `driver`
--

DROP TABLE IF EXISTS `driver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `driver` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `driver_active` bit(1) DEFAULT NULL,
  `driver_city` varchar(255) DEFAULT NULL,
  `driver_contact_number` varchar(255) DEFAULT NULL,
  `driver_email` varchar(255) DEFAULT NULL,
  `driver_latitude` varchar(255) DEFAULT NULL,
  `driver_longitude` varchar(255) DEFAULT NULL,
  `driver_name` varchar(255) DEFAULT NULL,
  `driver_token` varchar(255) DEFAULT NULL,
  `service_centre_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3mba66rxdqvttw5ksegk989sm` (`service_centre_id`),
  CONSTRAINT `FK3mba66rxdqvttw5ksegk989sm` FOREIGN KEY (`service_centre_id`) REFERENCES `service_centre` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `driver`
--

LOCK TABLES `driver` WRITE;
/*!40000 ALTER TABLE `driver` DISABLE KEYS */;
INSERT INTO `driver` VALUES (1,_binary '\0','Noida','788888888','abc@','28.5687','77.3827','Rahul','yujdvucjbdhdh',1),(2,_binary '','lucknow','63526762762','gdg@','26.8545','81.0013','Rohit','tygdghbuydhbd',1),(3,_binary '\0','Noida','587484848','dddds@','28.5753','77.3913','Amit','dhjdjdudiiedji',1);
/*!40000 ALTER TABLE `driver` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `location` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `area` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `latitude` varchar(255) DEFAULT NULL,
  `longitude` varchar(255) DEFAULT NULL,
  `shop_number` bigint DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `street_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
INSERT INTO `location` VALUES (1,'Noida','Noida','28.5682','77.3835',12,'UP','sec-76');
/*!40000 ALTER TABLE `location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `login`
--

DROP TABLE IF EXISTS `login`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `login` (
  `mobile` varchar(255) NOT NULL,
  `otp` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `login`
--

LOCK TABLES `login` WRITE;
/*!40000 ALTER TABLE `login` DISABLE KEYS */;
/*!40000 ALTER TABLE `login` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_date` datetime DEFAULT NULL,
  `payment_type` int DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `subscription_period` int DEFAULT NULL,
  `service_centre_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhcwpd0x8g4nbpive7a5ogpnp6` (`service_centre_id`),
  CONSTRAINT `FKhcwpd0x8g4nbpive7a5ogpnp6` FOREIGN KEY (`service_centre_id`) REFERENCES `service_centre` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rides`
--

DROP TABLE IF EXISTS `rides`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rides` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `driver_declination_reason` varchar(255) DEFAULT NULL,
  `driver_response` bit(1) DEFAULT NULL,
  `ride_id` varchar(255) NOT NULL,
  `ride_otp` int DEFAULT NULL,
  `ride_response_time` datetime DEFAULT NULL,
  `ride_status` bit(1) DEFAULT NULL,
  `ride_end_time` datetime DEFAULT NULL,
  `ride_start_time` datetime DEFAULT NULL,
  `driver_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `destination_latitude` varchar(255) DEFAULT NULL,
  `destination_longitude` varchar(255) DEFAULT NULL,
  `source_latitude` varchar(255) DEFAULT NULL,
  `source_longitude` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6tfxkpbgs1a25q51cy3iqwthu` (`ride_id`),
  KEY `FKinsp86xr2klco1n0e7lxtcpxt` (`driver_id`),
  KEY `FKbwiadceuacjmfcgfet18lmh53` (`user_id`),
  CONSTRAINT `FKbwiadceuacjmfcgfet18lmh53` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKinsp86xr2klco1n0e7lxtcpxt` FOREIGN KEY (`driver_id`) REFERENCES `driver` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rides`
--

LOCK TABLES `rides` WRITE;
/*!40000 ALTER TABLE `rides` DISABLE KEYS */;
INSERT INTO `rides` VALUES (1,'NA',_binary '','VI25083002060UKBPEZR',4563,'2025-08-30 11:04:25',_binary '',NULL,NULL,1,1,'22.11','73.21','28.77','77.34'),(3,'NA',_binary '','VI25083002060UKBPEZT',4563,'2025-08-30 11:24:05',_binary '',NULL,NULL,2,1,'22.11','73.21','28.77','77.34');
/*!40000 ALTER TABLE `rides` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_centre`
--

DROP TABLE IF EXISTS `service_centre`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_centre` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `contact_number` bigint DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `service_centre_name` varchar(255) DEFAULT NULL,
  `service_type` int DEFAULT NULL,
  `location_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp205fp40h2428wjbv3hc847gj` (`location_id`),
  CONSTRAINT `FKp205fp40h2428wjbv3hc847gj` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_centre`
--

LOCK TABLES `service_centre` WRITE;
/*!40000 ALTER TABLE `service_centre` DISABLE KEYS */;
INSERT INTO `service_centre` VALUES (1,_binary '',900000000,NULL,'abc tow service',1,1);
/*!40000 ALTER TABLE `service_centre` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_contact_number` bigint DEFAULT NULL,
  `user_email` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,7233925077,'shivankhal786@gmail.com','shivank'),(2,7007959733,'vishal@gmail.com','vishal');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_login_data`
--

DROP TABLE IF EXISTS `user_login_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_login_data` (
  `mobile_number` bigint NOT NULL,
  `otp` int DEFAULT NULL,
  PRIMARY KEY (`mobile_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_login_data`
--

LOCK TABLES `user_login_data` WRITE;
/*!40000 ALTER TABLE `user_login_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_login_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicle_transfer`
--

DROP TABLE IF EXISTS `vehicle_transfer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_transfer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_model` varchar(255) DEFAULT NULL,
  `vehicle_type` varchar(255) DEFAULT NULL,
  `destination_latitude` varchar(255) DEFAULT NULL,
  `destination_longitude` varchar(255) DEFAULT NULL,
  `source_latitude` varchar(255) DEFAULT NULL,
  `source_longitude` varchar(255) DEFAULT NULL,
  `transfer_date` datetime DEFAULT NULL,
  `transfer_otp` int DEFAULT NULL,
  `transfer_status` bit(1) DEFAULT NULL,
  `service_centre_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjmpry1rf10ufqrt9varebcvqa` (`service_centre_id`),
  CONSTRAINT `FKjmpry1rf10ufqrt9varebcvqa` FOREIGN KEY (`service_centre_id`) REFERENCES `service_centre` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicle_transfer`
--

LOCK TABLES `vehicle_transfer` WRITE;
/*!40000 ALTER TABLE `vehicle_transfer` DISABLE KEYS */;
/*!40000 ALTER TABLE `vehicle_transfer` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-14 16:09:28

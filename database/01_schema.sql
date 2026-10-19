
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coach` (
  `coach_id` bigint NOT NULL AUTO_INCREMENT,
  `train_id` bigint NOT NULL,
  `coach_no` int NOT NULL,
  PRIMARY KEY (`coach_id`),
  KEY `fk_coach_train` (`train_id`),
  CONSTRAINT `fk_coach_train` FOREIGN KEY (`train_id`) REFERENCES `train` (`train_id`)
) ENGINE=InnoDB AUTO_INCREMENT=211 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `amount` int DEFAULT NULL,
  `pay_method` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `pay_time` datetime DEFAULT NULL,
  `out_trade_no` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `fk_payment_order` (`order_id`),
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `ticket_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2082771829325766780 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refund` (
  `refund_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `amount` int DEFAULT NULL,
  `reason` varchar(45) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `refund_time` datetime DEFAULT NULL,
  PRIMARY KEY (`refund_id`),
  KEY `fk_refund_order` (`order_id`),
  CONSTRAINT `fk_refund_order` FOREIGN KEY (`order_id`) REFERENCES `ticket_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seat` (
  `seat_id` bigint NOT NULL AUTO_INCREMENT,
  `coach_id` bigint NOT NULL,
  `seat_no` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `seat_type_id` bigint NOT NULL,
  PRIMARY KEY (`seat_id`),
  KEY `fk_seat_coach` (`coach_id`),
  KEY `fk_seat_type` (`seat_type_id`),
  CONSTRAINT `fk_seat_coach` FOREIGN KEY (`coach_id`) REFERENCES `coach` (`coach_id`),
  CONSTRAINT `fk_seat_type` FOREIGN KEY (`seat_type_id`) REFERENCES `seat_type` (`seat_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10451 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seat_occupancy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `schedule_id` bigint NOT NULL,
  `seat_id` bigint NOT NULL,
  `start_order` int NOT NULL,
  `end_order` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_occupancy_seat` (`seat_id`),
  KEY `idx_seat_occupancy_search` (`schedule_id`,`seat_id`,`start_order`,`end_order`),
  CONSTRAINT `fk_occupancy_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `train_schedule` (`schedule_id`),
  CONSTRAINT `fk_occupancy_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`seat_id`)
) ENGINE=InnoDB AUTO_INCREMENT=286 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seat_type` (
  `seat_type_id` bigint NOT NULL AUTO_INCREMENT,
  `type_name` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`seat_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `station` (
  `station_id` bigint NOT NULL AUTO_INCREMENT,
  `station_name` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `city` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`station_id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `phone` char(11) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `idnum` char(18) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `role` varchar(10) COLLATE utf8mb4_general_ci DEFAULT 'USER',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket` (
  `ticket_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `schedule_id` bigint NOT NULL,
  `seat_id` bigint NOT NULL,
  `start_station_id` bigint NOT NULL,
  `end_station_id` bigint NOT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `status` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`ticket_id`),
  KEY `fk_ticket_order` (`order_id`),
  KEY `fk_ticket_schedule` (`schedule_id`),
  KEY `fk_ticket_seat` (`seat_id`),
  KEY `fk_ticket_start_station` (`start_station_id`),
  KEY `fk_ticket_end_station` (`end_station_id`),
  CONSTRAINT `fk_ticket_end_station` FOREIGN KEY (`end_station_id`) REFERENCES `station` (`station_id`),
  CONSTRAINT `fk_ticket_order` FOREIGN KEY (`order_id`) REFERENCES `ticket_order` (`order_id`),
  CONSTRAINT `fk_ticket_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `train_schedule` (`schedule_id`),
  CONSTRAINT `fk_ticket_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`seat_id`),
  CONSTRAINT `fk_ticket_start_station` FOREIGN KEY (`start_station_id`) REFERENCES `station` (`station_id`)
) ENGINE=InnoDB AUTO_INCREMENT=277 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `total_price` decimal(10,2) DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  KEY `fk_order_user` (`user_id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=274 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket_price` (
  `price_id` bigint NOT NULL AUTO_INCREMENT,
  `train_id` bigint NOT NULL,
  `start_station_id` bigint NOT NULL,
  `end_station_id` bigint NOT NULL,
  `seat_type_id` bigint NOT NULL,
  `price` int DEFAULT NULL,
  PRIMARY KEY (`price_id`),
  KEY `fk_price_train` (`train_id`),
  KEY `fk_price_start_station` (`start_station_id`),
  KEY `fk_price_end_station` (`end_station_id`),
  KEY `fk_price_seat_type` (`seat_type_id`),
  CONSTRAINT `fk_price_end_station` FOREIGN KEY (`end_station_id`) REFERENCES `station` (`station_id`),
  CONSTRAINT `fk_price_seat_type` FOREIGN KEY (`seat_type_id`) REFERENCES `seat_type` (`seat_type_id`),
  CONSTRAINT `fk_price_start_station` FOREIGN KEY (`start_station_id`) REFERENCES `station` (`station_id`),
  CONSTRAINT `fk_price_train` FOREIGN KEY (`train_id`) REFERENCES `train` (`train_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2094317544606711455 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `train` (
  `train_id` bigint NOT NULL AUTO_INCREMENT,
  `train_no` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `train_type` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `start_station_id` bigint DEFAULT NULL,
  `end_station_id` bigint DEFAULT NULL,
  PRIMARY KEY (`train_id`),
  KEY `fk_train_start_station` (`start_station_id`),
  KEY `fk_train_end_station` (`end_station_id`),
  CONSTRAINT `fk_train_end_station` FOREIGN KEY (`end_station_id`) REFERENCES `station` (`station_id`),
  CONSTRAINT `fk_train_start_station` FOREIGN KEY (`start_station_id`) REFERENCES `station` (`station_id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `train_schedule` (
  `schedule_id` bigint NOT NULL AUTO_INCREMENT,
  `train_id` bigint NOT NULL,
  `run_date` date NOT NULL,
  PRIMARY KEY (`schedule_id`),
  KEY `fk_schedule_train` (`train_id`),
  CONSTRAINT `fk_schedule_train` FOREIGN KEY (`train_id`) REFERENCES `train` (`train_id`)
) ENGINE=InnoDB AUTO_INCREMENT=654 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `train_station` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `train_id` bigint NOT NULL,
  `station_id` bigint NOT NULL,
  `arrival_time` time DEFAULT NULL,
  `departure_time` time DEFAULT NULL,
  `stop_order` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_train_station_train` (`train_id`),
  KEY `fk_train_station_station` (`station_id`),
  CONSTRAINT `fk_train_station_station` FOREIGN KEY (`station_id`) REFERENCES `station` (`station_id`),
  CONSTRAINT `fk_train_station_train` FOREIGN KEY (`train_id`) REFERENCES `train` (`train_id`)
) ENGINE=InnoDB AUTO_INCREMENT=495 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


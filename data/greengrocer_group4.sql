-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: greengrocer_group4
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `carrierrating`
--

DROP TABLE IF EXISTS `carrierrating`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carrierrating` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `carrier_id` int NOT NULL,
  `customer_id` int NOT NULL,
  `rating` int NOT NULL,
  `comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`),
  KEY `fk_rating_carrier` (`carrier_id`),
  KEY `fk_rating_customer` (`customer_id`),
  CONSTRAINT `fk_rating_carrier` FOREIGN KEY (`carrier_id`) REFERENCES `userinfo` (`id`),
  CONSTRAINT `fk_rating_customer` FOREIGN KEY (`customer_id`) REFERENCES `userinfo` (`id`),
  CONSTRAINT `fk_rating_order` FOREIGN KEY (`order_id`) REFERENCES `orderinfo` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_rating_range` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carrierrating`
--

LOCK TABLES `carrierrating` WRITE;
/*!40000 ALTER TABLE `carrierrating` DISABLE KEYS */;
/*!40000 ALTER TABLE `carrierrating` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messageinfo`
--

DROP TABLE IF EXISTS `messageinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messageinfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `from_user_id` int NOT NULL,
  `to_user_id` int NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `sent_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_msg_to` (`to_user_id`),
  KEY `idx_msg_from` (`from_user_id`),
  CONSTRAINT `fk_msg_from` FOREIGN KEY (`from_user_id`) REFERENCES `userinfo` (`id`),
  CONSTRAINT `fk_msg_to` FOREIGN KEY (`to_user_id`) REFERENCES `userinfo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messageinfo`
--

LOCK TABLES `messageinfo` WRITE;
/*!40000 ALTER TABLE `messageinfo` DISABLE KEYS */;
/*!40000 ALTER TABLE `messageinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orderinfo`
--

DROP TABLE IF EXISTS `orderinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orderinfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `carrier_id` int DEFAULT NULL,
  `status` enum('CREATED','ASSIGNED','DELIVERED','CANCELLED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CREATED',
  `order_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `requested_delivery_time` datetime NOT NULL,
  `delivered_time` datetime DEFAULT NULL,
  `total_cost` decimal(10,2) NOT NULL DEFAULT '0.00',
  `vat_rate` decimal(5,4) NOT NULL DEFAULT '0.1800',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `invoice_pdf` longblob,
  `invoice_log` longtext COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idx_order_customer` (`customer_id`),
  KEY `idx_order_carrier` (`carrier_id`),
  KEY `idx_order_status` (`status`),
  CONSTRAINT `fk_order_carrier` FOREIGN KEY (`carrier_id`) REFERENCES `userinfo` (`id`),
  CONSTRAINT `fk_order_customer` FOREIGN KEY (`customer_id`) REFERENCES `userinfo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orderinfo`
--

LOCK TABLES `orderinfo` WRITE;
/*!40000 ALTER TABLE `orderinfo` DISABLE KEYS */;
/*!40000 ALTER TABLE `orderinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orderiteminfo`
--

DROP TABLE IF EXISTS `orderiteminfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orderiteminfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `amount_kg` decimal(10,2) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `line_total` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_item_product` (`product_id`),
  KEY `idx_item_order` (`order_id`),
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `orderinfo` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_product` FOREIGN KEY (`product_id`) REFERENCES `productinfo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orderiteminfo`
--

LOCK TABLES `orderiteminfo` WRITE;
/*!40000 ALTER TABLE `orderiteminfo` DISABLE KEYS */;
/*!40000 ALTER TABLE `orderiteminfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productinfo`
--

DROP TABLE IF EXISTS `productinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productinfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('vegetable','fruit') COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock_kg` decimal(10,2) NOT NULL,
  `threshold_kg` decimal(10,2) NOT NULL,
  `image_blob` longblob,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_product_type` (`type`),
  KEY `idx_product_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productinfo`
--

LOCK TABLES `productinfo` WRITE;
/*!40000 ALTER TABLE `productinfo` DISABLE KEYS */;
INSERT INTO `productinfo` VALUES (1,'Tomato','vegetable',39.90,50.00,10.00,NULL,1),(2,'Cucumber','vegetable',34.90,40.00,8.00,NULL,1),(3,'Potato','vegetable',24.90,80.00,15.00,NULL,1),(4,'Onion','vegetable',19.90,70.00,12.00,NULL,1),(5,'Pepper','vegetable',49.90,30.00,6.00,NULL,1),(6,'Eggplant','vegetable',44.90,25.00,5.00,NULL,1),(7,'Zucchini','vegetable',29.90,35.00,7.00,NULL,1),(8,'Carrot','vegetable',27.90,45.00,9.00,NULL,1),(9,'Lettuce','vegetable',22.90,20.00,4.00,NULL,1),(10,'Spinach','vegetable',26.90,18.00,3.00,NULL,1),(11,'Broccoli','vegetable',59.90,15.00,3.00,NULL,1),(12,'Cauliflower','vegetable',54.90,14.00,3.00,NULL,1),(13,'Apple','fruit',34.90,60.00,12.00,NULL,1),(14,'Pear','fruit',39.90,40.00,8.00,NULL,1),(15,'Banana','fruit',64.90,35.00,7.00,NULL,1),(16,'Orange','fruit',29.90,70.00,14.00,NULL,1),(17,'Mandarin','fruit',32.90,55.00,11.00,NULL,1),(18,'Strawberry','fruit',89.90,20.00,4.00,NULL,1),(19,'Grapes','fruit',79.90,25.00,5.00,NULL,1),(20,'Peach','fruit',74.90,22.00,4.00,NULL,1),(21,'Cherry','fruit',119.90,12.00,2.50,NULL,1),(22,'Kiwi','fruit',69.90,18.00,3.50,NULL,1),(23,'Pineapple','fruit',129.90,10.00,2.00,NULL,1),(24,'Lemon','fruit',27.90,50.00,10.00,NULL,1);
/*!40000 ALTER TABLE `productinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `userinfo`
--

DROP TABLE IF EXISTS `userinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `userinfo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('customer','carrier','owner') COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `userinfo`
--

LOCK TABLES `userinfo` WRITE;
/*!40000 ALTER TABLE `userinfo` DISABLE KEYS */;
INSERT INTO `userinfo` VALUES (1,'cust','cust','customer','Ahmet Müşteri','Istanbul','555-000-0001'),(2,'carr','carr','carrier','Carrier User','Istanbul','555-000-0002'),(3,'own','own','owner','Birdem Üstündağ','Istanbul','555-000-0003');
/*!40000 ALTER TABLE `userinfo` ENABLE KEYS */;
UNLOCK TABLES;

-- Table structure for table `user_coupons`
DROP TABLE IF EXISTS `user_coupons`;
CREATE TABLE `user_coupons` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `coupon_id` int NOT NULL,
  `redeemed` tinyint(1) NOT NULL DEFAULT '0',
  `assigned_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uc_user` (`user_id`),
  KEY `idx_uc_coupon` (`coupon_id`),
  CONSTRAINT `fk_uc_user` FOREIGN KEY (`user_id`) REFERENCES `userinfo` (`id`),
  CONSTRAINT `fk_uc_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `couponinfo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SAMPLE COUPONS: WELCOME10 (welcome), SAVE20 (reward for large purchases), FREESHIP (free delivery code), LOYAL5 (loyalty small discount)
INSERT INTO `couponinfo` (code, discount_rate, is_active) VALUES
  ('WELCOME10', 10.0, 1),
  ('SAVE20', 20.0, 1),
  ('FREESHIP', 0.0, 1),
  ('LOYAL5', 5.0, 1)
ON DUPLICATE KEY UPDATE discount_rate = VALUES(discount_rate), is_active = VALUES(is_active);

-- Assign WELCOME10 to all existing users (if not already assigned)
INSERT INTO user_coupons (user_id, coupon_id, redeemed, assigned_at)
SELECT u.id, c.id, 0, NOW()
FROM userinfo u
JOIN couponinfo c ON c.code = 'WELCOME10'
WHERE NOT EXISTS (
  SELECT 1 FROM user_coupons uc WHERE uc.user_id = u.id AND uc.coupon_id = c.id
);

-- Trigger: award SAVE20 coupon to customer when an order of 500 TL or more is created
DELIMITER $$
CREATE TRIGGER award_save20 AFTER INSERT ON orderinfo
FOR EACH ROW
BEGIN
  DECLARE couponId INT;
  SELECT id INTO couponId FROM couponinfo WHERE code = 'SAVE20' AND is_active = 1 LIMIT 1;
  IF couponId IS NOT NULL AND NEW.total_cost >= 500 THEN
    IF NOT EXISTS (SELECT 1 FROM user_coupons uc WHERE uc.user_id = NEW.customer_id AND uc.coupon_id = couponId AND uc.redeemed = 0) THEN
      INSERT INTO user_coupons (user_id, coupon_id, redeemed, assigned_at) VALUES (NEW.customer_id, couponId, 0, NOW());
    END IF;
  END IF;
END$$
DELIMITER ;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-20 21:37:18

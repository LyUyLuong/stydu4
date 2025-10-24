-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: stydu5
-- ------------------------------------------------------
-- Server version	8.0.43

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
-- Table structure for table `answer_entity`
--

DROP TABLE IF EXISTS `answer_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `answer_entity` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `content` text,
  `is_correct` bit(1) DEFAULT NULL,
  `mark` varchar(255) DEFAULT NULL,
  `question_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpjcu6kejoqdkrqbjid2x3wehh` (`question_id`),
  CONSTRAINT `FKpjcu6kejoqdkrqbjid2x3wehh` FOREIGN KEY (`question_id`) REFERENCES `question_test_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `answer_entity`
--

LOCK TABLES `answer_entity` WRITE;
/*!40000 ALTER TABLE `answer_entity` DISABLE KEYS */;
INSERT INTO `answer_entity` VALUES ('056fd054-b353-4993-9f7c-16ada734be9b','admin','2025-10-23 13:05:37.514598','admin','2025-10-23 13:05:37.514598','to employ.',_binary '\0','B','67514033-2f47-4d13-9e11-0a07c64e84c3'),('06032f1d-f471-4237-b87e-338499f298a9','admin','2025-10-23 12:33:08.138510','admin','2025-10-23 12:33:08.138510','There are some tables and chairs outdoors.',_binary '','A','f491998f-d854-4a7e-a36a-f6419239b422'),('0828ceb9-10bb-4e6d-9cec-22238705b502','admin','2025-10-23 12:33:08.139014','admin','2025-10-23 12:33:08.139014','There are plastic umbrellas on the tables.',_binary '','C','f491998f-d854-4a7e-a36a-f6419239b422'),('094b3e5c-457d-44ad-8960-66a1ad1e52b7','admin','2025-10-23 12:49:36.275863','admin','2025-10-23 12:49:36.275863','A software upgrade..',_binary '\0','D','6b501374-7cd9-438d-8758-27399694e65a'),('0d802a00-1409-4890-951b-122fb0a8143f','admin','2025-10-23 13:15:22.783156','admin','2025-10-23 13:15:22.783156','recover.',_binary '\0','D','78afd35d-626e-4871-8b63-1d1d1e3eafa8'),('16ccf99a-41ed-4cd8-9526-8c6fc99d3f7a','admin','2025-10-23 12:33:08.139014','admin','2025-10-23 12:33:08.139014','There are many flowers in the garden.',_binary '\0','D','f491998f-d854-4a7e-a36a-f6419239b422'),('1eeaed7f-11be-4f4e-94af-4876105fdc1a','admin','2025-10-23 12:52:01.042890','admin','2025-10-23 12:52:01.042890','Order a replacement part.',_binary '\0','A','f807c3d4-e2a2-44fb-9682-015842f7f834'),('2272c752-ce5e-4a13-be94-3284b3515902','admin','2025-10-23 13:01:18.811005','admin','2025-10-23 13:01:18.811005','Tea shop.',_binary '\0','B','1c68a9d1-b484-4bfe-bdb1-d16c8cb4ebbd'),('238252f2-ce01-4a72-b91e-48214a9aed34','admin','2025-10-23 13:13:55.634174','admin','2025-10-23 13:13:55.634174','There will be noise and chaos as a result.',_binary '\0','D','8e6ddc84-1a4c-4cb0-be27-b3a46f303fbe'),('2549e38a-88a3-484e-a0d2-708edafda94f','admin','2025-10-23 13:00:20.092973','admin','2025-10-23 13:00:20.092973','Get to work faster than he does.',_binary '','C','8d0f7dd6-6839-4201-af3b-b49a4b8d3f15'),('2ba1ce43-0a75-4d0d-a032-c47a6ec4b677','admin','2025-10-23 13:14:42.949768','admin','2025-10-23 13:14:42.949768','after.',_binary '\0','B','85680c7a-1a0f-40f1-8dde-b98f7c9bc148'),('347e49e6-3328-4ac1-bdfa-794f976ec5d6','admin','2025-10-23 12:50:46.941930','admin','2025-10-23 12:50:46.941930','The phone number was wrong.',_binary '\0','D','5673a543-d1cf-4c0c-b022-4cf589a4af39'),('370ae490-3415-450b-8093-2bdf1f6608eb','admin','2025-10-23 12:49:36.273865','admin','2025-10-23 12:49:36.273865','What are the speakers mainly discussing.',_binary '\0','A','6b501374-7cd9-438d-8758-27399694e65a'),('3ad10604-2ae9-4105-82c5-5ab0a5f76a21','admin','2025-10-23 13:14:42.949768','admin','2025-10-23 13:14:42.949768','during.',_binary '','C','85680c7a-1a0f-40f1-8dde-b98f7c9bc148'),('3bf569da-8d48-441d-b18e-739c383f1c12','admin','2025-10-23 12:59:17.337277','admin','2025-10-23 12:59:17.337277','Which coffee shop to visit.',_binary '\0','B','2fd1e26b-4a96-4beb-9939-a5989c4bb609'),('3f13f3d7-53cd-44cb-89fd-e326cd789db6','admin','2025-10-23 13:14:42.949768','admin','2025-10-23 13:14:42.949768','before.',_binary '\0','A','85680c7a-1a0f-40f1-8dde-b98f7c9bc148'),('42e5a0c7-1556-40f9-86d1-dca1626c53a6','admin','2025-10-23 12:34:42.840506','admin','2025-10-23 12:34:42.840506','The woman is typing on her computer.',_binary '\0','B','3ae04aa5-f214-4a5e-b138-57c2e5d3e8aa'),('436810ae-3f1a-4665-bcf5-cfa70e472d63','admin','2025-10-23 13:15:22.782123','admin','2025-10-23 13:15:22.782123','develop.',_binary '\0','A','78afd35d-626e-4871-8b63-1d1d1e3eafa8'),('466bf153-b4fc-4ff8-afd0-518bd26d9a34','admin','2025-10-23 12:22:05.254809','admin','2025-10-23 12:22:05.254809','The woman is writing in her notebook.',_binary '\0','D','26668b59-d3fb-4fe7-b9e0-a78f34a56524'),('46aa9f75-6e2c-4188-aa74-2500410b5eb5','admin','2025-10-23 12:33:08.138510','admin','2025-10-23 12:33:08.138510','There are some people sitting at the tables.',_binary '\0','B','f491998f-d854-4a7e-a36a-f6419239b422'),('4791560d-0dda-441b-b714-e6bb68887d94','admin','2025-10-23 13:13:55.634174','admin','2025-10-23 13:13:55.634174','As a result, the convenience shops will be closed.',_binary '\0','B','8e6ddc84-1a4c-4cb0-be27-b3a46f303fbe'),('57256348-f5ea-4568-a49d-c97d61711138','admin','2025-10-23 13:01:18.811996','admin','2025-10-23 13:01:18.811996','Jake\'s Diner.',_binary '\0','D','1c68a9d1-b484-4bfe-bdb1-d16c8cb4ebbd'),('590eeaec-5c0c-4ef5-8467-9c1ecaf5ba81','admin','2025-10-23 12:52:01.043898','admin','2025-10-23 12:52:01.043898','Contact the woman.',_binary '','C','f807c3d4-e2a2-44fb-9682-015842f7f834'),('5e9acaab-a0bc-4f31-a230-9d3254efeb3e','admin','2025-10-23 13:04:36.841067','admin','2025-10-23 13:04:36.841067','direct.',_binary '\0','D','bedc1470-ede3-4788-8d0d-68c968867a53'),('604fee6f-dc10-4fd9-820a-2f53f59d886a','admin','2025-10-23 12:24:58.548577','admin','2025-10-23 12:24:58.548577','The woman is preparing for dinner.',_binary '','C','3cbbf7a0-1b77-4e38-935f-0d91410639b0'),('62786f10-8bca-456e-a055-26b4b37ffe7e','admin','2025-10-23 12:24:58.548577','admin','2025-10-23 12:24:58.548577','The woman is baking a cake.',_binary '\0','B','3cbbf7a0-1b77-4e38-935f-0d91410639b0'),('6bef9a31-cf7a-4ee5-894f-9c7cc59ba0db','admin','2025-10-23 13:15:22.783156','admin','2025-10-23 13:15:22.783156','rectify.',_binary '\0','C','78afd35d-626e-4871-8b63-1d1d1e3eafa8'),('741a161f-b2e2-4a74-adcf-230de74afcba','admin','2025-10-23 13:15:22.782123','admin','2025-10-23 13:15:22.782123','improve.',_binary '','B','78afd35d-626e-4871-8b63-1d1d1e3eafa8'),('7b887cab-b5bb-4086-9876-2225f4716078','admin','2025-10-23 12:34:42.840506','admin','2025-10-23 12:34:42.840506','The man is using the calculator.',_binary '','C','3ae04aa5-f214-4a5e-b138-57c2e5d3e8aa'),('7e80137b-afd3-4f5a-9105-d3489a93bb80','admin','2025-10-23 13:06:39.417352','admin','2025-10-23 13:06:39.417352','formula.',_binary '\0','C','2caa899f-a47d-413f-bc48-e1feac94ba9b'),('818f2ebb-6036-437b-9040-e349f7f6a48b','admin','2025-10-23 12:34:42.840506','admin','2025-10-23 12:34:42.840506','The man is writing something onto the notepad.',_binary '','D','3ae04aa5-f214-4a5e-b138-57c2e5d3e8aa'),('82f40c28-8ffd-4fb2-bf76-ea808408abda','admin','2025-10-23 13:06:39.416355','admin','2025-10-23 13:06:39.416355','experience.',_binary '\0','A','2caa899f-a47d-413f-bc48-e1feac94ba9b'),('84d9a4f5-5ee1-4abf-a3d3-bcac35cf9d18','admin','2025-10-23 13:06:39.417352','admin','2025-10-23 13:06:39.417352','incentive.',_binary '\0','D','2caa899f-a47d-413f-bc48-e1feac94ba9b'),('8777ecf2-a3a1-4716-80d1-8e340ff30611','admin','2025-10-23 13:01:18.811996','admin','2025-10-23 13:01:18.811996','Java the Cup.',_binary '','C','1c68a9d1-b484-4bfe-bdb1-d16c8cb4ebbd'),('8990b9c8-6456-4a33-8da5-c06ec9557069','admin','2025-10-23 13:00:20.092973','admin','2025-10-23 13:00:20.092973','Participate in a car race.',_binary '\0','D','8d0f7dd6-6839-4201-af3b-b49a4b8d3f15'),('8ca10596-e4f3-4d51-9ed7-3c79f4f00d13','admin','2025-10-23 12:29:17.268977','admin','2025-10-23 12:29:17.268977','The man is holding some seafood.',_binary '','A','bdfa582b-7fcd-4fad-a97f-a05fe5bd2af9'),('8e74f8b7-0dbc-4d1a-afad-3b2f2b644a95','admin','2025-10-23 12:59:17.337277','admin','2025-10-23 12:59:17.337277','How far Cambridge is from their apartments.',_binary '\0','C','2fd1e26b-4a96-4beb-9939-a5989c4bb609'),('91589923-34c1-4c46-8cc1-a8543665d978','admin','2025-10-23 12:22:05.253805','admin','2025-10-23 12:22:05.253805','The woman is talking on the phone.',_binary '','A','26668b59-d3fb-4fe7-b9e0-a78f34a56524'),('91e3b453-e7ca-4d70-bd34-8492a67702fd','admin','2025-10-23 13:05:37.515612','admin','2025-10-23 13:05:37.515612','employ.',_binary '\0','D','67514033-2f47-4d13-9e11-0a07c64e84c3'),('93164a54-bab9-4b3b-8c95-c4fbd981ffbc','admin','2025-10-23 13:13:55.634174','admin','2025-10-23 13:13:55.634174','Because of this, hot meals will not be available for the patrons.',_binary '','C','8e6ddc84-1a4c-4cb0-be27-b3a46f303fbe'),('944230e9-d668-4c3f-beff-a761021ba1a8','admin','2025-10-23 12:59:17.337277','admin','2025-10-23 12:59:17.337277','The fastest route to work.',_binary '','D','2fd1e26b-4a96-4beb-9939-a5989c4bb609'),('993168d2-2c95-4597-8a05-2939ab606cae','admin','2025-10-23 13:01:18.811005','admin','2025-10-23 13:01:18.811005','Coffee Bean.',_binary '\0','A','1c68a9d1-b484-4bfe-bdb1-d16c8cb4ebbd'),('99881e10-4d20-49f2-9d5e-53d93d5765e0','admin','2025-10-23 12:29:17.269982','admin','2025-10-23 12:29:17.269982','The family is shopping for breakfast.',_binary '\0','D','bdfa582b-7fcd-4fad-a97f-a05fe5bd2af9'),('9ad6e90e-1cda-4dc4-9a99-a7fadbf11983','admin','2025-10-23 12:31:25.298536','admin','2025-10-23 12:31:25.298536','The man is making the frame with his hand.',_binary '','C','75bc0800-cb1a-4ff4-9bb9-db5665c13468'),('9c31ac2b-0f7c-4549-bb49-62fa23d22ebe','admin','2025-10-23 13:04:36.841067','admin','2025-10-23 13:04:36.841067','send.',_binary '\0','C','bedc1470-ede3-4788-8d0d-68c968867a53'),('9f361e6c-827d-4ab9-8ab2-a0215525f64b','admin','2025-10-23 12:52:01.043898','admin','2025-10-23 12:52:01.043898','Fill out a work order.',_binary '\0','D','f807c3d4-e2a2-44fb-9682-015842f7f834'),('9f54cf55-1e14-466b-8c8e-742772dd2599','admin','2025-10-23 13:06:39.416355','admin','2025-10-23 13:06:39.416355','growth.',_binary '','B','2caa899f-a47d-413f-bc48-e1feac94ba9b'),('a3fa85d9-cb99-4a1f-ab6b-04152b70b3d3','admin','2025-10-23 13:00:20.092973','admin','2025-10-23 13:00:20.092973','Make more money than he does.',_binary '\0','B','8d0f7dd6-6839-4201-af3b-b49a4b8d3f15'),('aa79727b-b766-4909-b65f-bff46d1c2d3f','admin','2025-10-23 12:50:46.940927','admin','2025-10-23 12:50:46.940927','The necessary tools are unavailable.',_binary '','A','5673a543-d1cf-4c0c-b022-4cf589a4af39'),('b0191343-6086-4d24-8241-2b8591c0ce08','admin','2025-10-23 12:31:25.297302','admin','2025-10-23 12:31:25.297302','The man is using a screwdriver to screw a nail into the building frame.',_binary '\0','A','75bc0800-cb1a-4ff4-9bb9-db5665c13468'),('b6fff712-c667-4819-9d81-0decaa6b40b6','admin','2025-10-23 12:24:58.548577','admin','2025-10-23 12:24:58.548577','The woman is frying some fish.',_binary '\0','D','3cbbf7a0-1b77-4e38-935f-0d91410639b0'),('b70a03e2-d15f-40ce-9383-05a96c3286b5','admin','2025-10-23 13:00:20.091967','admin','2025-10-23 13:00:20.091967','Keep losing the game.',_binary '\0','A','8d0f7dd6-6839-4201-af3b-b49a4b8d3f15'),('b8c5c278-ab3a-4daf-b68b-849b74d10216','admin','2025-10-23 12:50:46.941930','admin','2025-10-23 12:50:46.941930','The wall is too weak.',_binary '\0','C','5673a543-d1cf-4c0c-b022-4cf589a4af39'),('be874c1f-9d66-4e22-a0df-41112ce32c73','admin','2025-10-23 12:59:17.336277','admin','2025-10-23 12:59:17.336277','Their GPS systems..',_binary '\0','A','2fd1e26b-4a96-4beb-9939-a5989c4bb609'),('ca9c31bd-8e5b-4bf7-aedb-2ec014e22f11','admin','2025-10-23 12:31:25.298536','admin','2025-10-23 12:31:25.298536','The man is hammering something into a building frame.',_binary '','B','75bc0800-cb1a-4ff4-9bb9-db5665c13468'),('cea2bfe4-6b16-41f8-9382-0eff00f7a37b','admin','2025-10-23 13:13:55.634174','admin','2025-10-23 13:13:55.634174','This will take a lot of work.',_binary '\0','A','8e6ddc84-1a4c-4cb0-be27-b3a46f303fbe'),('d05fd62e-7cc5-4966-b1c3-7d791783b6bd','admin','2025-10-23 12:22:05.253805','admin','2025-10-23 12:22:05.253805','The woman is using her cell phone.',_binary '\0','B','26668b59-d3fb-4fe7-b9e0-a78f34a56524'),('d1497e32-4407-4a34-ae41-39a461469626','admin','2025-10-23 12:49:36.275863','admin','2025-10-23 12:49:36.275863','The date of a presentation.',_binary '\0','C','6b501374-7cd9-438d-8758-27399694e65a'),('d21e61f3-37e1-49fb-b65c-2600db244864','admin','2025-10-23 12:31:25.298536','admin','2025-10-23 12:31:25.298536','The man is wearing protective glasses.',_binary '\0','D','75bc0800-cb1a-4ff4-9bb9-db5665c13468'),('d491d558-47a6-48a9-91c6-45f9aaa5a442','admin','2025-10-23 12:49:36.274869','admin','2025-10-23 12:49:36.274869','The installation of a television.',_binary '','B','6b501374-7cd9-438d-8758-27399694e65a'),('d549c58f-f0f2-49ba-863a-13adbe248e4f','admin','2025-10-23 13:04:36.841067','admin','2025-10-23 13:04:36.841067','write.',_binary '','B','bedc1470-ede3-4788-8d0d-68c968867a53'),('d583906a-41c6-48de-af21-56903bba0c29','admin','2025-10-23 13:14:42.949768','admin','2025-10-23 13:14:42.949768','within.',_binary '\0','D','85680c7a-1a0f-40f1-8dde-b98f7c9bc148'),('db140bff-0a8d-4b24-8411-ab54ccad374f','admin','2025-10-23 12:52:01.042890','admin','2025-10-23 12:52:01.042890','Consult an instruction manual.',_binary '\0','B','f807c3d4-e2a2-44fb-9682-015842f7f834'),('dc603846-14e9-4d52-9b46-9656318ec42b','admin','2025-10-23 12:29:17.268977','admin','2025-10-23 12:29:17.268977','The woman is baking a crab.',_binary '\0','B','bdfa582b-7fcd-4fad-a97f-a05fe5bd2af9'),('df8921ec-8717-4f07-9454-f91f3d53e7b8','admin','2025-10-23 13:05:37.514598','admin','2025-10-23 13:05:37.514598','will employ.',_binary '','A','67514033-2f47-4d13-9e11-0a07c64e84c3'),('e3dec30a-3576-4f79-bf12-16a120156a34','admin','2025-10-23 13:04:36.841067','admin','2025-10-23 13:04:36.841067','fix.',_binary '\0','A','bedc1470-ede3-4788-8d0d-68c968867a53'),('e744c245-72a3-4089-a764-3c136dd3cd76','admin','2025-10-23 12:29:17.268977','admin','2025-10-23 12:29:17.268977','They are scared of the crab.',_binary '','C','bdfa582b-7fcd-4fad-a97f-a05fe5bd2af9'),('f3dffe59-8a08-4343-bc8d-642a99a20525','admin','2025-10-23 12:34:42.840506','admin','2025-10-23 12:34:42.840506','They are looking at each other.',_binary '\0','A','3ae04aa5-f214-4a5e-b138-57c2e5d3e8aa'),('f874ad7e-2999-4340-bb99-545f23d88e8a','admin','2025-10-23 12:50:46.941930','admin','2025-10-23 12:50:46.941930','The office is closed.',_binary '\0','B','5673a543-d1cf-4c0c-b022-4cf589a4af39'),('f9a6d12a-078b-4439-9130-c15d65247c1c','admin','2025-10-23 12:24:58.547572','admin','2025-10-23 12:24:58.547572','The woman is cooking some bacon.',_binary '','A','3cbbf7a0-1b77-4e38-935f-0d91410639b0'),('faad3fb0-94cd-45d8-9903-5b95c701bfcb','admin','2025-10-23 13:05:37.514598','admin','2025-10-23 13:05:37.514598','has been employed.',_binary '\0','C','67514033-2f47-4d13-9e11-0a07c64e84c3'),('fda4c320-0392-4465-ad02-215701cbd3db','admin','2025-10-23 12:22:05.254809','admin','2025-10-23 12:22:05.254809','The woman is typing on the laptop.',_binary '','C','26668b59-d3fb-4fe7-b9e0-a78f34a56524');
/*!40000 ALTER TABLE `answer_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `files`
--

DROP TABLE IF EXISTS `files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `files` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `content_type` varchar(100) NOT NULL,
  `description` text,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint NOT NULL,
  `file_type` enum('AUDIO','DOCUMENT','IMAGE') NOT NULL,
  `file_url` varchar(500) NOT NULL,
  `original_filename` varchar(255) NOT NULL,
  `stored_filename` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `files`
--

LOCK TABLES `files` WRITE;
/*!40000 ALTER TABLE `files` DISABLE KEYS */;
INSERT INTO `files` VALUES ('00a63843-aaea-4df0-9095-32f7fcf06d6c','admin','2025-10-23 13:10:48.889198','admin','2025-10-23 13:10:48.889198','image/png',NULL,'images/question-groups/2025/10/23/b57b42e4-4b27-4511-a3d7-90fb33ee87c2.png',82026,'IMAGE','http://localhost:8080/api/v1/files/00a63843-aaea-4df0-9095-32f7fcf06d6c','145-147.png','b57b42e4-4b27-4511-a3d7-90fb33ee87c2.png'),('04fcd5dd-2c63-41c6-a37f-8e205405f67a','admin','2025-10-23 12:11:08.795013','admin','2025-10-23 12:11:08.795013','audio/mpeg',NULL,'audio/tests/2025/10/23/c8859acf-2cb5-4ff1-8c39-e749acbcc7c4.mp3',45039195,'AUDIO','http://localhost:8080/api/v1/files/04fcd5dd-2c63-41c6-a37f-8e205405f67a','eco_toeic_1000_test_1_eco_toeic_1000_test_1.mp3','c8859acf-2cb5-4ff1-8c39-e749acbcc7c4.mp3'),('0a45aa7a-372a-4dcc-8147-4afa88c4a188','admin','2025-10-23 12:34:42.803854','admin','2025-10-23 12:34:42.803854','audio/mpeg',NULL,'audio/questions/2025/10/23/2acd5512-19b1-49e1-b7ef-61b781484271.mp3',396261,'AUDIO','http://localhost:8080/api/v1/files/0a45aa7a-372a-4dcc-8147-4afa88c4a188','eco_toeic_1000_test_1_6.mp3','2acd5512-19b1-49e1-b7ef-61b781484271.mp3'),('0da3fd4d-f017-412a-a0f5-c732746ff19d','admin','2025-10-23 13:01:18.804982','admin','2025-10-23 13:01:18.804982','audio/mpeg',NULL,'audio/questions/2025/10/23/8b81c105-d65c-4a16-bae1-2cf22a19dd53.mp3',1271908,'AUDIO','http://localhost:8080/api/v1/files/0da3fd4d-f017-412a-a0f5-c732746ff19d','eco_toeic_1000_test_1_68_70.mp3','8b81c105-d65c-4a16-bae1-2cf22a19dd53.mp3'),('10c5c740-7aad-4380-a18d-906afa317ffc','admin','2025-10-23 12:31:25.260520','admin','2025-10-23 12:31:25.260520','audio/mpeg',NULL,'audio/questions/2025/10/23/b9532b20-d6c7-4fda-a11e-7d0da58e89ea.mp3',443490,'AUDIO','http://localhost:8080/api/v1/files/10c5c740-7aad-4380-a18d-906afa317ffc','eco_toeic_1000_test_1_4.mp3','b9532b20-d6c7-4fda-a11e-7d0da58e89ea.mp3'),('2cec4f63-2cdd-4daf-8bf3-8cddaae9d4a2','admin','2025-10-23 12:31:25.295289','admin','2025-10-23 12:31:25.295289','image/png',NULL,'images/questions/2025/10/23/ce0cd175-406e-406e-b779-9515500e10f4.png',52864,'IMAGE','http://localhost:8080/api/v1/files/2cec4f63-2cdd-4daf-8bf3-8cddaae9d4a2','eco_toeic_1000_test_1_eco_toeic_1000_test_1_4.png','ce0cd175-406e-406e-b779-9515500e10f4.png'),('34557006-302e-4245-bde3-c72df1797502','admin','2025-10-23 13:13:55.632945','admin','2025-10-23 13:13:55.632945','image/png',NULL,'images/questions/2025/10/23/6373a3ee-79b7-4d1a-8f48-b03292a0070b.png',82026,'IMAGE','http://localhost:8080/api/v1/files/34557006-302e-4245-bde3-c72df1797502','144-146.png','6373a3ee-79b7-4d1a-8f48-b03292a0070b.png'),('3ea22204-4565-4905-bcae-0a1d9f351681','admin','2025-10-23 12:49:36.262254','admin','2025-10-23 12:49:36.262254','audio/mpeg',NULL,'audio/questions/2025/10/23/02ed92bc-1b79-431b-bcae-cc028e64327f.mp3',1089260,'AUDIO','http://localhost:8080/api/v1/files/3ea22204-4565-4905-bcae-0a1d9f351681','eco_toeic_1000_test_1_32_34.mp3','02ed92bc-1b79-431b-bcae-cc028e64327f.mp3'),('49a548d4-03f9-4c8c-a16c-b9ff49e9fe50','admin','2025-10-23 12:24:58.545574','admin','2025-10-23 12:24:58.545574','image/png',NULL,'images/questions/2025/10/23/21c2bf8d-7d8e-4943-b51e-d7c9fc0a1074.png',38622,'IMAGE','http://localhost:8080/api/v1/files/49a548d4-03f9-4c8c-a16c-b9ff49e9fe50','eco_toeic_1000_test_1_eco_toeic_1000_test_1_2.png','21c2bf8d-7d8e-4943-b51e-d7c9fc0a1074.png'),('596efb65-a1d9-4b76-8b38-3a3892bdb78e','admin','2025-10-23 12:52:01.039590','admin','2025-10-23 12:52:01.039590','audio/mpeg',NULL,'audio/questions/2025/10/23/00f3e876-331b-4361-9b81-6fb5c40dc7aa.mp3',1089260,'AUDIO','http://localhost:8080/api/v1/files/596efb65-a1d9-4b76-8b38-3a3892bdb78e','eco_toeic_1000_test_1_32_34.mp3','00f3e876-331b-4361-9b81-6fb5c40dc7aa.mp3'),('5ba4f2e6-13fd-4a27-a6f4-4d7ed5ad9b5e','admin','2025-10-23 13:00:20.084344','admin','2025-10-23 13:00:20.084344','audio/mpeg',NULL,'audio/questions/2025/10/23/c06c53e2-8af3-419d-908a-b2d671fef187.mp3',1271908,'AUDIO','http://localhost:8080/api/v1/files/5ba4f2e6-13fd-4a27-a6f4-4d7ed5ad9b5e','eco_toeic_1000_test_1_68_70.mp3','c06c53e2-8af3-419d-908a-b2d671fef187.mp3'),('5e19a0f6-4727-4895-90d0-8ef8d3653b24','admin','2025-10-23 12:33:08.132748','admin','2025-10-23 12:33:08.132748','audio/mpeg',NULL,'audio/questions/2025/10/23/dfa7fba5-4e12-4ad2-8776-fd8eff6fccf7.mp3',412560,'AUDIO','http://localhost:8080/api/v1/files/5e19a0f6-4727-4895-90d0-8ef8d3653b24','eco_toeic_1000_test_1_5.mp3','dfa7fba5-4e12-4ad2-8776-fd8eff6fccf7.mp3'),('62cd275c-c029-4b4a-89af-ebc34d885b62','admin','2025-10-23 13:01:18.808993','admin','2025-10-23 13:01:18.808993','image/png',NULL,'images/questions/2025/10/23/c4f86904-1ab2-4d53-bb6b-5d9d1b443045.png',273338,'IMAGE','http://localhost:8080/api/v1/files/62cd275c-c029-4b4a-89af-ebc34d885b62','eco_toeic_1000_test_1_eco_toeic_1000_test_1_68_70.png','c4f86904-1ab2-4d53-bb6b-5d9d1b443045.png'),('648806b9-000c-46bb-92ae-d5a49c9ec835','admin','2025-10-23 12:29:17.266981','admin','2025-10-23 12:29:17.266981','image/png',NULL,'images/questions/2025/10/23/115cd434-f3d5-445c-b596-0ba12aef7944.png',42651,'IMAGE','http://localhost:8080/api/v1/files/648806b9-000c-46bb-92ae-d5a49c9ec835','eco_toeic_1000_test_1_eco_toeic_1000_test_1_3.png','115cd434-f3d5-445c-b596-0ba12aef7944.png'),('859715e6-98a4-4baf-b0f3-4a84efcfcaa4','admin','2025-10-23 12:59:17.335279','admin','2025-10-23 12:59:17.335279','image/png',NULL,'images/questions/2025/10/23/d5655b00-b76b-4ec5-83fe-f01f4a5aeec4.png',273338,'IMAGE','http://localhost:8080/api/v1/files/859715e6-98a4-4baf-b0f3-4a84efcfcaa4','eco_toeic_1000_test_1_eco_toeic_1000_test_1_68_70.png','d5655b00-b76b-4ec5-83fe-f01f4a5aeec4.png'),('8f3c02d1-cc7a-4aaa-a6a9-42cfded71c97','admin','2025-10-23 12:56:38.657534','admin','2025-10-23 12:56:38.657534','image/png',NULL,'images/question-groups/2025/10/23/b55e53e5-82b4-4d5e-922f-aed72a0013e4.png',273338,'IMAGE','http://localhost:8080/api/v1/files/8f3c02d1-cc7a-4aaa-a6a9-42cfded71c97','eco_toeic_1000_test_1_eco_toeic_1000_test_1_68_70.png','b55e53e5-82b4-4d5e-922f-aed72a0013e4.png'),('c0314944-b768-494a-9697-9d984d49478c','admin','2025-10-23 12:56:38.622591','admin','2025-10-23 12:56:38.622591','audio/mpeg',NULL,'audio/question-groups/2025/10/23/47b8320f-3a5a-4fde-a390-4875b62b35e1.mp3',1271908,'AUDIO','http://localhost:8080/api/v1/files/c0314944-b768-494a-9697-9d984d49478c','eco_toeic_1000_test_1_68_70.mp3','47b8320f-3a5a-4fde-a390-4875b62b35e1.mp3'),('c3c4165a-03a1-47b2-9b92-d3dead6d795b','admin','2025-10-23 12:29:17.262987','admin','2025-10-23 12:29:17.262987','audio/mpeg',NULL,'audio/questions/2025/10/23/2e8477e4-1fe8-4bbc-aef1-3f60abc775bb.mp3',385812,'AUDIO','http://localhost:8080/api/v1/files/c3c4165a-03a1-47b2-9b92-d3dead6d795b','eco_toeic_1000_test_1_3.mp3','2e8477e4-1fe8-4bbc-aef1-3f60abc775bb.mp3'),('ced162ea-de91-4209-8b03-31aea6f64451','admin','2025-10-23 12:33:08.136265','admin','2025-10-23 12:33:08.136265','image/png',NULL,'images/questions/2025/10/23/60e0433f-7e15-4345-9c9b-f8b902a10dec.png',71552,'IMAGE','http://localhost:8080/api/v1/files/ced162ea-de91-4209-8b03-31aea6f64451','eco_toeic_1000_test_1_eco_toeic_1000_test_1_5.png','60e0433f-7e15-4345-9c9b-f8b902a10dec.png'),('cee8b07d-8599-4475-8d06-a8cde354971d','admin','2025-10-23 12:46:21.462757','admin','2025-10-23 12:46:21.462757','audio/mpeg',NULL,'audio/question-groups/2025/10/23/556f91d6-22af-4d2c-b61b-fce5bd3c273a.mp3',1089260,'AUDIO','http://localhost:8080/api/v1/files/cee8b07d-8599-4475-8d06-a8cde354971d','eco_toeic_1000_test_1_32_34.mp3','556f91d6-22af-4d2c-b61b-fce5bd3c273a.mp3'),('cfe4fce3-489f-4ad7-93dd-8bd7ee6f5116','admin','2025-10-23 13:15:22.781105','admin','2025-10-23 13:15:22.781105','image/png',NULL,'images/questions/2025/10/23/1da76a2b-f1b4-493d-b62f-7a4671bda684.png',82026,'IMAGE','http://localhost:8080/api/v1/files/cfe4fce3-489f-4ad7-93dd-8bd7ee6f5116','144-146.png','1da76a2b-f1b4-493d-b62f-7a4671bda684.png'),('d9d33322-f1a7-405c-b3fd-c3e5bcd1f0d2','admin','2025-10-23 13:00:20.090771','admin','2025-10-23 13:00:20.090771','image/png',NULL,'images/questions/2025/10/23/dc227671-c1cc-4c32-bfad-e98e24625dbb.png',273338,'IMAGE','http://localhost:8080/api/v1/files/d9d33322-f1a7-405c-b3fd-c3e5bcd1f0d2','eco_toeic_1000_test_1_eco_toeic_1000_test_1_68_70.png','dc227671-c1cc-4c32-bfad-e98e24625dbb.png'),('dc17a699-cb9c-4767-80df-e12ed3d26926','admin','2025-10-23 12:59:17.330979','admin','2025-10-23 12:59:17.330979','audio/mpeg',NULL,'audio/questions/2025/10/23/d42c7b19-0692-4877-9aa0-a6c3a5efafb6.mp3',1271908,'AUDIO','http://localhost:8080/api/v1/files/dc17a699-cb9c-4767-80df-e12ed3d26926','eco_toeic_1000_test_1_68_70.mp3','d42c7b19-0692-4877-9aa0-a6c3a5efafb6.mp3'),('eafafdb8-9f44-458c-b756-f51269f49828','admin','2025-10-23 12:22:05.242940','admin','2025-10-23 12:22:05.242940','image/png',NULL,'images/questions/2025/10/23/c7e4225f-08b3-41d6-a729-93edfecfbe8a.png',50624,'IMAGE','http://localhost:8080/api/v1/files/eafafdb8-9f44-458c-b756-f51269f49828','eco_toeic_1000_test_1_eco_toeic_1000_test_1_1.png','c7e4225f-08b3-41d6-a729-93edfecfbe8a.png'),('eee80cd8-58c8-43f7-9b97-aa06547a9b4e','admin','2025-10-23 12:24:58.509916','admin','2025-10-23 12:24:58.509916','audio/mpeg',NULL,'audio/questions/2025/10/23/102fac73-3899-4550-adae-ee133acf5226.mp3',380378,'AUDIO','http://localhost:8080/api/v1/files/eee80cd8-58c8-43f7-9b97-aa06547a9b4e','eco_toeic_1000_test_1_2.mp3','102fac73-3899-4550-adae-ee133acf5226.mp3'),('f121aa5c-c5a8-40d3-886a-5b16e4c6edaf','admin','2025-10-23 12:22:05.237634','admin','2025-10-23 12:22:05.237634','audio/mpeg',NULL,'audio/questions/2025/10/23/7969f22a-85f9-4b95-884a-b9ef0d9b7e97.mp3',373690,'AUDIO','http://localhost:8080/api/v1/files/f121aa5c-c5a8-40d3-886a-5b16e4c6edaf','eco_toeic_1000_test_1_1.mp3','7969f22a-85f9-4b95-884a-b9ef0d9b7e97.mp3'),('f618e53e-90df-4cbd-ad5f-4a6ac352beec','admin','2025-10-23 12:34:42.838917','admin','2025-10-23 12:34:42.838917','image/png',NULL,'images/questions/2025/10/23/5e42e7e1-91d5-4292-8073-f41b6ce6d257.png',52250,'IMAGE','http://localhost:8080/api/v1/files/f618e53e-90df-4cbd-ad5f-4a6ac352beec','eco_toeic_1000_test_1_eco_toeic_1000_test_1_6.png','5e42e7e1-91d5-4292-8073-f41b6ce6d257.png'),('f7db01eb-9414-4d4c-a4d9-173b36135444','admin','2025-10-23 12:50:46.938928','admin','2025-10-23 12:50:46.938928','audio/mpeg',NULL,'audio/questions/2025/10/23/6109d409-7213-4fd1-aacb-5146e517848c.mp3',1089260,'AUDIO','http://localhost:8080/api/v1/files/f7db01eb-9414-4d4c-a4d9-173b36135444','eco_toeic_1000_test_1_32_34.mp3','6109d409-7213-4fd1-aacb-5146e517848c.mp3'),('f866aeb3-23f7-4eb9-a471-d2b2dff7ed54','admin','2025-10-23 13:14:42.948772','admin','2025-10-23 13:14:42.948772','image/png',NULL,'images/questions/2025/10/23/517facb4-4c30-489e-952c-97c698b8a1c1.png',82026,'IMAGE','http://localhost:8080/api/v1/files/f866aeb3-23f7-4eb9-a471-d2b2dff7ed54','144-146.png','517facb4-4c30-489e-952c-97c698b8a1c1.png');
/*!40000 ALTER TABLE `files` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `part_test_entity`
--

DROP TABLE IF EXISTS `part_test_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `part_test_entity` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` enum('PART_1_IELTS','PART_1_TOEIC','PART_2_IELTS','PART_2_TOEIC','PART_3_IELTS','PART_3_TOEIC','PART_4_IELTS','PART_4_TOEIC','PART_5_IELTS','PART_5_TOEIC','PART_6_IELTS','PART_6_TOEIC','PART_7_IELTS','PART_7_TOEIC') DEFAULT NULL,
  `test_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmxu1dshaktobxwtkhg2r0m4pa` (`test_id`),
  CONSTRAINT `FKmxu1dshaktobxwtkhg2r0m4pa` FOREIGN KEY (`test_id`) REFERENCES `test_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `part_test_entity`
--

LOCK TABLES `part_test_entity` WRITE;
/*!40000 ALTER TABLE `part_test_entity` DISABLE KEYS */;
INSERT INTO `part_test_entity` VALUES ('34e5ccb2-6b19-4183-be83-25df1b8f6f3a','admin','2025-10-23 12:12:12.542808','admin','2025-10-23 13:17:36.885980','Description for Part 1 for Test 1','Part 1 for Test 1','PART_1_TOEIC','bb6ebe70-a625-492b-bc02-bf6d64685e2f'),('382e715e-22b5-4aa0-bd1d-ed1c1882fdd1','admin','2025-10-23 12:12:29.032771','admin','2025-10-23 13:17:36.885980','Description for Part 3 for Test 1','Part 3 for Test 1','PART_3_TOEIC','bb6ebe70-a625-492b-bc02-bf6d64685e2f'),('a4b8a0e4-0fe7-48af-aa82-acc034b53aa6','admin','2025-10-23 13:08:45.003713','admin','2025-10-23 13:17:36.886980','Description for Part 6 for Test 1','Part 6 for Test 1','PART_6_TOEIC','bb6ebe70-a625-492b-bc02-bf6d64685e2f'),('ed40c457-8727-4562-87ec-f48c9c5cd5ec','admin','2025-10-23 13:01:59.616481','admin','2025-10-23 13:17:36.886980','Description for Part 5 for Test 1','Part 5 for Test 1','PART_5_TOEIC','bb6ebe70-a625-492b-bc02-bf6d64685e2f');
/*!40000 ALTER TABLE `part_test_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission_entity`
--

DROP TABLE IF EXISTS `permission_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission_entity` (
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission_entity`
--

LOCK TABLES `permission_entity` WRITE;
/*!40000 ALTER TABLE `permission_entity` DISABLE KEYS */;
/*!40000 ALTER TABLE `permission_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_group_entity`
--

DROP TABLE IF EXISTS `question_group_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_group_entity` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `content` text,
  `name` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `audio_id` varchar(255) DEFAULT NULL,
  `image_id` varchar(255) DEFAULT NULL,
  `part_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtnl5cuom70sae72mgd9wwkpxy` (`audio_id`),
  KEY `FK4n3l8w0nkitynnwccci199qgy` (`image_id`),
  KEY `FK34r13543a1ji3wkj21m8ihlng` (`part_id`),
  CONSTRAINT `FK34r13543a1ji3wkj21m8ihlng` FOREIGN KEY (`part_id`) REFERENCES `part_test_entity` (`id`),
  CONSTRAINT `FK4n3l8w0nkitynnwccci199qgy` FOREIGN KEY (`image_id`) REFERENCES `files` (`id`),
  CONSTRAINT `FKtnl5cuom70sae72mgd9wwkpxy` FOREIGN KEY (`audio_id`) REFERENCES `files` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_group_entity`
--

LOCK TABLES `question_group_entity` WRITE;
/*!40000 ALTER TABLE `question_group_entity` DISABLE KEYS */;
INSERT INTO `question_group_entity` VALUES ('ba034ca2-9a1f-4c2c-b48d-166cbb70340c','admin','2025-10-23 12:56:38.658535','admin','2025-10-23 12:56:38.658535','Question Group 68 - 70 - Part 3.','Question Group 68 - 70 - Part 3','MULTIPLE_CHOICE','c0314944-b768-494a-9697-9d984d49478c','8f3c02d1-cc7a-4aaa-a6a9-42cfded71c97','382e715e-22b5-4aa0-bd1d-ed1c1882fdd1'),('d07879df-54ca-43ca-905b-7db9766ef45f','admin','2025-10-23 13:10:48.891201','admin','2025-10-23 13:10:48.891201','Question Group 144 - 146 - Part 6.','Question Group 144 - 146 - Part 6','MULTIPLE_CHOICE',NULL,'00a63843-aaea-4df0-9095-32f7fcf06d6c','a4b8a0e4-0fe7-48af-aa82-acc034b53aa6'),('f038801c-ade4-4019-b47f-c6f26332f218','admin','2025-10-23 12:46:21.480796','admin','2025-10-23 12:46:21.480796','Question Group 32 - 34 - Part 3.','Question Group 32 - 34 - Part 3','MULTIPLE_CHOICE','cee8b07d-8599-4475-8d06-a8cde354971d',NULL,'382e715e-22b5-4aa0-bd1d-ed1c1882fdd1');
/*!40000 ALTER TABLE `question_group_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_test_entity`
--

DROP TABLE IF EXISTS `question_test_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_test_entity` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `content` text,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` enum('MULTIPLE_CHOICE','SHORT_ANSWER','TRUE_FALSE') DEFAULT NULL,
  `audio_id` varchar(255) DEFAULT NULL,
  `image_id` varchar(255) DEFAULT NULL,
  `part_id` varchar(255) DEFAULT NULL,
  `question_group_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7b4xt38vpot4pg9ge3ra24hov` (`audio_id`),
  KEY `FKqng6m5939ulnfn37opy3yc7yy` (`image_id`),
  KEY `FKaa2w7t85b8f21pk22og63guu6` (`part_id`),
  KEY `FKsvxscjhynh74abwxce97u3mo1` (`question_group_id`),
  CONSTRAINT `FK7b4xt38vpot4pg9ge3ra24hov` FOREIGN KEY (`audio_id`) REFERENCES `files` (`id`),
  CONSTRAINT `FKaa2w7t85b8f21pk22og63guu6` FOREIGN KEY (`part_id`) REFERENCES `part_test_entity` (`id`),
  CONSTRAINT `FKqng6m5939ulnfn37opy3yc7yy` FOREIGN KEY (`image_id`) REFERENCES `files` (`id`),
  CONSTRAINT `FKsvxscjhynh74abwxce97u3mo1` FOREIGN KEY (`question_group_id`) REFERENCES `question_group_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_test_entity`
--

LOCK TABLES `question_test_entity` WRITE;
/*!40000 ALTER TABLE `question_test_entity` DISABLE KEYS */;
INSERT INTO `question_test_entity` VALUES ('1c68a9d1-b484-4bfe-bdb1-d16c8cb4ebbd','admin','2025-10-23 13:01:18.810002','admin','2025-10-23 13:01:18.812994','Look at the list. Which shop does the man most likely stop at? ','TOEIC Part 3 - Questions about topic and purpose','Question 70 - Part 3','MULTIPLE_CHOICE','0da3fd4d-f017-412a-a0f5-c732746ff19d','62cd275c-c029-4b4a-89af-ebc34d885b62',NULL,'ba034ca2-9a1f-4c2c-b48d-166cbb70340c'),('26668b59-d3fb-4fe7-b9e0-a78f34a56524','admin','2025-10-23 12:22:05.248474','admin','2025-10-23 12:22:05.258760','Look at the picture marked number 1 in your test book.','TOEIC Part 1 - Photo description','Question 1 - Part 1','MULTIPLE_CHOICE','f121aa5c-c5a8-40d3-886a-5b16e4c6edaf','eafafdb8-9f44-458c-b756-f51269f49828','34e5ccb2-6b19-4183-be83-25df1b8f6f3a',NULL),('2caa899f-a47d-413f-bc48-e1feac94ba9b','admin','2025-10-23 13:06:39.416355','admin','2025-10-23 13:06:39.418355','The contractor had a fifteen-percent _____ in his business after advertising in the local newspaper.','TOEIC Part 5 - Vocabulary questions','Question 103 - Part 5','MULTIPLE_CHOICE',NULL,NULL,'ed40c457-8727-4562-87ec-f48c9c5cd5ec',NULL),('2fd1e26b-4a96-4beb-9939-a5989c4bb609','admin','2025-10-23 12:59:17.336277','admin','2025-10-23 12:59:17.338276','What are the speakers discussing? ','TOEIC Part 3 - Questions about topic and purpose','Question 68 - Part 3','MULTIPLE_CHOICE','dc17a699-cb9c-4767-80df-e12ed3d26926','859715e6-98a4-4baf-b0f3-4a84efcfcaa4',NULL,'ba034ca2-9a1f-4c2c-b48d-166cbb70340c'),('3ae04aa5-f214-4a5e-b138-57c2e5d3e8aa','admin','2025-10-23 12:34:42.840002','admin','2025-10-23 12:34:42.841797','Look at the picture marked number 6 in your test book.','TOEIC Part 1 - Photo description','Question 6 - Part 1','MULTIPLE_CHOICE','0a45aa7a-372a-4dcc-8147-4afa88c4a188','f618e53e-90df-4cbd-ad5f-4a6ac352beec','34e5ccb2-6b19-4183-be83-25df1b8f6f3a',NULL),('3cbbf7a0-1b77-4e38-935f-0d91410639b0','admin','2025-10-23 12:24:58.546573','admin','2025-10-23 12:24:58.549572','Look at the picture marked number 2 in your test book.','TOEIC Part 1 - Photo description','Question 2 - Part 1','MULTIPLE_CHOICE','eee80cd8-58c8-43f7-9b97-aa06547a9b4e','49a548d4-03f9-4c8c-a16c-b9ff49e9fe50','34e5ccb2-6b19-4183-be83-25df1b8f6f3a',NULL),('5673a543-d1cf-4c0c-b022-4cf589a4af39','admin','2025-10-23 12:50:46.939930','admin','2025-10-23 12:50:46.943925','What is the problem?.','TOEIC Part 3 - Questions about topic and purpose','Question 33 - Part 3','MULTIPLE_CHOICE','f7db01eb-9414-4d4c-a4d9-173b36135444',NULL,NULL,'f038801c-ade4-4019-b47f-c6f26332f218'),('67514033-2f47-4d13-9e11-0a07c64e84c3','admin','2025-10-23 13:05:37.514094','admin','2025-10-23 13:05:37.515612','Ms. Morgan recruited the individuals that the company _____ for the next three months.','TOEIC Part 5 - Vocabulary questions','Question 102 - Part 5','MULTIPLE_CHOICE',NULL,NULL,'ed40c457-8727-4562-87ec-f48c9c5cd5ec',NULL),('6b501374-7cd9-438d-8758-27399694e65a','admin','2025-10-23 12:49:36.267776','admin','2025-10-23 12:49:36.280372','What are the speakers mainly discussing?.','TOEIC Part 3 - Questions about topic and purpose','Question 32 - Part 3','MULTIPLE_CHOICE','3ea22204-4565-4905-bcae-0a1d9f351681',NULL,NULL,'f038801c-ade4-4019-b47f-c6f26332f218'),('75bc0800-cb1a-4ff4-9bb9-db5665c13468','admin','2025-10-23 12:31:25.297302','admin','2025-10-23 12:31:25.299537','Look at the picture marked number 4 in your test book.','TOEIC Part 1 - Photo description','Question 4 - Part 1','MULTIPLE_CHOICE','10c5c740-7aad-4380-a18d-906afa317ffc','2cec4f63-2cdd-4daf-8bf3-8cddaae9d4a2','34e5ccb2-6b19-4183-be83-25df1b8f6f3a',NULL),('78afd35d-626e-4871-8b63-1d1d1e3eafa8','admin','2025-10-23 13:15:22.782123','admin','2025-10-23 13:15:22.784167','146  ','TOEIC Part 6 - Questions about topic and purpose','Question 146 - Part 6','MULTIPLE_CHOICE',NULL,'cfe4fce3-489f-4ad7-93dd-8bd7ee6f5116',NULL,'d07879df-54ca-43ca-905b-7db9766ef45f'),('85680c7a-1a0f-40f1-8dde-b98f7c9bc148','admin','2025-10-23 13:14:42.948772','admin','2025-10-23 13:14:42.951067','145  ','TOEIC Part 6 - Questions about topic and purpose','Question 145 - Part 6','MULTIPLE_CHOICE',NULL,'f866aeb3-23f7-4eb9-a471-d2b2dff7ed54',NULL,'d07879df-54ca-43ca-905b-7db9766ef45f'),('8d0f7dd6-6839-4201-af3b-b49a4b8d3f15','admin','2025-10-23 13:00:20.091967','admin','2025-10-23 13:00:20.094970','What does the woman want to do? ','TOEIC Part 3 - Questions about topic and purpose','Question 69 - Part 3','MULTIPLE_CHOICE','5ba4f2e6-13fd-4a27-a6f4-4d7ed5ad9b5e','d9d33322-f1a7-405c-b3fd-c3e5bcd1f0d2',NULL,'ba034ca2-9a1f-4c2c-b48d-166cbb70340c'),('8e6ddc84-1a4c-4cb0-be27-b3a46f303fbe','admin','2025-10-23 13:13:55.632945','admin','2025-10-23 13:13:55.635207','144  ','TOEIC Part 6 - Questions about topic and purpose','Question 144 - Part 6','MULTIPLE_CHOICE',NULL,'34557006-302e-4245-bde3-c72df1797502',NULL,'d07879df-54ca-43ca-905b-7db9766ef45f'),('bdfa582b-7fcd-4fad-a97f-a05fe5bd2af9','admin','2025-10-23 12:29:17.266981','admin','2025-10-23 12:29:17.270492','Look at the picture marked number 3 in your test book.','TOEIC Part 1 - Photo description','Question 3 - Part 1','MULTIPLE_CHOICE','c3c4165a-03a1-47b2-9b92-d3dead6d795b','648806b9-000c-46bb-92ae-d5a49c9ec835','34e5ccb2-6b19-4183-be83-25df1b8f6f3a',NULL),('bedc1470-ede3-4788-8d0d-68c968867a53','admin','2025-10-23 13:04:36.840070','admin','2025-10-23 13:04:36.842065','When filling out the order form, please _____ your address clearly to prevent delays','TOEIC Part 5 - Vocabulary questions','Question 101 - Part 5','MULTIPLE_CHOICE',NULL,NULL,'ed40c457-8727-4562-87ec-f48c9c5cd5ec',NULL),('f491998f-d854-4a7e-a36a-f6419239b422','admin','2025-10-23 12:33:08.137438','admin','2025-10-23 12:33:08.140156','Look at the picture marked number 5 in your test book.','TOEIC Part 1 - Photo description','Question 5 - Part 1','MULTIPLE_CHOICE','5e19a0f6-4727-4895-90d0-8ef8d3653b24','ced162ea-de91-4209-8b03-31aea6f64451','34e5ccb2-6b19-4183-be83-25df1b8f6f3a',NULL),('f807c3d4-e2a2-44fb-9682-015842f7f834','admin','2025-10-23 12:52:01.041891','admin','2025-10-23 12:52:01.044948','What most likely will the man do first tomorrow?','TOEIC Part 3 - Questions about topic and purpose','Question 34 - Part 3','MULTIPLE_CHOICE','596efb65-a1d9-4b76-8b38-3a3892bdb78e',NULL,NULL,'f038801c-ade4-4019-b47f-c6f26332f218');
/*!40000 ALTER TABLE `question_test_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `result_entity`
--

DROP TABLE IF EXISTS `result_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `result_entity` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `complete_time` bigint DEFAULT NULL,
  `is_full_test` bit(1) DEFAULT NULL,
  `listening_correct_answer` int DEFAULT NULL,
  `listening_point` int DEFAULT NULL,
  `reading_correct_answer` int DEFAULT NULL,
  `reading_point` int DEFAULT NULL,
  `total_questions` int DEFAULT NULL,
  `type` enum('IELTS','TOEIC') DEFAULT NULL,
  `test_id` varchar(255) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKshfp5xelc7eyer4kuk4by5s1w` (`test_id`),
  KEY `FK6qa9n55hye9mc24m2nd2lydhh` (`user_id`),
  CONSTRAINT `FK6qa9n55hye9mc24m2nd2lydhh` FOREIGN KEY (`user_id`) REFERENCES `user_entity` (`id`),
  CONSTRAINT `FKshfp5xelc7eyer4kuk4by5s1w` FOREIGN KEY (`test_id`) REFERENCES `test_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `result_entity`
--

LOCK TABLES `result_entity` WRITE;
/*!40000 ALTER TABLE `result_entity` DISABLE KEYS */;
/*!40000 ALTER TABLE `result_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `result_have_parts_entity`
--

DROP TABLE IF EXISTS `result_have_parts_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `result_have_parts_entity` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `accuracy` double DEFAULT NULL,
  `correct_answers` int DEFAULT NULL,
  `total_questions` int DEFAULT NULL,
  `part_id` varchar(255) NOT NULL,
  `result_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi1wo06jn71rrvx25q2tbuq93l` (`part_id`),
  KEY `FK5be2pfjofnh7s0h69aif00k7o` (`result_id`),
  CONSTRAINT `FK5be2pfjofnh7s0h69aif00k7o` FOREIGN KEY (`result_id`) REFERENCES `result_entity` (`id`),
  CONSTRAINT `FKi1wo06jn71rrvx25q2tbuq93l` FOREIGN KEY (`part_id`) REFERENCES `part_test_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `result_have_parts_entity`
--

LOCK TABLES `result_have_parts_entity` WRITE;
/*!40000 ALTER TABLE `result_have_parts_entity` DISABLE KEYS */;
/*!40000 ALTER TABLE `result_have_parts_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_entity`
--

DROP TABLE IF EXISTS `role_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_entity` (
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_entity`
--

LOCK TABLES `role_entity` WRITE;
/*!40000 ALTER TABLE `role_entity` DISABLE KEYS */;
INSERT INTO `role_entity` VALUES ('ADMIN','Default ADMIN role'),('USER','Default USER role');
/*!40000 ALTER TABLE `role_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_permission`
--

DROP TABLE IF EXISTS `role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permission` (
  `role_name` varchar(255) NOT NULL,
  `permission_name` varchar(255) NOT NULL,
  PRIMARY KEY (`role_name`,`permission_name`),
  KEY `FKh4y62y8yf04ie9k9gf27sayia` (`permission_name`),
  CONSTRAINT `FK8yuwugalfrlfrbil6x2jhicwj` FOREIGN KEY (`role_name`) REFERENCES `role_entity` (`name`),
  CONSTRAINT `FKh4y62y8yf04ie9k9gf27sayia` FOREIGN KEY (`permission_name`) REFERENCES `permission_entity` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_permission`
--

LOCK TABLES `role_permission` WRITE;
/*!40000 ALTER TABLE `role_permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_entity`
--

DROP TABLE IF EXISTS `test_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `test_entity` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `number_of_participants` bigint DEFAULT NULL,
  `slug` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `type` enum('IELTS','TOEIC') DEFAULT NULL,
  `audio_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3dxygr0wbepgjtuqondb9olnc` (`audio_id`),
  CONSTRAINT `FK3dxygr0wbepgjtuqondb9olnc` FOREIGN KEY (`audio_id`) REFERENCES `files` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_entity`
--

LOCK TABLES `test_entity` WRITE;
/*!40000 ALTER TABLE `test_entity` DISABLE KEYS */;
INSERT INTO `test_entity` VALUES ('bb6ebe70-a625-492b-bc02-bf6d64685e2f','admin','2025-10-23 12:11:08.824357','admin','2025-10-23 12:11:08.824357','Description for Test 1 Toeic','Test 1 Toeic',1234,NULL,1,'TOEIC','04fcd5dd-2c63-41c6-a37f-8e205405f67a');
/*!40000 ALTER TABLE `test_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_answer_entity`
--

DROP TABLE IF EXISTS `user_answer_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_answer_entity` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `is_correct` bit(1) DEFAULT NULL,
  `answer_id` varchar(255) NOT NULL,
  `question_id` varchar(255) NOT NULL,
  `result_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlkjdiw03qy4kcrf41a2bpphue` (`answer_id`),
  KEY `FKbu0e14irf07eknfpmyydxm12y` (`question_id`),
  KEY `FKobhy9d5dgp9wdnkmbs4mqg4o9` (`result_id`),
  CONSTRAINT `FKbu0e14irf07eknfpmyydxm12y` FOREIGN KEY (`question_id`) REFERENCES `question_test_entity` (`id`),
  CONSTRAINT `FKlkjdiw03qy4kcrf41a2bpphue` FOREIGN KEY (`answer_id`) REFERENCES `answer_entity` (`id`),
  CONSTRAINT `FKobhy9d5dgp9wdnkmbs4mqg4o9` FOREIGN KEY (`result_id`) REFERENCES `result_entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_answer_entity`
--

LOCK TABLES `user_answer_entity` WRITE;
/*!40000 ALTER TABLE `user_answer_entity` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_answer_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_entity`
--

DROP TABLE IF EXISTS `user_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_entity` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `modified_date` datetime(6) DEFAULT NULL,
  `auth_provider` enum('GOOGLE','LOCAL') DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `provider_id` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_entity`
--

LOCK TABLES `user_entity` WRITE;
/*!40000 ALTER TABLE `user_entity` DISABLE KEYS */;
INSERT INTO `user_entity` VALUES ('67da702c-331e-4945-8d22-288ff35fcbce',NULL,'2025-10-23 11:57:07.504548',NULL,'2025-10-23 11:57:07.504548','LOCAL',NULL,'admin@stydu4.com','System','Administrator','$2a$10$Q0yq2llRhSqu9CzsSWhQpefttgkPRAYyCQ37H5VQKdtA6FCXd1nt.',NULL,NULL,'admin');
/*!40000 ALTER TABLE `user_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` varchar(255) NOT NULL,
  `role_name` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`,`role_name`),
  KEY `FKcpr6e2ac8nccywstgah26vb59` (`role_name`),
  CONSTRAINT `FK6y02653x6ebhsu2plf21ard62` FOREIGN KEY (`user_id`) REFERENCES `user_entity` (`id`),
  CONSTRAINT `FKcpr6e2ac8nccywstgah26vb59` FOREIGN KEY (`role_name`) REFERENCES `role_entity` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES ('67da702c-331e-4945-8d22-288ff35fcbce','ADMIN');
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-23 16:13:05

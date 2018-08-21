DROP SCHEMA IF EXISTS `discopolord.db`;

CREATE SCHEMA `discopolord.db`;

use `discopolord.db`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `identifier` varchar(20) UNIQUE NOT NULL,
  `username` varchar(20) NOT NULL,
  `password` varchar(64) NOT NULL,
  `email` varchar(50) NOT NULL,
  PRIMARY KEY(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1;

DROP TABLE IF EXISTS `user_contacts`;

CREATE TABLE `user_contacts` (
  `user_id` int NOT NULL,
  `contact_id` int NOT NULL,
  `contact_type` TINYINT NOT NULL,
  
  PRIMARY KEY(`user_id`, `contact_id`),
  CONSTRAINT `FK_USER`
  FOREIGN KEY(`user_id`) REFERENCES `user`(`id`) 
  ON DELETE NO ACTION ON UPDATE NO ACTION,
  
  CONSTRAINT `FK_CONTACT`
  FOREIGN KEY(`contact_id`) REFERENCES `user`(`id`) 
  ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;


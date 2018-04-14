DROP SCHEMA IF EXISTS `discopolord.db`;

CREATE SCHEMA `discopolord.db`;

use `discopolord.db`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `identifier` varchar(20) UNIQUE NOT NULL,
  `username` varchar(20) NOT NULL,
  `password` varchar(20) NOT NULL,
  `email` varchar(40) NOT NULL,
  PRIMARY KEY(`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1;

DROP TABLE IF EXISTS `user_connection`;

CREATE TABLE `user_relations` (
  `user1_id` int NOT NULL,
  `user2_id` int NOT NULL,
  `relation_type` TINYINT NOT NULL,
  
  PRIMARY KEY(`user1_id`, `user2_id`),
  CONSTRAINT `FK_USER1`
  FOREIGN KEY(`user1_id`) REFERENCES `user`(`id`) 
  ON DELETE NO ACTION ON UPDATE NO ACTION,
  
  CONSTRAINT `FK_USER2`
  FOREIGN KEY(`user2_id`) REFERENCES `user`(`id`) 
  ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;


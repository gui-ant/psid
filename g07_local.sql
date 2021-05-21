-- phpMyAdmin SQL Dump
-- version 5.1.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 21-Maio-2021 às 21:46
-- Versão do servidor: 10.4.18-MariaDB
-- versão do PHP: 8.0.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `g07_local`
--
CREATE DATABASE IF NOT EXISTS `g07_local` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `g07_local`;

DELIMITER $$
--
-- Procedimentos
--
DROP PROCEDURE IF EXISTS `spAddUserToCulture`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spAddUserToCulture` (IN `p_culture_id` INT(11), IN `p_user_id` INT(11))  NO SQL
    SQL SECURITY INVOKER
BEGIN

INSERT INTO culture_users (culture_id, user_id) 
SELECT p_culture_id, p_user_id
WHERE 
	(SELECT c.manager_id FROM cultures c, culture_users AS cu WHERE c.id = p_culture_id) <> p_user_id 
	OR 
	(SELECT COUNT(*) FROM culture_users cu WHERE cu.culture_id=p_culture_id AND cu.user_id=p_user_id) = 0
;
END$$

DROP PROCEDURE IF EXISTS `spCreateCulture`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateCulture` (IN `p_name` INT, IN `p_zone_id` INT, IN `p_manager_id` INT)  SQL SECURITY INVOKER
BEGIN
IF NOT isResearcher() THEN 
	SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = "Only researcher's can create cultures";
END IF;

INSERT INTO cultures (name, zone_id, p_manager_id) VALUES (p_name, p_zone_id, p_manager_id);

END$$

DROP PROCEDURE IF EXISTS `spCreateCultureParam`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateCultureParam` (IN `p_sensor_type` VARCHAR(64), IN `p_valmax` INT(11), IN `p_valmin` INT(11), IN `p_tolerance` INT(11), IN `p_set_id` INT, OUT `out_param_id` INT(11))  BEGIN 

SET @culture_id:=0;
SET @mysql_user:="";


SELECT culture_id INTO @culture_id 
FROM  culture_params_sets
WHERE id = p_set_id; 

IF isManager(@culture_id) && p_valmax > p_valmin && p_tolerance >= 0 THEN

	
	SELECT CONCAT("'",getUserInfo('name'),"'@'",getUserInfo('host'),"'") INTO @mysql_user;
	
	
	SET @qry = CONCAT("GRANT INSERT ON culture_params TO ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
	INSERT INTO culture_params (sensor_type, valmax, valmin, tolerance) VALUES (p_sensor_type, p_valmax, p_valmin, p_tolerance);
	SET out_param_id = LAST_INSERT_ID();
	
	
	SET @qry = CONCAT("REVOKE INSERT ON culture_params FROM ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
	CALL spCreateRelCultureParamsSet(p_set_id, out_param_id);
	
END IF;
END$$

DROP PROCEDURE IF EXISTS `spCreateCultureParamsSet`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateCultureParamsSet` (IN `p_culture_id` INT(11), OUT `out_set_id` INT)  BEGIN
SET @mysql_user:="";

IF isManager(p_culture_id) THEN
	
	
	SELECT CONCAT("'",getUserInfo('name'),"'@'",getUserInfo('host'),"'") INTO @mysql_user;
	
	
	SET @qry = CONCAT("GRANT INSERT ON culture_params_sets TO ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
	INSERT INTO culture_params_sets (culture_id) VALUES (p_culture_id);
	SET out_set_id = LAST_INSERT_ID();
	
		
	SET @qry = CONCAT("REVOKE INSERT ON culture_params_sets FROM ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
END IF;
END$$

DROP PROCEDURE IF EXISTS `spCreateRelCultureParamsSet`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateRelCultureParamsSet` (IN `p_set_id` INT, IN `p_param_id` INT)  BEGIN 
SET @culture_id:=0;
SET @mysql_user:="";

SELECT culture_id INTO @culture_id 
FROM  culture_params_sets
WHERE id = p_set_id; 

IF isManager(@culture_id) THEN

	
	SELECT CONCAT("'",getUserInfo('name'),"'@'",getUserInfo('host'),"'") INTO @mysql_user;
	
	
	SET @qry = CONCAT("GRANT INSERT ON rel_culture_params_set TO ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
	INSERT INTO rel_culture_params_set (set_id, culture_param_id) VALUES (p_set_id, p_param_id);
	
			
	SET @qry = CONCAT("REVOKE INSERT ON rel_culture_params_set FROM ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
END IF;

END$$

DROP PROCEDURE IF EXISTS `spCreateUser`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateUser` (IN `p_email` VARCHAR(50) CHARSET latin1, IN `p_name` VARCHAR(100) CHARSET latin1, IN `p_pass` VARCHAR(64) CHARSET latin1, IN `p_role` ENUM('admin','researcher','technician') CHARSET latin1, OUT `out_user_id` INT)  BEGIN
IF NOT isEmail(p_email) THEN
	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "The email is invalid.";
END IF;

SET p_pass := CONCAT("'", p_pass, "'");

SET @p_role_group := CONCAT("'group_", p_role, "'");
SET @mysqluser := CONCAT("'", p_email,"'");


SET @qry := CONCAT('CREATE USER IF NOT EXISTS ', @mysqluser, '  IDENTIFIED BY ', p_pass);
PREPARE stmt FROM @qry; EXECUTE stmt;

SET @qry := CONCAT('GRANT ', @p_role_group,' TO ', @mysqluser);
PREPARE stmt FROM @qry; EXECUTE stmt;


SET @qry := CONCAT('SET DEFAULT ROLE ', @p_role_group,' FOR ', @mysqluser);
PREPARE stmt FROM @qry; EXECUTE stmt;

INSERT INTO users (username, email) VALUES (p_name, p_email) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), username=p_name;

SET out_user_id = LAST_INSERT_ID();

SELECT CONCAT("User created (id: ", out_user_id,")") as success;

DEALLOCATE PREPARE stmt;
FLUSH PRIVILEGES;

END$$

DROP PROCEDURE IF EXISTS `spDeleteCulture`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spDeleteCulture` (IN `p_culture_id` INT)  BEGIN
CALL spStopIfNotManager(p_culture_id);
DELETE FROM cultures WHERE id=p_culture_id;
END$$

DROP PROCEDURE IF EXISTS `spDeleteParam`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spDeleteParam` (IN `p_param_id` INT)  BEGIN
SET @culture_id:=-1;

SELECT sets.culture_id INTO @culture_id 
FROM culture_params params
JOIN rel_culture_params_set rels ON params.id = rels.culture_param_id
JOIN culture_params_sets sets ON sets.id = rels.set_id
WHERE params.id = p_param_id; 

CALL spStopIfNotManager(@culture_id);

IF @culture_id > 0 THEN
	DELETE FROM culture_params WHERE id=p_param_id;
END IF;

END$$

DROP PROCEDURE IF EXISTS `spDeleteUser`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spDeleteUser` (IN `p_user_id` INT(10))  NO SQL
IF getUserInfo('role') = 'group_admin' THEN
	DELETE from users WHERE id = p_user_id;
END IF$$

DROP PROCEDURE IF EXISTS `spExportCulturemeasurestoCSV`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spExportCulturemeasurestoCSV` ()  SELECT 1$$

DROP PROCEDURE IF EXISTS `spGetCultureById`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spGetCultureById` (IN `p_culture_id` INT)  NO SQL
BEGIN
SELECT c.*
FROM cultures c
LEFT JOIN culture_users cu on cu.culture_id = c.id 
WHERE c.id = p_culture_id;
END$$

DROP PROCEDURE IF EXISTS `spGetCultureParams`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spGetCultureParams` (IN `p_culture_id` INT)  NO SQL
SELECT
	params.id as id,
    sets.id as set_id,
    sensor_type,
    valmax,
    valmin,
    tolerance
FROM
    rel_culture_params_set rel
JOIN culture_params params
ON
    params.id = rel.culture_param_id
JOIN culture_params_sets sets
ON
    sets.id = rel.set_id
WHERE
    sets.culture_id = p_culture_id$$

DROP PROCEDURE IF EXISTS `spGetCulturesByUserId`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spGetCulturesByUserId` (IN `p_user_id` INT(11))  NO SQL
BEGIN
SELECT c.*
FROM cultures c
LEFT JOIN culture_users cu on cu.culture_id = c.id 
WHERE cu.user_id = p_user_id or c.manager_id = p_user_id;
END$$

DROP PROCEDURE IF EXISTS `spSetCultureManager`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spSetCultureManager` (IN `p_culture_id` INT, IN `p_user_id` INT)  BEGIN
UPDATE cultures c SET c.manager_id=p_user_id WHERE c.id=p_culture_id;
END$$

DROP PROCEDURE IF EXISTS `spStopIfNotManager`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spStopIfNotManager` (IN `p_culture_id` INT)  BEGIN
IF NOT isManager(p_culture_id) THEN
	SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = "You do not have permission to change this culture";
END IF;
END$$

DROP PROCEDURE IF EXISTS `spUpdateCultureName`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spUpdateCultureName` (IN `p_culture_id` INT, IN `p_new_name` VARCHAR(50))  BEGIN
CALL spStopIfNotManager(p_culture_id);
UPDATE cultures c SET c.name=p_new_name WHERE c.id=p_culture_id;
END$$

DROP PROCEDURE IF EXISTS `spUpdateUser`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `spUpdateUser` (IN `p_email` VARCHAR(50), IN `p_name` VARCHAR(50), IN `p_pass` VARCHAR(50))  BEGIN
DECLARE user_id INT;
DECLARE role VARCHAR(50);
DECLARE err VARCHAR(64);

SET user_id=0;
SET role='';
SET err='';

IF p_email='' THEN
	SET err='Argument #1(p_email) cannot be empty.';
END IF;

SELECT 'The user email doenst exists.' INTO err WHERE err='' AND (SELECT COUNT(*) FROM users u WHERE u.email=p_email) = 0;

IF err<>'' THEN 
	SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = err;
END IF;
SET err ='';

SELECT u.id INTO user_id FROM users u WHERE u.email=p_email;

IF p_name='' THEN
	SELECT (SELECT u.username FROM users u WHERE u.id=user_id) INTO p_name;
END IF;

SET p_email=CONCAT("'",p_email ,"'@'%'");
SET @qry:=CONCAT("ALTER USER ", p_email, " IDENTIFIED BY '",p_pass,"'");
PREPARE stmt FROM @qry;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Name already exists.' INTO err WHERE err='' AND (SELECT COUNT(*) FROM users u WHERE u.username=p_name AND u.id<>user_id) > 0 ;
IF err<>'' THEN 
	SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = err;
END IF;

UPDATE users u SET u.username=p_name WHERE u.id=user_id;
END$$

--
-- Funções
--
DROP FUNCTION IF EXISTS `checkPrevAlert`$$
CREATE DEFINER=`root`@`localhost` FUNCTION `checkPrevAlert` (`p_mins` INT, `p_rule_set_id` INT, `p_sensor_id` INT, `p_param_id` INT) RETURNS TINYINT(1) 
RETURN EXISTS ( 
SELECT * 
FROM alerts 
WHERE ((sensor_id = p_sensor_id)
  OR ( parameter_set_id = p_rule_set_id)
  OR (param_id = p_param_id) )
  AND created_at >= NOW() - INTERVAL p_mins MINUTE
)
$$

DROP FUNCTION IF EXISTS `getUserInfo`$$
CREATE DEFINER=`root`@`localhost` FUNCTION `getUserInfo` (`p_property` ENUM('name','host','role')) RETURNS VARCHAR(50) CHARSET latin1 BEGIN

	DECLARE res VARCHAR(50); 
	DECLARE rev_username VARCHAR(64);
	DECLARE at_sign_pos INT;
	DECLARE username VARCHAR(64);
	DECLARE is_manager INT;
	
	SET res='';
	SET rev_username = REVERSE(USER());
	SET at_sign_pos = LOCATE("@", rev_username);
	SET username = REVERSE(REPLACE(rev_username, LEFT(rev_username,at_sign_pos),""));
	SET is_manager = 0;
	
	SELECT (
		CASE
			WHEN p_property = 'name' THEN u.User
			WHEN p_property = 'host' THEN u.Host
			WHEN p_property = 'role' THEN u.default_role
		END) INTO res 
	FROM mysql.user u WHERE u.User=username;
	
	RETURN res;
	
END$$

DROP FUNCTION IF EXISTS `isEmail`$$
CREATE DEFINER=`root`@`localhost` FUNCTION `isEmail` (`p_email` VARCHAR(50)) RETURNS TINYINT(4) BEGIN
SET p_email = CONCAT("'",p_email,"'");
RETURN (SELECT p_email REGEXP '^[^@]+@[^@]+\.[^@]{2,}$')=1;
END$$

DROP FUNCTION IF EXISTS `isManager`$$
CREATE DEFINER=`root`@`localhost` FUNCTION `isManager` (`p_culture_id` INT) RETURNS VARCHAR(64) CHARSET utf8mb4 SQL SECURITY INVOKER
    COMMENT 'Verifica se o user com sessão ativa (invoker) é responsável pela cultura dada'
BEGIN

DECLARE is_manager INT;
SET is_manager = 0;

SELECT COUNT(*) INTO is_manager 
FROM users u 
JOIN cultures c ON c.manager_id = u.id 
WHERE u.email=getUserInfo('name') AND c.id=p_culture_id; 

RETURN is_manager;
 
END$$

DROP FUNCTION IF EXISTS `isResearcher`$$
CREATE DEFINER=`root`@`localhost` FUNCTION `isResearcher` () RETURNS VARCHAR(50) CHARSET utf8mb4 BEGIN 
	DECLARE res VARCHAR(50);
	
	SELECT getUserInfo('role') INTO res;
	
	RETURN res="group_researcher";
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `alerts`
--

DROP TABLE IF EXISTS `alerts`;
CREATE TABLE IF NOT EXISTS `alerts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parameter_set_id` int(11) DEFAULT NULL COMMENT 'id do set de parametros: utilizado por alerta de parametrizacao de cultura',
  `sensor_id` int(11) DEFAULT NULL COMMENT 'id do sensor. utilizar por alerta de manutencao',
  `param_id` int(11) DEFAULT NULL COMMENT 'id do parametro simples. utilizado por alerta de previsao',
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `message` varchar(300) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Acionadores `alerts`
--
DROP TRIGGER IF EXISTS `existsPrevAlert`;
DELIMITER $$
CREATE TRIGGER `existsPrevAlert` BEFORE INSERT ON `alerts` FOR EACH ROW IF checkPrevAlert(5, NEW.parameter_set_id, NEW.sensor_id, NEW.param_id) THEN
SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = 'A similar previous alert already exists in past 5 minute(s)';
END IF
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `cultures`
--

DROP TABLE IF EXISTS `cultures`;
CREATE TABLE IF NOT EXISTS `cultures` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `zone_id` int(11) NOT NULL,
  `manager_id` int(1) NOT NULL,
  `state` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `culture_zone` (`zone_id`),
  KEY `culture_manager` (`manager_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `culture_params`
--

DROP TABLE IF EXISTS `culture_params`;
CREATE TABLE IF NOT EXISTS `culture_params` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sensor_type` varchar(1) NOT NULL,
  `valmax` double(5,2) NOT NULL,
  `valmin` double(5,2) NOT NULL,
  `tolerance` double(5,2) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `culture_params_sets`
--

DROP TABLE IF EXISTS `culture_params_sets`;
CREATE TABLE IF NOT EXISTS `culture_params_sets` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `culture_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `culture_id` (`culture_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `culture_users`
--

DROP TABLE IF EXISTS `culture_users`;
CREATE TABLE IF NOT EXISTS `culture_users` (
  `culture_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`culture_id`,`user_id`),
  KEY `culture` (`culture_id`),
  KEY `culture_users_ibfk_1` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `measurements`
--

DROP TABLE IF EXISTS `measurements`;
CREATE TABLE IF NOT EXISTS `measurements` (
  `id` varchar(32) NOT NULL,
  `value` double DEFAULT NULL,
  `sensor_id` int(11) NOT NULL,
  `zone_id` int(11) NOT NULL,
  `date` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_correct` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sensure_measure` (`sensor_id`),
  KEY `sensor_zone` (`zone_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `rel_culture_params_set`
--

DROP TABLE IF EXISTS `rel_culture_params_set`;
CREATE TABLE IF NOT EXISTS `rel_culture_params_set` (
  `set_id` int(11) NOT NULL,
  `culture_param_id` int(11) NOT NULL,
  PRIMARY KEY (`set_id`,`culture_param_id`),
  KEY `culture_param_id` (`culture_param_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `email` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Restrições para despejos de tabelas
--

--
-- Limitadores para a tabela `cultures`
--
ALTER TABLE `cultures`
  ADD CONSTRAINT `culture_manager` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`);

--
-- Limitadores para a tabela `culture_params_sets`
--
ALTER TABLE `culture_params_sets`
  ADD CONSTRAINT `culture_params_sets_ibfk_1` FOREIGN KEY (`culture_id`) REFERENCES `cultures` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limitadores para a tabela `culture_users`
--
ALTER TABLE `culture_users`
  ADD CONSTRAINT `culture` FOREIGN KEY (`culture_id`) REFERENCES `cultures` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `culture_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limitadores para a tabela `rel_culture_params_set`
--
ALTER TABLE `rel_culture_params_set`
  ADD CONSTRAINT `rel_culture_params_set_ibfk_1` FOREIGN KEY (`culture_param_id`) REFERENCES `culture_params` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `rel_culture_params_set_ibfk_2` FOREIGN KEY (`set_id`) REFERENCES `culture_params_sets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

DELIMITER $$
--
-- Eventos
--
DROP EVENT IF EXISTS `LimpezaDados`$$
CREATE DEFINER=`root`@`localhost` EVENT `LimpezaDados` ON SCHEDULE EVERY 1 DAY STARTS '2021-05-03 22:54:35' ON COMPLETION NOT PRESERVE ENABLE DO BEGIN
SET @dia_anterior = CURRENT_DATE - INTERVAL 1 DAY;
INSERT INTO measurements (id, value, sensor_id, zone_id, date, is_correct)
SELECT 	Concat( DATE_FORMAT(date, "%Y-%m-%d_%H"), '_Z', zone_id , '_S', sensor_id ) AS id , 
		AVG(value) AS value, 
		sensor_id , 
		zone_id , 
		0, 
		is_correct
FROM measurements
WHERE date > @dia_anterior 
  AND date < CURRENT_DATE
  AND is_correct = 1
GROUP BY HOUR(date) , sensor_id , zone_id;

DELETE FROM measurements
WHERE date > @dia_anterior 
  AND date < CURRENT_DATE
  AND is_correct = 1;
END$$

DELIMITER ;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

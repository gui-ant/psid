-- phpMyAdmin SQL Dump
-- version 5.1.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 02-Maio-2021 às 18:15
-- Versão do servidor: 10.4.18-MariaDB
-- versão do PHP: 7.4.16

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

DELIMITER $$
--
-- Procedimentos
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `spAddUsersToCultures` (IN `p_culture_id` INT(11), IN `p_user_id` INT(11))  NO SQL
    SQL SECURITY INVOKER
BEGIN
IF CURRENT_ROLE() ='group_admin' THEN
	INSERT INTO culture_users (culture_id, user_id) VALUES (p_culture_id, p_user_id);
END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateCulture` (IN `p_name` INT, IN `p_zone_id` INT, IN `p_manager_id` INT)  SQL SECURITY INVOKER
BEGIN
IF NOT isResearcher() THEN 
	SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = "Only researcher's can create cultures";
END IF;
INSERT INTO cultures (`name`,`zone_id`,`p_manager_id`) VALUES (p_name,p_zone_id,p_manager_id);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateCultureParam` (IN `p_user_id` INT(11), IN `p_sensor_type` VARCHAR(64), IN `p_valmax` INT(11), IN `p_valmin` INT(11), IN `p_tolerance` INT(11), INOUT `io_set_id` INT, INOUT `io_param_id` INT(11))  BEGIN 

SET @culture_id:=0;
SET @user_email:="";
SET @mysql_user:="";

/* DETERMINA A CULTURE_ID ATRAVÉS DO SET_ID */
SELECT culture_id INTO @culture_id 
FROM  culture_params_sets
WHERE id = io_set_id; 

IF isManager(@culture_id) && p_valmax > p_valmin && p_tolerance >= 0 THEN

	SELECT email INTO @user_email FROM users WHERE id = p_user_id;
	SELECT CONCAT("'",User,"'@'",HOST,"'") INTO @mysql_user from mysql.user WHERE USER = @user_email;
	
	/* ELEVA PRIVILÉGIOS DE ESCRITA */
	SET @qry = CONCAT("GRANT INSERT ON g07_local.culture_params TO ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
	INSERT INTO culture_params (sensor_type, valmax, valmin, tolerance) VALUES (p_sensor_type, p_valmax, p_valmin, p_tolerance);
	SET io_param_id = LAST_INSERT_ID();
	
	/* ELIMINA PRIVILÉGIOS DE ESCRITA */
	SET @qry = CONCAT("REVOKE INSERT ON g07_local.culture_params FROM ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
	CALL spCreateRelCultureParamsSet(p_user_id, io_set_id, io_param_id);
	
END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateCultureParamsSet` (IN `p_user_id` INT(11), IN `p_culture_id` INT(11), INOUT `out_set_id` INT)  BEGIN
SET @user_email:="";
SET @mysql_user:="";

IF isManager(p_culture_id) THEN

	SELECT email INTO @user_email FROM users WHERE id = p_user_id;
	SELECT CONCAT("'",User,"'@'",HOST,"'") INTO @mysql_user from mysql.user WHERE USER = @user_email;
	
	/* ELEVA PRIVILÉGIOS DE ESCRITA */
	SET @qry = CONCAT("GRANT INSERT ON g07_local.culture_params_sets TO ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
	INSERT INTO culture_params_sets (culture_id) VALUES (p_culture_id);
	SET out_set_id = LAST_INSERT_ID();
	
		/* ELIMINA PRIVILÉGIOS DE ESCRITA */
	SET @qry = CONCAT("REVOKE INSERT ON g07_local.culture_params_sets FROM ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateRelCultureParamsSet` (IN `p_user_id` INT, IN `p_set_id` INT, IN `p_param_id` INT)  BEGIN 
SET @culture_id:=0;
SET @user_email:="";
SET @mysql_user:="";

SELECT culture_id INTO @culture_id 
FROM  culture_params_sets
WHERE id = p_set_id; 

IF isManager(@culture_id) THEN

	SELECT email INTO @user_email FROM users WHERE id = p_user_id;
	SELECT CONCAT("'",User,"'@'",HOST,"'") INTO @mysql_user from mysql.user WHERE USER = @user_email;
	
	/* ELEVA PRIVILÉGIOS DE ESCRITA */
	SET @qry = CONCAT("GRANT INSERT ON g07_local.rel_culture_params_set TO ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
	INSERT INTO rel_culture_params_set (set_id, culture_param_id) VALUES (p_set_id, p_param_id);
	
			/* ELIMINA PRIVILÉGIOS DE ESCRITA */
	SET @qry = CONCAT("REVOKE INSERT ON g07_local.rel_culture_params_set FROM ", @mysql_user);
	PREPARE stmt FROM @qry;	EXECUTE stmt;
	
END IF;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateUser` (IN `p_email` VARCHAR(50) CHARSET latin1, IN `p_name` VARCHAR(100) CHARSET latin1, IN `p_pass` VARCHAR(64) CHARSET latin1, IN `p_role` ENUM('admin','researcher','technician') CHARSET latin1, OUT `out_user_id` INT)  BEGIN
IF NOT isEmail(p_email) THEN
	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "The email is invalid.";
END IF;

SET p_pass := CONCAT("'", p_pass, "'");

SET @p_role_group := CONCAT("'group_", p_role, "'");
SET @mysqluser := CONCAT("'", p_email,"'");


SET @sql := CONCAT('CREATE USER ', @mysqluser, ' IDENTIFIED BY ', p_pass);
PREPARE stmt FROM @sql;
EXECUTE stmt;


SET @sql := CONCAT('GRANT ', @p_role_group,' TO ', @mysqluser);
PREPARE stmt FROM @sql;
EXECUTE stmt;


SET @sql := CONCAT('SET DEFAULT ROLE ', @p_role_group,' FOR ', @mysqluser);
PREPARE stmt FROM @sql;
EXECUTE stmt;


INSERT INTO users (username,email) VALUES (p_name, p_email);

SET out_user_id = LAST_INSERT_ID(); COMMIT;

SELECT CONCAT("User created (id: ", @inserted_id,")") as SUCCESS;

DEALLOCATE PREPARE stmt;
FLUSH PRIVILEGES;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spDeleteCulture` (IN `p_culture_id` INT)  BEGIN
CALL spStopIfNotManager(p_culture_id);
DELETE FROM cultures WHERE id=p_culture_id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spDeleteParam` (IN `p_param_id` INT)  BEGIN
SET @culture_id:=-1;

SELECT sets.culture_id INTO @culture_id 
FROM culture_params AS params
JOIN rel_culture_params_set as rels ON params.id = rels.culture_param_id
JOIN culture_params_sets as sets ON sets.id = rels.set_id
WHERE params.id = p_param_id; 

CALL spStopIfNotManager(@culture_id);

IF @culture_id > 0 THEN
	DELETE FROM culture_params WHERE id=p_param_id;
END IF;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spDeleteUser` (IN `p_user_id` INT(10))  NO SQL
DELETE from users WHERE id = p_user_id$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spGetCultureById` (IN `CULTURE_ID` INT)  NO SQL
BEGIN
	SELECT c.*
	FROM cultures AS c
	LEFT JOIN culture_users AS cu on cu.culture_id = c.id 
	WHERE c.id = CULTURE_ID;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spGetCulturesByUserId` (IN `USER_ID` INT(11))  NO SQL
BEGIN
	SELECT c.*
	FROM cultures AS c
	LEFT JOIN culture_users AS cu on cu.culture_id = c.id 
	WHERE cu.user_id = USER_ID or c.manager_id = USER_ID;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spStopIfNotManager` (IN `p_culture_id` INT)  BEGIN
IF NOT isManager(p_culture_id) THEN
	SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = "You do not have permission to change this culture";
END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `spUpdateCultureName` (IN `p_culture_id` INT, IN `p_new_name` VARCHAR(50))  BEGIN
CALL spStopIfNotManager(p_culture_id);
UPDATE cultures AS c SET c.name=p_new_name WHERE c.id=p_culture_id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `test_spGetCultureParams` (IN `p_culture_id` INT)  NO SQL
    COMMENT 'Permite ver parâmetros de uma cultura. Só para testes!'
SELECT
	sets.id as id,
    sensor_type,
    valmax,
    valmin,
    tolerance
FROM
    rel_culture_params_set AS rel
JOIN culture_params AS params
ON
    params.id = rel.culture_param_id
JOIN culture_params_sets AS sets
ON
    sets.id = rel.set_id
WHERE
    sets.culture_id = p_culture_id$$

--
-- Funções
--
CREATE DEFINER=`root`@`localhost` FUNCTION `checkPrevAlert` (`p_rule_set_id` INT, `p_mins` INT) RETURNS TINYINT(1) RETURN EXISTS ( 
SELECT * 
FROM alerts 
WHERE parameter_set_id = p_rule_set_id 
AND
created_at >= NOW()- INTERVAL p_mins MINUTE 
)$$

CREATE DEFINER=`root`@`localhost` FUNCTION `isEmail` (`p_email` VARCHAR(50)) RETURNS TINYINT(4) BEGIN
SET p_email = CONCAT("'",p_email,"'");
RETURN (SELECT p_email REGEXP '^[^@]+@[^@]+\.[^@]{2,}$')=1;
END$$

CREATE DEFINER=`root`@`localhost` FUNCTION `isManager` (`p_culture_id` INT) RETURNS VARCHAR(64) CHARSET utf8mb4 SQL SECURITY INVOKER
    COMMENT 'Verifica se o user com sessão ativa (invoker) é responsável pela cultura dada'
BEGIN

DECLARE rev_username VARCHAR(64);
DECLARE at_sign_pos INT;
DECLARE username VARCHAR(64);
DECLARE is_manager INT;

SET rev_username = REVERSE(USER());
SET at_sign_pos = LOCATE("@", rev_username);
SET username = REVERSE(REPLACE(rev_username, LEFT(rev_username,at_sign_pos),""));
SET is_manager = 0;

SELECT COUNT(*) INTO is_manager 
FROM users AS u 
JOIN cultures AS c ON c.manager_id = u.id 
WHERE u.email=username AND c.id=p_culture_id; 

RETURN is_manager;
 
END$$

CREATE DEFINER=`root`@`localhost` FUNCTION `isResearcher` () RETURNS VARCHAR(50) CHARSET utf8mb4 BEGIN 
	DECLARE role VARCHAR(50);
	SET role="";
	SELECT default_role INTO role FROM mysql.user AS u WHERE u.User=REPLACE(USER(),CONCAT("@",CONVERT(u.Host,CHAR)),"");
	RETURN role="group_researcher";
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `alerts`
--

CREATE TABLE `alerts` (
  `id` int(11) NOT NULL,
  `parameter_set_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `mensagem` varchar(150) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Acionadores `alerts`
--
DELIMITER $$
CREATE TRIGGER `existsPrevAlert` BEFORE INSERT ON `alerts` FOR EACH ROW IF checkPrevAlert(NEW.parameter_set_id, 5) THEN
SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = 'A similar previous alert already exists in past 5 minute(s)';
END IF
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estrutura da tabela `cultures`
--

CREATE TABLE `cultures` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `zone_id` int(11) NOT NULL,
  `manager_id` int(1) NOT NULL,
  `state` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `culture_params`
--

CREATE TABLE `culture_params` (
  `id` int(11) NOT NULL,
  `sensor_type` varchar(1) NOT NULL,
  `valmax` double(5,2) NOT NULL,
  `valmin` double(5,2) NOT NULL,
  `tolerance` double(5,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `culture_params_sets`
--

CREATE TABLE `culture_params_sets` (
  `id` int(11) NOT NULL,
  `culture_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `culture_users`
--

CREATE TABLE `culture_users` (
  `culture_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `measurements`
--

CREATE TABLE `measurements` (
  `id` varchar(32) NOT NULL,
  `value` double DEFAULT NULL,
  `sensor_id` int(11) NOT NULL,
  `zone_id` int(11) NOT NULL,
  `date` timestamp NOT NULL DEFAULT current_timestamp(),
  `isCorrect` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `rel_culture_params_set`
--

CREATE TABLE `rel_culture_params_set` (
  `set_id` int(11) NOT NULL,
  `culture_param_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estrutura da tabela `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Extraindo dados da tabela `users`
--

INSERT INTO `users` (`id`, `username`, `email`) VALUES
(81, 'Res1', 'res1@foo.bar'),
(82, 'Tech1', 'tech1@foo.bar');

--
-- Índices para tabelas despejadas
--

--
-- Índices para tabela `alerts`
--
ALTER TABLE `alerts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `parameter_alert` (`parameter_set_id`);

--
-- Índices para tabela `cultures`
--
ALTER TABLE `cultures`
  ADD PRIMARY KEY (`id`),
  ADD KEY `culture_zone` (`zone_id`),
  ADD KEY `culture_manager` (`manager_id`);

--
-- Índices para tabela `culture_params`
--
ALTER TABLE `culture_params`
  ADD PRIMARY KEY (`id`);

--
-- Índices para tabela `culture_params_sets`
--
ALTER TABLE `culture_params_sets`
  ADD PRIMARY KEY (`id`),
  ADD KEY `culture_id` (`culture_id`);

--
-- Índices para tabela `culture_users`
--
ALTER TABLE `culture_users`
  ADD PRIMARY KEY (`culture_id`,`user_id`),
  ADD KEY `culture` (`culture_id`),
  ADD KEY `culture_users_ibfk_1` (`user_id`);

--
-- Índices para tabela `measurements`
--
ALTER TABLE `measurements`
  ADD PRIMARY KEY (`id`),
  ADD KEY `sensure_measure` (`sensor_id`),
  ADD KEY `sensor_zone` (`zone_id`);

--
-- Índices para tabela `rel_culture_params_set`
--
ALTER TABLE `rel_culture_params_set`
  ADD PRIMARY KEY (`set_id`,`culture_param_id`),
  ADD KEY `culture_param_id` (`culture_param_id`);

--
-- Índices para tabela `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT de tabelas despejadas
--

--
-- AUTO_INCREMENT de tabela `alerts`
--
ALTER TABLE `alerts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `cultures`
--
ALTER TABLE `cultures`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de tabela `culture_params`
--
ALTER TABLE `culture_params`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT de tabela `culture_params_sets`
--
ALTER TABLE `culture_params_sets`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT de tabela `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=83;

--
-- Restrições para despejos de tabelas
--

--
-- Limitadores para a tabela `alerts`
--
ALTER TABLE `alerts`
  ADD CONSTRAINT `parameter_alert` FOREIGN KEY (`parameter_set_id`) REFERENCES `culture_params_sets` (`id`);

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
  ADD CONSTRAINT `culture_users_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Limitadores para a tabela `rel_culture_params_set`
--
ALTER TABLE `rel_culture_params_set`
  ADD CONSTRAINT `rel_culture_params_set_ibfk_1` FOREIGN KEY (`culture_param_id`) REFERENCES `culture_params` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `rel_culture_params_set_ibfk_2` FOREIGN KEY (`set_id`) REFERENCES `culture_params_sets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

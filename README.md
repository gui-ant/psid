## Grupo 7
Link ZOOM Slot 5: https://videoconf-colibri.zoom.us/j/87585381703

**Drive**:<br/>
- [Relatório](https://docs.google.com/document/d/1F14r7k54XJ3Kmzq6IZxJsG_Xur3vkzZY/edit)<br/>
- [Relatório Grupo02](https://docs.google.com/document/d/1SCfdpyMIYwfB00AgGP9rdt_9Ycls6vsEastxUZjk_HM/edit?usp=sharing)
- [Diário](https://docs.google.com/spreadsheets/d/1HMAvvbRs9QXDj8qZwiOb9Uf7KmsjCt36/edit)<br/>
- [Croqui Laboratório](https://docs.google.com/document/d/1Lv8bhDtPm4bYxZKTBfCdPttEHuGRpBRA/edit)<br/>
- [Perguntas para reuniões](https://docs.google.com/document/d/1m1g19S2wEBp_5jOAlmTetTr329ICJ58XwlmQ7cQJcI4/edit?usp=sharing)<br/>

**Java**:
1. Abrir o projeto no Intellij (só pasta java)
2. Abrir Project Settings (F4) -> Libraries
3. Adicionar os *.jar [dbtools](https://drive.google.com/drive/folders/1EONx7NXCGDmnfU55PpnrQfEw2xk_ei0T?usp=sharing) ([Blackboard](https://e-learning.iscte-iul.pt/webapps/blackboard/content/listContent.jsp?course_id=_13125_1&content_id=_120562_1))

**Mongo Atlas**: https://cloud.mongodb.com/ <br/> 
 - Sign in with google account (user: sid2021g07, pass: sid2021!)

**PHP/MySQL**:<br/>
1. Iniciar XAMPP
2. Iniciar servidor Apache
3. Aceder a [localhost/psid/php](http://localhost/psid/php) no browser (considerando que o projeto se encontra na pasta c:\xampp\htdocs. Podem também fazer um clone do rep para essa pasta)
4. Login:
    * **Admin** -> **user**: sid2021g07\@gmail<span>.</span>com **pass**: sid2021!
    * **Investigador** -> **user**: gfaas1@iscte.pt (ou o vosso) **pass**: asd
    * **Técnico de Manutenção** -> **user**: pajo@iscte<span>.</span>pt **pass**: often

[phpMyAdmin](http://194.210.86.10/phpmyadmin/db_structure.php?server=1&db=aluno_g07) (user: aluno, pass: aluno)

- Criação de Roles no MySQL de acordo com a especificação
```mysql

use g07_local;
CREATE ROLE 'group_admin';
GRANT CREATE USER ON *.* TO `group_admin`;
GRANT GRANT OPTION ON *.* TO 'group_admin';
GRANT SELECT,INSERT,UPDATE,DELETE ON g07_local.users TO 'group_admin';
GRANT SELECT,INSERT,UPDATE,DELETE ON g07_local.cultures TO 'group_admin';
GRANT SELECT,INSERT,UPDATE,DELETE ON g07_local.culture_users TO 'group_admin';
GRANT SELECT ON g07_local.measurements TO 'group_admin';
GRANT SELECT ON g07_local.alerts TO 'group_admin';

GRANT EXECUTE ON PROCEDURE g07_local.spCreateUser TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spCreateRole TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spDeleteUser TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spUpdateUser TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spGetUserById TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spGetUserByRoleId TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spAddUserToCultures TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spGetCultureById TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spGetCulturesByUserId TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spCreateCulture TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spDeleteCulture TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spUpdateCultureName TO 'group_admin';

CREATE ROLE 'group_researcher';
GRANT SELECT ON g07_local.* TO 'group_researcher';

GRANT EXECUTE ON PROCEDURE g07_local.spGetCultureById TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spGetCulturesByUserId TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spUpdateCultureName TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spCreateCultureParam TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spCreateRelCultureParamsSet TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spCreateCultureParamsSet TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spDeleteParam TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spExportCultureMeasuresToCSV TO 'group_researcher';
GRANT EXECUTE ON FUNCTION g07_local.isManager TO 'group_researcher';
GRANT EXECUTE ON FUNCTION g07_local.isResearcher TO 'group_researcher';

CREATE ROLE 'group_technician';
GRANT SELECT ON g07_local.users TO 'group_technician';
GRANT SELECT ON g07_local.alerts TO 'group_technician';

FLUSH PRIVILEGES;

SET @inserted_id=-1;

SET @p0='admin1@foo.bar'; SET @p1='Admin1'; SET @p2='pass'; SET @p3='admin';  
CALL g07_local.spCreateUser(@p0, @p1, @p2, @p3,@inserted_id);

SET @p0='res1@foo.bar'; SET @p1='Res1'; SET @p2='pass'; SET @p3='researcher'; 
CALL g07_local.spCreateUser(@p0, @p1, @p2, @p3,@inserted_id);
INSERT INTO `cultures` (`id`, `name`, `zone_id`, `manager_id`, `state`) VALUES
(1, 'Amoebozoa', 1, @inserted_id, 0),
(2, 'Sporozoa', 2, @inserted_id, 0),
(3, 'Escherichia coli', 1, @inserted_id, 0),
(4, 'Ranunculus', 2, @inserted_id, 0),
(5, 'Archamoebae', 1, @inserted_id, 0),
(6, 'Flabellinea', 1, @inserted_id, 0);

SET @p0='tech1@foo.bar'; SET @p1='Tech1'; SET @p2='pass'; SET @p3='technician'; 
CALL g07_local.spCreateUser(@p0, @p1, @p2, @p3,@inserted_id);
```
- Criação de user (como root ou admin)  
:warning: Têm de selecionar primeiro a BD (e.g. USE aluno_g07_local;)
```mysql
SET @email:= 'inv1@foo.bar';SET @name:= 'Inv1';SET @pass:= '';SET @role:= 'researcher'; call spCreateUser(@email,@name,@pass,@role);
```

- Exibir roles
```mysql

Em root:
SHOW GRANTS FOR 'inv@foo.bar'; /* user */
SHOW GRANTS FOR 'group_researcher'; /* role (researcher) */

Da dessão ativa:
SHOW GRANTS;
```
- Stored Procedures
```mysql

DELIMITER $$
/* spCreateUser */
DROP PROCEDURE IF EXISTS spCreateUser;
CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateUser`(
	IN `p_email` VARCHAR(50) CHARSET latin1,
	IN `p_name` VARCHAR(100) CHARSET latin1,
	IN `p_pass` VARCHAR(64) CHARSET latin1,
	IN `p_role` ENUM('admin','researcher','technician') CHARSET latin1
)
BEGIN
IF NOT isEmail(p_email) THEN
	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "The email is invalid.";
END IF;

SET p_pass := CONCAT("'", p_pass, "'");

SET @p_role_group := CONCAT("'group_", p_role, "'");
SET @mysqluser := CONCAT("'", p_email,"'@'localhost'");


SET @sql := CONCAT('CREATE USER ', @mysqluser, ' IDENTIFIED BY ', p_pass);
SELECT @SQL;
PREPARE stmt FROM @sql;
EXECUTE stmt;


SET @sql := CONCAT('GRANT ', @p_role_group,' TO ', @mysqluser);
SELECT @SQL;
PREPARE stmt FROM @sql;
EXECUTE stmt;


SET @sql := CONCAT('SET DEFAULT ROLE ', @p_role_group,' FOR ', @mysqluser);
SELECT @SQL;
PREPARE stmt FROM @sql;
EXECUTE stmt;


INSERT INTO users (username,email) VALUES (p_name, p_email);

DEALLOCATE PREPARE stmt;
FLUSH PRIVILEGES;

END$$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS spCreateCultureParam;
CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateCultureParam`(
	IN user_id INT(11), 
	IN sensor_type VARCHAR(64), 
	IN valmax INT(11), 
	IN valmin INT(11), 
	IN tolerance INT(11), 
	OUT param_id INT(11)
)
IF ((SELECT role_id from users WHERE id = user_id) = 1 && valmax > valmin) THEN

INSERT INTO culture_params (sensor_type, valmax, valmin, tolerance)
VALUES (sensor_type, valmax, valmin, tolerance);
SET param_id = LAST_INSERT_ID();

END IF$$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS spCreateCultureParamsSet;
CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateCultureParamsSet`(
	IN user_id INT(11), 
	IN culture_id INT(11), 
	OUT set_id INT
)
IF ((SELECT role_id from users WHERE id = user_id) = 1) THEN

INSERT INTO culture_params_sets (culture_id)
VALUES (culture_id);
SET set_id = LAST_INSERT_ID();

END IF$$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS spCreateRelCultureParamsSet;
CREATE DEFINER=`root`@`localhost` PROCEDURE `spCreateRelCultureParamsSet`(
	IN user_id INT(11), 
	IN set_id INT(11), 
	IN param_id INT(11)
)
IF ((SELECT role_id from users WHERE id = user_id) = 1) THEN
	Insert INTO rel_culture_params_set (set_id, culture_param_id)
	VALUES (set_id, param_id);
END IF$$
DELIMITER ;

DELIMITER $$
DROP FUNCTION IF EXISTS isManager;
CREATE DEFINER=`root`@`localhost` FUNCTION `isManager`(
	`p_culture_id` INT
) RETURNS varchar(64) CHARSET utf8mb4
    SQL SECURITY INVOKER
BEGIN

DECLARE rev_username VARCHAR(64);
DECLARE at_sign_pos INT;
DECLARE username VARCHAR(64);
DECLARE is_manager INT;

SET rev_username = REVERSE(CURRENT_USER());
SET at_sign_pos = LOCATE("@", rev_username);
SET username = CONCAT("'",REVERSE(REPLACE(rev_username, LEFT(rev_username,at_sign_pos),"")),"'");
SET is_manager = 0;

SELECT COUNT(*) INTO is_manager 
FROM users AS u JOIN cultures AS c ON c.manager_id = u.id 
WHERE u.email=username AND c.id=p_culture_id; 

RETURN is_manager;
 
END$$
DELIMITER ;

DELIMITER $$
DROP FUNCTION IF EXISTS hasRole;
CREATE DEFINER=`root`@`localhost` FUNCTION `hasRole`(
	IN `p_role` ENUM('admin','researcher','technician') CHARSET latin1
	) RETURNS tinyint(4)
	SQL SECURITY INVOKER
RETURN CURRENT_ROLE()=CONCAT('group_', p_role);$$
DELIMITER ;

DELIMITER $$
DROP FUNCTION IF EXISTS isEmail;
CREATE DEFINER=`root`@`localhost` FUNCTION `isEmail`(`p_email` VARCHAR(50)
) RETURNS tinyint(4)
BEGIN
SET p_email = CONCAT("'",p_email,"'");
RETURN (SELECT p_email REGEXP '^[^@]+@[^@]+\.[^@]{2,}$')=1;
END$$
DELIMITER ;

DELIMITER $$
DROP FUNCTION IF EXISTS checkPrevAlert;
CREATE DEFINER=`root`@`localhost` FUNCTION checkPrevAlert(`rule_set_id` INT, mins INT) RETURNS tinyint(1)
RETURN EXISTS ( 
SELECT * 
FROM alerts 
WHERE parameter_set_id = rule_set_id 
AND
created_at >= NOW()- INTERVAL mins MINUTE 
)$$
DELIMITER ;

DELIMITER $$
DROP TRIGGER IF EXISTS existsPrevAlert;
CREATE TRIGGER existsPrevAlert BEFORE INSERT ON alerts
FOR EACH ROW IF checkPrevAlert(NEW.parameter_set_id,5) THEN
	SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = 'Alerta já existente';
END IF$$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS spAddUsersToCultures;
CREATE DEFINER=`root`@`localhost` PROCEDURE `spAddUsersToCultures`(
	IN `p_culture_id` INT(11),
	IN `p_user_id` INT(11)
)
    NO SQL
IF isManager(p_user_id) THEN
	Insert INTO culture_users (culture_id, user_id) VALUES (p_culture_id, p_user_id);
END IF$$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS spDeleteParam;
CREATE DEFINER=`root`@`localhost` PROCEDURE `spDeleteParam`(
	IN `p_param_id` INT
)
BEGIN
SET @culture_id:=-1;

SELECT sets.culture_id INTO @culture_id 
FROM culture_params AS params
JOIN rel_culture_params_set as rels ON params.id = rels.culture_param_id
JOIN culture_params_sets as sets ON sets.id = rels.set_id
WHERE params.id = p_param_id; 

CALL spStopIfNotManager(@culture_id);

DELETE FROM culture_params WHERE id=p_param_id;
END$$
DELIMITER ;
```

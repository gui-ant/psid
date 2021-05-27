DROP DATABASE IF EXISTS g07_local; 
DROP USER IF EXISTS 'admin1@foo.bar'@'%', 'res1@foo.bar'@'%', 'res2@foo.bar'@'%', 'tech1@foo.bar'@'%','aluno'@'%';
DROP ROLE IF EXISTS 'group_admin', 'group_researcher', 'group_technician';
CREATE USER 'aluno'@'%' IDENTIFIED BY 'aluno';
GRANT ALL PRIVILEGES ON *.* TO 'aluno'@'%';
CREATE ROLE IF NOT EXISTS 'group_admin', 'group_researcher', 'group_technician';

GRANT CREATE USER ON *.* TO `group_admin`;
GRANT GRANT OPTION ON *.* TO 'group_admin';

\. g07_local.sql
\. g07_cloud.sql

GRANT SELECT,INSERT,UPDATE,DELETE ON g07_local.users TO 'group_admin';
GRANT SELECT,INSERT,UPDATE,DELETE ON g07_local.cultures TO 'group_admin';
GRANT SELECT,INSERT,UPDATE,DELETE ON g07_local.culture_users TO 'group_admin';
GRANT SELECT ON g07_local.measurements TO 'group_admin';
GRANT SELECT ON g07_local.alerts TO 'group_admin';

GRANT EXECUTE ON PROCEDURE g07_local.spAddUserToCulture TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spCreateCulture TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spCreateUser TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spDeleteCulture TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spDeleteUser TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spGetCultureById TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spGetCulturesByUserId TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spUpdateCultureName TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spUpdateUser TO 'group_admin';
GRANT EXECUTE ON PROCEDURE g07_local.spSetCultureManager TO 'group_admin';

GRANT SELECT ON g07_local.users TO `group_researcher`;
GRANT SELECT ON g07_local.measurements TO 'group_researcher';
GRANT SELECT ON g07_local.alerts TO 'group_researcher';

GRANT EXECUTE ON PROCEDURE g07_local.spGetCultureById TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spGetCulturesByUserId TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spUpdateCultureName TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spCreateCultureParam TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spCreateCultureParamsSet TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spCreateRelCultureParamsSet TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spDeleteParam TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spExportCultureMeasuresToCSV TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE g07_local.spGetCultureParams TO 'group_researcher';
GRANT EXECUTE ON FUNCTION g07_local.isManager TO 'group_researcher';

GRANT SELECT ON g07_local.users TO 'group_technician';
GRANT SELECT ON g07_local.alerts TO 'group_technician';

FLUSH PRIVILEGES;

use g07_local

SET @p0='admin1@foo.bar'; SET @p1='Admin1'; SET @p2='pass'; SET @p3='admin'; 
SET @admin1_id=-1; CALL spCreateUser(@p0, @p1, @p2, @p3, @admin1_id);

SET @p0='res1@foo.bar'; SET @p1='Aristotle'; SET @p2='pass'; SET @p3='researcher'; 
SET @res1_id=-1; CALL spCreateUser(@p0, @p1, @p2, @p3, @res1_id);

SET @p0='res2@foo.bar'; SET @p1='Darwin'; SET @p2='pass'; SET @p3='researcher'; 
SET @res2_id=-1; CALL spCreateUser(@p0, @p1, @p2, @p3, @res2_id);

SET @p0='tech1@foo.bar'; SET @p1='Tech1'; SET @p2='pass'; SET @p3='technician'; 
SET @tech1_id=-1; CALL spCreateUser(@p0, @p1, @p2, @p3, @tech1_id);

INSERT INTO `cultures` (`id`, `name`, `zone_id`, `manager_id`, `state`) VALUES
(1, 'Amoebozoa', 1, @res1_id, 0),
(2, 'Sporozoa', 2, @res2_id, 0),
(3, 'Escherichia coli', 1, @res1_id, 0),
(4, 'Ranunculus', 2, @res2_id, 0),
(5, 'Archamoebae', 1, @res1_id, 0),
(6, 'Flabellinea', 1, @res2_id, 0);

CALL spAddUserToCulture(1, @res2_id);
CALL spAddUserToCulture(2, @res1_id);

SET @set_id=0;SET @param_id=0; 

SET @culture_id=1; 
INSERT INTO culture_params_sets (culture_id) VALUES (@culture_id); SET @set_id = LAST_INSERT_ID();
INSERT INTO culture_params (sensor_type, valmax, valmin, tolerance) VALUES ("H", 20, 10, 0); SET @param_id = LAST_INSERT_ID();
INSERT INTO rel_culture_params_set (set_id, culture_param_id) VALUES (@set_id, @param_id);

INSERT INTO culture_params_sets (culture_id) VALUES (@culture_id); SET @set_id = LAST_INSERT_ID();
INSERT INTO culture_params (sensor_type, valmax, valmin, tolerance) VALUES ("L", 1, -5, 0); SET @param_id = LAST_INSERT_ID();
INSERT INTO rel_culture_params_set (set_id, culture_param_id) VALUES (@set_id, @param_id);
INSERT INTO culture_params (sensor_type, valmax, valmin, tolerance) VALUES ("T", 5, 0, 0); SET @param_id = LAST_INSERT_ID();
INSERT INTO rel_culture_params_set (set_id, culture_param_id) VALUES (@set_id, @param_id);

SET @culture_id=2; 
INSERT INTO culture_params_sets (culture_id) VALUES (@culture_id); SET @set_id = LAST_INSERT_ID();
INSERT INTO culture_params (sensor_type, valmax, valmin, tolerance) VALUES ("L", 20, 15, 0); SET @param_id = LAST_INSERT_ID();
INSERT INTO rel_culture_params_set (set_id, culture_param_id) VALUES (@set_id, @param_id);
INSERT INTO culture_params (sensor_type, valmax, valmin, tolerance) VALUES ("T", 30, 5, 60); SET @param_id = LAST_INSERT_ID();
INSERT INTO rel_culture_params_set (set_id, culture_param_id) VALUES (@set_id, @param_id);

SET @culture_id=3; 
INSERT INTO culture_params_sets (culture_id) VALUES (@culture_id); SET @set_id = LAST_INSERT_ID();
INSERT INTO culture_params (sensor_type, valmax, valmin, tolerance) VALUES ("H", 10, -10, 0); SET @param_id = LAST_INSERT_ID();
INSERT INTO rel_culture_params_set (set_id, culture_param_id) VALUES (@set_id, @param_id);
INSERT INTO culture_params (sensor_type, valmax, valmin, tolerance) VALUES ("L", 10, 5, 0); SET @param_id = LAST_INSERT_ID();
INSERT INTO rel_culture_params_set (set_id, culture_param_id) VALUES (@set_id, @param_id);
INSERT INTO culture_params (sensor_type, valmax, valmin, tolerance) VALUES ("T", 5, 0, 0); SET @param_id = LAST_INSERT_ID();
INSERT INTO rel_culture_params_set (set_id, culture_param_id) VALUES (@set_id, @param_id);
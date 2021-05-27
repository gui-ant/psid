## Grupo 7
Link ZOOM Slot 5: https://videoconf-colibri.zoom.us/j/87585381703

**Drive**:<br/>
- [Relatório](https://docs.google.com/document/d/1F14r7k54XJ3Kmzq6IZxJsG_Xur3vkzZY/edit)<br/>
- [Relatório Grupo02](https://docs.google.com/document/d/1SCfdpyMIYwfB00AgGP9rdt_9Ycls6vsEastxUZjk_HM/edit?usp=sharing)
- [Doc Preparação Oral](https://docs.google.com/document/d/1eHwrF3U2C-V8QPtfatJ6IAoDTyzEBnl2gizSGe3CU7M/edit?usp=sharing)
- [Croqui Laboratório](https://docs.google.com/document/d/1Lv8bhDtPm4bYxZKTBfCdPttEHuGRpBRA/edit)<br/>
- [Perguntas para reuniões](https://docs.google.com/document/d/1m1g19S2wEBp_5jOAlmTetTr329ICJ58XwlmQ7cQJcI4/edit?usp=sharing)<br/>
- [Link para a ficha de grupo](https://drive.google.com/file/d/1HMAvvbRs9QXDj8qZwiOb9Uf7KmsjCt36/view?usp=sharing)<br>

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

[phpMyAdmin](http://194.210.86.10/phpmyadmin/db_structure.php?server=1&db=aluno_g07) (user: aluno, pass: aluno)

- Reset à DB por CLI (Shell XAMPP)
```
cd <path_to_project>
mysql -u root < db_reset.sql
```

- Criação de Roles no MySQL de acordo com a especificação  
⚠️Script de reset à DB já corre estes comandos
```mysql
CREATE ROLE IF NOT EXISTS 'group_admin';
GRANT CREATE USER ON *.* TO `group_admin`;
GRANT GRANT OPTION ON *.* TO 'group_admin';
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

CREATE ROLE IF NOT EXISTS 'group_researcher';
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

CREATE ROLE IF NOT EXISTS 'group_technician';
GRANT SELECT ON g07_local.users TO 'group_technician';
GRANT SELECT ON g07_local.alerts TO 'group_technician';

FLUSH PRIVILEGES;
```
- Criação de users e culturas default (como root ou admin)  

|User          |Email          |Name        |Pass |Role             | 
|--------------|---------------|------------|-----|-----------------| 
|Administrador |admin1@foo.bar |Admin1      |pass |group_admin      | 
|Investigador  |res1@foo.bar   |Aristotle   |pass |group_researcher | 
|Investigador  |res2@foo.bar   |Darwin      |pass |group_researcher | 
|Técnico Man.  |tech1@foo.bar  |Tech1       |pass |group_technician | 
  
⚠️Script de reset à DB já corre estes comandos
```mysql
use g07_local

DELIMITER $$

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
$$
DELIMITER ;
```

- Criação de parametrização default para a cultura (1 - Amoebozoa). Cria 2 Sets (OR) e um dos Sets tem parametrizações para 2 tipos de sensor (AND).
 
:warning: Têm de definir o manager da cultura para o mesmo user que executa os comandos seguintes, pois é feita a validação se o user é responsável pela cultura que quer parametrizar(e.g. definem 'Res1' como responsável da cultura 1, logam-se como 'Res1' no mysql e correm os comandos)  

```mysql
use g07_local

DELIMITER $$

SET @culture_id=1;

SET @set_id=0;SET @param_id=0; 
call spCreateCultureParamsSet(@culture_id,@set_id); 
call spCreateCultureParam("H",20.0,10.0,0,@set_id,@param_id);

SET @set_id=0;SET @param_id=0; 
call spCreateCultureParamsSet(@culture_id,@set_id);
call spCreateCultureParam("L",1.0,-5.0,0,@set_id,@param_id);
call spCreateCultureParam("T",5.0,0.0,0,@set_id,@param_id);
$$
DELIMITER ;
```

- Exibir roles
```mysql

Em root:
SHOW GRANTS FOR 'res1@foo.bar'; /* user */
SHOW GRANTS FOR 'group_researcher'; /* role (researcher) */

Da dessão ativa:
SHOW GRANTS;
```

<br>
<hr>
<br>
<strong><u>Usar os sensores</u>:</strong>
<ul>
 <li>Abrir o MQTT client (http://www.hivemq.com/demos/websocket-client/)</li>
   <ul>
    <li>HOST: broker.mqttdashboard.com</li>
    <li>Add New Topic Subscription: pisid_g07_sensors</li>
   </ul>
 <li>double-tap nos .jar que estão nas pastas SimulateSensorXY (dentro de resources)</li>
 <li>no IntelliJ: no package common, correr o BrokerToMongo</li>
</ul>

<br>
<hr>
<br>
<strong><u>Conteudo dos .conf das replicas</u>:</strong>
<br>
<br>
db1.conf
<br>
storage:
<br>
  dbPath: /home/rep/rpl1/data/
  <br>
  journal:
  <br>
    enabled: true
    <br>
    <br>

systemLog:
<br>
  destination: file
  <br>
  logAppend: true
  <br>
  path: /home/rep/rpl1/logs/mongo.log
  <br>
  <br>

net:
<br>
  port: 28017
  <br>
  bindIp: 192.168.31.126
  <br>
  <br>

security:
<br>
  authorization: enabled
  <br>
  keyFile: /home/rep/key.key
  <br>
  <br>

replication:
<br>
  replSetName: replica
  <br>
  <br>
  <br>



db2.conf
<br>
storage:
<br>
  dbPath: /home/rep/rpl2/data/
  <br>
  journal:
  <br>
    enabled: true
    <br>
    <br>

systemLog:
<br>
  destination: file
  <br>
  logAppend: true
  <br>
  path: /home/rep/rpl2/logs/mongo.log
  <br>
  <br>

net:
<br>
  port: 25017
  <br>
  bindIp: 192.168.31.126
  <br>
  <br>

security:
<br>
  authorization: enabled
  <br>
  keyFile: /home/rep/key.key
  <br>
  <br>

replication:
<br>
  replSetName: replica
  <br>
  <br>
  <br>



db3.conf
<br>
storage:
<br>
  dbPath: /home/rep/rpl3/data/
  <br>
  journal:
  <br>
    enabled: true
    <br>
    <br>

systemLog:
<br>
  destination: file
  <br>
  logAppend: true
  <br>
  path: /home/rep/rpl3/logs/mongo.log
  <br>
  <br>

net:
<br>
  port: 23017
  <br>
  bindIp: 192.168.31.126
  <br>
  <br>

security:
<br>
  authorization: enabled
  <br>
  keyFile: /home/rep/key.key
  <br>
  <br>

replication:
<br>
  replSetName: replica
  <br>
  <br>

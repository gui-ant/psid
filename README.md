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

Criação de Roles no MySQL
```mysql
USE mysql;
CREATE ROLE 'admin';
GRANT SELECT,INSERT,UPDATE,DELETE ON aluno_g07_local.users TO 'admin';
GRANT SELECT,INSERT,UPDATE,DELETE ON aluno_g07_local.cultures TO 'admin';
GRANT SELECT,INSERT,UPDATE,DELETE ON aluno_g07_local.culture_users TO 'admin';
GRANT SELECT ON aluno_g07_local.measurements TO 'admin';
GRANT SELECT ON aluno_g07_local.alerts TO 'admin';

GRANT EXECUTE ON PROCEDURE spCreateUser TO 'admin';
GRANT EXECUTE ON PROCEDURE spCreateRole TO 'admin';
GRANT EXECUTE ON PROCEDURE spDeleteUser TO 'admin';
GRANT EXECUTE ON PROCEDURE spUpdateUser TO 'admin';
GRANT EXECUTE ON PROCEDURE spGetUserById TO 'admin';
GRANT EXECUTE ON PROCEDURE spGetUserByRoleId TO 'admin';
GRANT EXECUTE ON PROCEDURE spAddUserToCultures TO 'admin';
GRANT EXECUTE ON PROCEDURE spGetCultureById TO 'admin';
GRANT EXECUTE ON PROCEDURE spGetCulturesByUserId TO 'admin';
GRANT EXECUTE ON PROCEDURE spCreateCulture TO 'admin';
GRANT EXECUTE ON PROCEDURE spDeleteCulture TO 'admin';
GRANT EXECUTE ON PROCEDURE spUpdateCultureName TO 'admin';

CREATE ROLE 'researcher';
GRANT SELECT ON aluno_g07_local.* TO 'researcher';

GRANT EXECUTE ON PROCEDURE spGetCultureById TO 'researcher';
GRANT EXECUTE ON PROCEDURE spGetCulturesByUserId TO 'researcher';
GRANT EXECUTE ON PROCEDURE spUpdateCultureName TO 'researcher';
GRANT EXECUTE ON PROCEDURE spCreate_culture_params TO 'researcher';
GRANT EXECUTE ON PROCEDURE spCreate_rel_culture_params_set TO 'researcher';
GRANT EXECUTE ON PROCEDURE spCreate_culture_params_set TO 'researcher';
GRANT EXECUTE ON PROCEDURE spDeleteParam TO 'researcher';
GRANT EXECUTE ON PROCEDURE spExportCultureMeasuresToCSV TO 'researcher';
GRANT EXECUTE ON PROCEDURE spIsManager TO 'researcher';
GRANT EXECUTE ON PROCEDURE spIsResearcher TO 'researcher';

CREATE ROLE 'technician';
GRANT SELECT ON aluno_g07_local.users TO 'technician';
GRANT SELECT ON aluno_g07_local.alerts TO 'technician';
```

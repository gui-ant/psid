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

CREATE ROLE 'group_admin';
GRANT CREATE_USER TO 'group_admin';
GRANT SELECT,INSERT,UPDATE,DELETE ON aluno_g07_local.users TO 'group_admin';
GRANT SELECT,INSERT,UPDATE,DELETE ON aluno_g07_local.cultures TO 'group_admin';
GRANT SELECT,INSERT,UPDATE,DELETE ON aluno_g07_local.culture_users TO 'group_admin';
GRANT SELECT ON aluno_g07_local.measurements TO 'group_admin';
GRANT SELECT ON aluno_g07_local.alerts TO 'group_admin';

GRANT EXECUTE ON PROCEDURE aluno_g07_local.spCreateUser TO 'group_admin';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spCreateRole TO 'group_admin';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spDeleteUser TO 'group_admin';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spUpdateUser TO 'group_admin';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spGetUserById TO 'group_admin';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spGetUserByRoleId TO 'group_admin';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spAddUserToCultures TO 'group_admin';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spGetCultureById TO 'group_admin';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spGetCulturesByUserId TO 'group_admin';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spCreateCulture TO 'group_admin';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spDeleteCulture TO 'group_admin';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spUpdateCultureName TO 'group_admin';

CREATE ROLE 'group_researcher';
GRANT SELECT ON aluno_g07_local.* TO 'group_researcher';

GRANT EXECUTE ON PROCEDURE aluno_g07_local.spGetCultureById TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spGetCulturesByUserId TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spUpdateCultureName TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spCreate_culture_params TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spCreate_rel_culture_params_set TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spCreate_culture_params_set TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spDeleteParam TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spExportCultureMeasuresToCSV TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spIsManager TO 'group_researcher';
GRANT EXECUTE ON PROCEDURE aluno_g07_local.spIsResearcher TO 'group_researcher';

CREATE ROLE 'group_technician';
GRANT SELECT ON aluno_g07_local.users TO 'group_technician';
GRANT SELECT ON aluno_g07_local.alerts TO 'group_technician';

FLUSH PRIVILEGES;
```
- Criação de user 'researcher'
```mysql
CREATE USER 'inv@foo.bar';
GRANT 'group_researcher' TO 'inv@foo.bar';
FLUSH PRIVILEGES;
```

- Exibir roles
```mysql
SHOW GRANTS FOR 'inv@foo.bar'; /* user */
SHOW GRANTS FOR 'group_researcher'; /* role (researcher) */
```

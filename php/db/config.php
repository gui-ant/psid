<?php

$mysql_server = "194.210.86.10";
$mysql_database = "aluno_g07";
$mysql_user = "aluno";
$mysql_pass = "aluno";

function db_connect()
{
    return mysqli_connect(
        $GLOBALS['mysql_server'],
        $GLOBALS['mysql_user'],
        $GLOBALS['mysql_pass'],
        $GLOBALS['mysql_database']
    );
}

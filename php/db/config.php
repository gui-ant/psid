<?php

 $mysql_server = "localhost";
 $mysql_database = "g07_local";
 $mysql_user = "";
 $mysql_pass = "";

function db_connect($mysql_user = "", $mysql_pass = "")
{   

    // User logged
    if(isset($_SESSION['user_email'])){
        return mysqli_connect(
            $GLOBALS['mysql_server'],
            $_SESSION['user_email'],
            $_SESSION['user_pass'],
            $GLOBALS['mysql_database']
        );

    // Using function parameters
    }else{
        return mysqli_connect(
            $GLOBALS['mysql_server'],
            $mysql_user,
            $mysql_pass,
            $GLOBALS['mysql_database']
        );
    }
}

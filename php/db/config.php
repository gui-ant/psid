<?php
$mysql_server = "localhost";
$mysql_database = "g07_local";

function db_connect($mysql_user = "", $mysql_pass = "")
{
    if (isset($_SESSION['user_email'])) {
        $mysql_user = $_SESSION['user_email'];
        $mysql_pass = $_SESSION['user_pass'];
    }
    return mysqli_connect(
        $GLOBALS['mysql_server'],
        $mysql_user,
        $mysql_pass,
        $GLOBALS['mysql_database']
    );
}

function db_curl_request($url)
{
    $client = curl_init($url);

    curl_setopt_array($client, array(
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => "session=" . json_encode($_SESSION),
    ));

    return curl_exec($client);
}

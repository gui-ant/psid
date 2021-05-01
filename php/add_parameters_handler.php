<?php
//include('cultures.php');
//echo var_dump($_POST['culture_id']);

// if ( !isset($_POST['hum']) && !isset($_POST['temp']) && !isset($_POST['luz']) ) {
//     $_GLOBALS['message'] = "Please select at least one of the checkboxes (Humidity, Temperature, Light)";
//     include('add_parameters.php');
// } else {
if (isset($_POST['hum']) || isset($_POST['temp']) || isset($_POST['luz'])){

    $params = array();

    if (isset($_POST['hum'])) {

        $param_id_hum = (int)create_param("H", $_POST['min_h'], $_POST['max_h'], $_POST['tol_h']);
        array_push($params, $param_id_hum);

    }

    if (isset($_POST['temp'])) {

        $param_id_temp = (int)create_param("T", $_POST['min_t'], $_POST['max_t'], $_POST['tol_t']);
        array_push($params, $param_id_temp);

    }

    if (isset($_POST['luz'])) {

        $param_id_luz = (int)create_param("L", $_POST['min_l'], $_POST['max_l'], $_POST['tol_l']);
        array_push($params, $param_id_luz);

    }

    $set_id = (int)create_set($_POST['culture_id']);

    foreach($params as $param_id) {
        create_rel($set_id, $param_id);
    }

    include('add_parameters.php');

}

function create_param($sensor, $valmax, $valmin, $tolerance){
    $url = "http://localhost/psid/php/db/getStoredProcData.php?sp=spCreateCultureParam&p=" . $_SESSION['user_id'] . $sensor . (int)$valmax. (int)$tolerance . "&json=false";
    echo $url . "<br>";
    $client = curl_init($url);
    curl_setopt($client, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($client, CURLOPT_POST, true);
    curl_setopt($client, CURLOPT_POSTFIELDS, "username=".$_SESSION['user_email']."&password=".$_SESSION['user_pass']);
    $response = curl_exec($client);

    return $response;
}

function create_set($culture_id) {
    $url = "http://localhost/psid/php/db/getStoredProcData.php?sp=spCreateCultureParamsSet&p=" . $_SESSION['user_id'] . $culture_id . "&json=false";
    echo $url . "<br>";
    $client = curl_init($url);
    curl_setopt($client, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($client, CURLOPT_POST, true);
    curl_setopt($client, CURLOPT_POSTFIELDS, "username=".$_SESSION['user_email']."&password=".$_SESSION['user_pass']);
    $response = curl_exec($client);

    return $response;
}

function create_rel($set_id, $param_id) {
    $url = "http://localhost/psid/php/db/getStoredProcData.php?sp=spRelCreateCultureParamsSet&p=" . $_SESSION['user_id'] . $set_id . $param_id . "&json=false";
    echo $url . "<br>";
    $client = curl_init($url);
    curl_setopt($client, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($client, CURLOPT_POST, true);
    curl_setopt($client, CURLOPT_POSTFIELDS, "username=".$_SESSION['user_email']."&password=".$_SESSION['user_pass']);
}


?>
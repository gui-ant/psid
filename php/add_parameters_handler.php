<?php
include('db/config.php');
//echo var_dump($_POST['culture_id']);

// verifica se existe pelo menos uma checkbox assinalada
if (isset($_POST['hum']) || isset($_POST['temp']) || isset($_POST['luz'])){

    $params = array();

    if (isset($_POST['hum'])) {

        //create_param("H", $_POST['min_h'], $_POST['max_h'], $_POST['tol_h'], $set_id, $param_id);
        $param_hum = '"H",' . $_POST['max_h'] . ',' . $_POST['min_h'] . ',' . $_POST['tol_h'] . ',@set_id,@param_id);';
        array_push($params, $param_hum);
        

    }

    if (isset($_POST['temp'])) {

        //$param_id_temp = (int)create_param("T", $_POST['min_t'], $_POST['max_t'], $_POST['tol_t'], $set_id, $param_id);
        $param_temp = '"T",' . $_POST['max_t'] . ',' . $_POST['min_t'] . ',' . $_POST['tol_t'] . ',@set_id,@param_id);';
        array_push($params, $param_temp);

    }

    if (isset($_POST['luz'])) {

        $param_luz = '"L",' . $_POST['max_l'] . ',' . $_POST['min_l'] . ',' . $_POST['tol_l'] . ',@set_id,@param_id);';
        array_push($params, $param_luz);

    }

    create_params($params);

    //include('index.php');
    //header("location: add_parameters.php");

} else {

    // verifica se o formulario ja foi submitted
    if (isset($_POST['submit'])) {
        echo '<div class="u-form-send-error u-form-send-message"> Please select at least one of the checkboxes (Humidity, Temperature, Light) </div>';
    }

}

function create_params($params) {

    $conn = db_connect();

    if (!$conn) {
        die("Connection failed: " .$conn->connect_error);
        return false;
      }

    $query = "
    SET @user_id=" . $_SESSION['user_id'] . ";SET @set_id=0;SET @param_id=0;
    call spCreateCultureParamsSet(" . $_POST['culture_id'] . ",@set_id);";

    foreach($params as $p) {
        $query = $query . "call spCreateCultureParam(" . $p;
    }
      //echo var_dump($query) . "<br>";
    
    $results = mysqli_multi_query($conn, $query);
    mysqli_close($conn);

    if ($results){
        echo '<div class="u-form-send-message u-form-send-success"> Your form has been successfully submitted :) </div>';
    } else {
        echo '<div class="u-form-send-error u-form-send-message"> Unable to send your data. Please fix errors then try again. </div>';
    }

} 


?>
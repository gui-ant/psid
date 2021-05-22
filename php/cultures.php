<?php
$culture_id = "";
$pieces = null;

if (!empty($_GET['culture_id']))
    $culture_id = $_GET['culture_id'];

if (isset($_POST['update_culture'])) {
    if (!empty($_POST['culture_name']))
        $culture_name = $_POST['culture_name'];
    else
        $culture_name = " ";
    $result = edit_culture_data($culture_id, $culture_name);
    $success = "Cultura atualizada";
}

function edit_culture_data($culture_id, $culture_name)
{
    $conn = db_connect();

    $number_updates = 0;

    if ($conn) {
        $sql = "call spUpdateCultureName($culture_id, '$culture_name');";

        $res = mysqli_query($conn, $sql);
        if (!$res) {
            $result = new stdClass();
            $result->status = false;
            $result->msg = mysqli_error($conn);
            echo mysqli_error($conn);
            exit;
        }
        $number_updates = 1;

        mysqli_close($conn);
    }
    return $number_updates;
}
?>


<?php
$url = "localhost/psid/php/db/getStoredProcData.php?sp=spGetCulturesByUserId&p=" . $_SESSION['user_id'] . "&json=true";
$res = db_curl_request($url);
$user_cultures = json_decode($res);

if ($culture_id != "") {
    $url = "localhost/psid/php/db/getStoredProcData.php?sp=spGetCultureById&p=" . $culture_id . "&json=true";
    $res = db_curl_request($url);
    $active_culture = json_decode($res)[0];

    // echo var_dump($response);
    // echo var_dump($pieces);

    $url = "localhost/psid/php/db/getStoredProcData.php?sp=spGetCultureParams&p=" . $culture_id . "&json=true";
    $res = db_curl_request($url);
    $culture_params = json_decode($res);

    $params_sets = array();
    foreach ($culture_params as $i => $param) {
        if (!isset($params_sets[$param->set_id]))
            $params_sets[$param->set_id] = array();
        array_push($params_sets[$param->set_id], $param);
    }
}
?>

<?php if (isset($success)) : ?>
    <div class="alert alert-success">
        <?= $success; ?>
    </div>
    <?php unset($success); ?>
<?php endif ?>

<?php if (isset($user_cultures)) : ?>
    <div class="container-fluid py-5">

        <form action="index.php">
            <div class="row">
                <div class="col-md-5 text-end">
                    <h4 class="section-title text-light text-end">Select a culture</h3>
                </div>
                <div class="col-md-5 px-md-0">
                    <div class="form-group">
                        <select name="culture_id" onchange="this.form.submit()" class="form-select bg-dark text-white">
                            <option> </option>
                            <?php foreach ($user_cultures as $i => $piece) : ?>
                                <?php if (isset($_GET['culture_id'])) : ?>
                                    <option value="<?= $piece->id; ?>" <?= $_GET['culture_id'] == $piece->id ? 'selected' : ''; ?>><?= $piece->name; ?></option>
                                <?php else : ?>
                                    <option value="<?= $piece->id; ?>"><?= $piece->name; ?></option>
                                <?php endif; ?>
                            <?php endforeach; ?>
                        </select>
                    </div>
                </div>
            </div>
        </form>
    </div>
<?php else : ?>
    <p>You don't have any cultures yet.</p>
<?php endif; ?>

<?php if (isset($active_culture)) : ?>
    <div class="container-fluid py-5">

        <div class="row">
            <div class="col-md-5 py-3">
                <h4 class="section-title text-light text-end">Culture's Information</h3>
            </div>
            <div class="col-md-5 py-3 container-border">

                <form action="index.php?culture_id=<?= $culture_id ?>" method="POST" id="itw3p" class="form-block">
                    <div class="form-group">
                        <label for="culture_id" id="culture_id" class="form-label">Culture's ID:</label>
                        <input type="text" name="culture_id" class="form-control form-input" value="<?= $active_culture->id; ?>" readonly />
                    </div>
                    <div class="form-group">
                        <label for="culture_name" id="culture_name" class="form-label">Culture's name:</label>
                        <input type="text" name="culture_name" value="<?= $active_culture->name; ?>" <?= $active_culture->manager_id == $_SESSION['user_id'] ?: "readonly" ?> class="form-control form-input" />
                    </div>
                    <div class="form-group">
                        <input type="hidden" name="update_culture">
                        <input type=submit value="Save Changes" class="btn btn-primary" <?= $active_culture->manager_id != $_SESSION['user_id'] ?  "disabled" : "" ?> />
                    </div>
                </form>

            </div>
        </div>
    </div>
<?php endif ?>

<?php if (isset($params_sets)) : ?>
    <div class="container-fluid py-5">
        <div class="row">
            <div class="col-md-5 py-3">
                <h4 class="section-title text-light text-end">Culture's Paramaters</h4>
            </div>
            <div class="col-md-5 py-3 container-border">
                <form action="parameters_handler.php?culture_id=<?= $active_culture->id; ?>" method="POST" class="form-block">
                    <div class="form-group">
                        <?php foreach ($params_sets as $i => $param) : ?>
                            <input type="hidden" name="culture_name" value="<?= $active_culture->name; ?>">
                            <div class="form-check">
                                <?php foreach ($param as $p) : ?>
                                    <label for="chk_param_<?= $param[0]->id; ?>">
                                        <input id="chk_param_<?= $param[0]->id; ?>" type="checkbox" name="chk_param[]" value="<?= $p->id; ?>" <?= $active_culture->manager_id == $_SESSION['user_id'] ?: "disabled" ?>>
                                        <?php switch($p->sensor_type) :
                                            case "H": 
                                                echo "Sensor Type: Humidity, Min. Val.: $p->valmin, Max. Val.: $p->valmax, Tolerance: $p->tolerance; "; 
                                                break;
                                            case "T":
                                                echo "Sensor Type: Temperature, Min. Val.: $p->valmin, Max. Val.: $p->valmax, Tolerance: $p->tolerance; "; 
                                                break; 
                                            case "L": 
                                                echo "Sensor Type: Light, Min. Val.: $p->valmin, Max. Val.: $p->valmax, Tolerance: $p->tolerance; " ;
                                                break; 
                                            default: 
                                                "Sensor Type: $p->sensor_type, Min. Val.: $p->valmin, Max. Val.: $p->valmax, Tolerance: $p->tolerance; " ;
                                        endswitch; ?>
                                        
                                    </label>
                                <?php endforeach; ?>
                            </div>
                        <?php endforeach; ?>
                        <div class="row py-0">
                            <div class="col-6">
                                <a href="add_parameters.php?culture_id=<?= $active_culture->id; ?>" target="_blank" class="btn btn-primary btn-add <?= $active_culture->manager_id != $_SESSION['user_id'] ?  "disabled" : "" ?>">Add Parameters</a>
                            </div>
                            <div class="col-6 text-end">
                                <input type="submit" name="delete_param" value="Delete" class="btn btn-primary" <?= $active_culture->manager_id != $_SESSION['user_id'] ?  "disabled" : "" ?> />
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
<?php endif ?>
</body>

</html>
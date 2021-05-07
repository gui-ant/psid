<?php


$culture_id = "";
$pieces = null;

if (!empty($_POST['culture_id']))
	$culture_id = $_POST['culture_id'];

if (isset($_POST['update_culture'])) {
	if (!empty($_POST['culture_name']))
		$culture_name = $_POST['culture_name'];
	else
		$culture_name = " ";
	$result = edit_culture_data($culture_id, $culture_name);
	$success = "Cultura atualizada";
}

if (isset($_POST['delete_param'])) {
	$checkboxes = $_POST['chk_param'];

	$result = delete_culture_params($checkboxes);
	$success = "Parametro elimnado";
}

function delete_culture_params($param_ids)
{
	$conn = db_connect();

	$number_updates = 0;
	if ($conn) {
		$sql = "";
		foreach ($param_ids as $id)
			$sql .= "call spDeleteParam($id);";

		$res = mysqli_multi_query($conn, $sql);
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

<?php if (isset($success)) : ?>
	<div class="success">
		<h3>
			<?php
			echo $success;
			unset($success);
			?>
		</h3>
	</div>
<?php endif ?>

<?php
$url = "localhost/psid/php/db/getStoredProcData.php?sp=spGetCulturesByUserId&p=" . $_SESSION['user_id'] . "&json=true";
$res = db_curl_request($url);
$user_cultures = json_decode($res);

if ($culture_id != "") {
	$url = "localhost/psid/php/db/getStoredProcData.php?sp=spGetCultureById&p=" . $culture_id . "&json=true";
	$res = db_curl_request($url);
	$active_culture = json_decode($res);

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





    <?php if (!is_null($user_cultures)) : ?>
        <div class="row">
            <div class="col-xl-8 col-lg-8 col-md-8 col-sm-8 col-12 py-3 border-left">
                <h3 class="text-light">Select a Culture:</h3>

                <form action="index.php" method="POST" id="igb8m" class="form-block">
                    <select name="culture_id" onchange="this.form.submit()" class="form-control">
                        <option> </option>
                        <?php foreach ($user_cultures as $i => $piece) : ?>
                            <?php if (isset($_POST['culture_id'])) : ?>
                                <option value="<?= $piece->id; ?>" <?= $_POST['culture_id'] == $piece->id ? 'selected' : ''; ?>><?= $piece->name; ?></option>
                            <?php else : ?>
                                <option value="<?= $piece->id; ?>"><?= $piece->name; ?></option>
                            <?php endif; ?>
                        <?php endforeach; ?>
                    </select>
                </form>

            </div>
            <div class="col-xl-4 col-lg-4 col-md-4 col-sm-4 col-12 py-3"></div>
        </div>
        <?php else : ?>
			<p>You don't have any cultures yet.</p>
		<?php endif; ?>

    <div class="container-fluid py-5">

        <?php if (isset($active_culture)) : ?>
            <div class="row">
                <div class="col-xl-4 col-lg-4 col-md-4 col-sm-4 col-12 py-3">
                    <h4 class="text-light text-center"><u>Culture's Information</u></h4>
                </div>
                <div class="col-xl-4 col-lg-4 col-md-4 col-sm-4 col-12 py-3 container-border">

                    <form action="index.php" method="POST" id="itw3p" class="form-block">
                        <label for="culture_id" id="culture_id" class="form-label">Culture's ID:</label>
                        <input type="text" name="culture_id" class="form-control form-input" value="<?= $active_culture[0]->id; ?>" readonly/>
                        <label for="culture_name" id="culture_name" class="form-label">Culture's name:</label>
                        <input type="text" name="culture_name" value="<?= $active_culture[0]->name; ?>" <?= $active_culture[0]->manager_id == $_SESSION['user_id'] ?: "readonly" ?> class="form-control form-input" />
                        <?php if ($active_culture[0]->manager_id == $_SESSION['user_id']) : ?>
                            <input type=submit value="Save Changes" class="btn btn-primary"/>
                        <?php endif ?>
                    </form>

                </div>
                <div class="col-xl-4 col-lg-4 col-md-4 col-sm-4 col-12 py-3"></div>
            </div>
        <?php endif ?>
    </div>

    <div class="container-fluid py-5">

        <?php if (isset($params_sets)) : ?>
            <div class="row">
                <div class="col-xl-4 col-lg-4 col-md-4 col-sm-4 col-12 py-3">
                    <h4 class="text-light text-center"><u>Culture's Paramaters</u></h4>
                </div>
                <div class="col-xl-4 col-lg-4 col-md-4 col-sm-4 col-12 py-3 container-border">

                    <form action="index.php" method="POST" class="form-block">
                        <?php foreach ($params_sets as $i => $param) : ?>
                            <div class="form-check">
                                <?php foreach ($param as $p) : ?>
                                    <label for="chk_param_<?= $param[0]->id; ?>" class="u-label u-label-1">
                                    <span></span>
                                    <input id="chk_param_<?= $param[0]->id; ?>" type="checkbox" name="chk_param[]" value="<?= $param[0]->id; ?>" <?= $active_culture[0]->manager_id == $_SESSION['user_id'] ?: "disabled" ?> class="form-check-input">
                                    <?= "Sensor Type: $p->sensor_type, Min. Val.: $p->valmin, Max. Val.: $p->valmax, Tolerance: $p->tolerance; " ?>
                                    </label>
                                <?php endforeach; ?>
                            </div>
                        <?php endforeach; ?>

                        <?php if ($active_culture[0]->manager_id == $_SESSION['user_id'] && count($params_sets) > 0) : ?>
                            <input type="submit" value="Delete" class="btn btn-primary"/>
                            <input type="hidden" name="delete_param">
                        <?php endif ?>
                    </form>

                </div>
                <div class="col-xl-4 col-lg-4 col-md-4 col-sm-4 col-12 py-3"></div>
            </div>

            <?php if ($active_culture[0]->manager_id == $_SESSION['user_id']) : ?>
                <div class="row">
                    <div class="col-xl-4 col-lg-4 col-md-4 col-sm-4 col-12 py-3"></div>
                    <div class="col-xl-4 col-lg-4 col-md-4 col-sm-4 col-12 py-3">

                        <form action="add_parameters.php" method="POST">
                            <input type="hidden" name="culture_id" value="<?= $active_culture[0]->id; ?>">
                            <input type="hidden" name="culture_name" value="<?= $active_culture[0]->name; ?>">
                            <input type="submit" class="btn btn-primary btn-add" value="Add Parameters" />
                        </form>
                        <!--<a href="#" target="_blank" class="btn btn-primary btn-add">Add Parameters</a>-->

                    </div>
                    <div class="col-xl-4 col-lg-4 col-md-4 col-sm-4 col-12 py-3"></div>
                </div>
            <?php endif ?> 
        <?php endif ?> 

        <section class="py-5"></section>
    </div>
    
</body>

</html>

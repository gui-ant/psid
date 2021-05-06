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


 
    <section class="u-align-center u-clearfix u-grey-90 u-section-1" id="carousel_b2e3">
      <div class="u-clearfix u-sheet u-sheet-1">
      

        <div class="u-form u-form-1">

        <?php if (!is_null($user_cultures)) : ?>
          
        <form action="index.php" method="POST" class="u-clearfix u-form-spacing-15 u-form-vertical u-inner-form" style="padding: 0px;" source="custom" name="Combobox">
            <div class="u-form-group u-form-select u-form-group-1">
              <div class="u-form-select-wrapper">

              <select name="culture_id" id="itens" onchange="this.form.submit()" class="u-border-2 u-border-grey-90 u-custom-font u-input u-input-rectangle u-palette-1-dark-1 u-text-font u-input-1" >
			
							<option>Pick a culture:</option>
							<?php foreach ($user_cultures as $i => $piece) : ?>
								<?php if (isset($_POST['culture_id'])) : ?>
									<option value="<?= $piece->id; ?>" <?= $_POST['culture_id'] == $piece->id ? 'selected' : ''; ?>><?= $piece->name; ?></option>
								<?php else : ?>
									<option value="<?= $piece->id; ?>"><?= $piece->name; ?></option>
								<?php endif; ?>
							<?php endforeach; ?>
							
							</select>

                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="12" version="1" class="u-caret"><path fill="currentColor" d="M4 8L0 4h8z"></path></svg>
              </div>
            
            <input type="hidden" name="goback" value="<?php echo $_SERVER['REQUEST_URI'] ?>">
          </form>

        </div>
      <?php else : ?>
				<p>You don't have any cultures yet.</p>
			<?php endif; ?>
      </div>
    </section>

    <?php if (isset($active_culture)) : ?>
    <section class="u-align-center u-clearfix u-grey-90 u-section-2" id="sec-ff2f">
      <div class="u-clearfix u-sheet u-sheet-1">
        <h5 class="u-align-center u-text u-text-1">Culture's data</h5>
        <div class="u-border-2 u-border-grey-50 u-container-style u-group u-shape-rectangle u-group-1">
          <div class="u-container-layout u-container-layout-1">
            <div class="u-form u-form-1">
              
            <form action="index.php" method="POST" class="u-clearfix u-form-spacing-12 u-form-vertical u-inner-form" style="padding: 0;" source="custom" name="form">
                <div class="u-form-group u-form-group-1">
                  <label for="text-6fa8" class="u-label u-label-1">Culture's id:</label>
                  <input type="text" id="text-6fa8" name="culture_id" class="u-input u-input-1" value="<?= $active_culture[0]->id; ?>" readonly>
                </div>
                <div class="u-form-group u-form-group-2">
                  <label for="text-6ea0" class="u-label u-label-2">Culture's name:</label>
                  <input type="text" id="text-6ea0" name="culture_name" class="u-input u-input-1" value="<?= $active_culture[0]->name; ?>" <?= $active_culture[0]->manager_id == $_SESSION['user_id'] ?: "readonly" ?>>
                </div>
                <?php if ($active_culture[0]->manager_id == $_SESSION['user_id']) : ?>
                <div class="u-align-right u-form-group u-form-submit u-form-group-3">
                  <!--<a href="#" class="u-black u-btn u-btn-rectangle u-btn-submit u-button-style u-hover-palette-1-dark-1 u-btn-1">Save</a>
                  <input type="submit" value="submit" class="u-form-control-hidden">-->
                  <input type="submit" value="Save" class="u-black u-btn u-btn-rectangle u-btn-submit u-button-style u-hover-palette-1-dark-1 u-btn-1">
                </div>
                <?php endif ?>
                
            </form>

            </div>
          </div>
        </div>
      </div>
    </section>
    <?php endif ?>

    <?php if (isset($params_sets)) : ?>
    <section class="u-align-center u-clearfix u-grey-90 u-section-3" id="carousel_7d02">
      <div class="u-clearfix u-sheet u-sheet-1">
        <h5 class="u-align-center u-text u-text-1">Culture's Parameters</h5>
        <div class="u-border-2 u-border-grey-50 u-container-style u-group u-shape-rectangle u-group-1">
          <div class="u-container-layout u-container-layout-1">
            
            <div class="u-form u-form-1">
              <form action="index.php" method="POST" class="u-clearfix u-form-spacing-12 u-form-vertical u-inner-form" style="padding: 0;" source="custom" name="form">
              
              <?php foreach ($params_sets as $i => $param) : ?>
                <div class="input-group" style="border: 3px dashed grey;border-radius: 6px;margin-bottom:10px; padding:3px;">
                  <div class="u-form-checkbox u-form-group u-form-group-1">
                    <?php foreach ($param as $p) : ?>
                      <label for="chk_param_<?= $param[0]->id; ?>" class="u-label u-label-1">
                        <span></span><input id="chk_param_<?= $param[0]->id; ?>" type="checkbox" name="chk_param[]" value="<?= $param[0]->id; ?>" <?= $active_culture[0]->manager_id == $_SESSION['user_id'] ?: "disabled" ?> class="u-block-45b7-76 u-border-2 u-border-grey-75 u-border-no-left u-border-no-right u-border-no-top u-input-rectangle">
                        <?= "Sensor Type: $p->sensor_type, Min. Val.: $p->valmin, Max. Val.: $p->valmax, Tolerance: $p->tolerance; " ?>
                      </label>
                    <?php endforeach; ?>
                  </div>
                </div>
              <?php endforeach; ?>

              <!--<div class="u-form-checkbox u-form-group u-form-group-2">
                  <input type="checkbox" id="checkbox-703f" name="checkbox" value="On">
                  <label for="checkbox-703f" class="u-label u-label-2">[Parameter 2]</label>
                </div>-->
                
              <?php if ($active_culture[0]->manager_id == $_SESSION['user_id'] && count($params_sets) > 0) : ?>
                <div class="u-align-right u-form-group u-form-submit u-form-group-6">
                  <!--<a href="#" class="u-black u-btn u-btn-rectangle u-btn-submit u-button-style u-hover-palette-1-dark-1 u-btn-1">Delete</a>
                  <input type="submit" value="submit" class="u-form-control-hidden">-->
                  <input type="submit" value="Delete" class="u-black u-btn u-btn-rectangle u-btn-submit u-button-style u-hover-palette-1-dark-1 u-btn-1">
                  <input type="hidden" name="delete_param">
                </div>
              <?php endif ?>
                
              </form>
            </div>
          </div>
        </div>

        <?php if ($active_culture[0]->manager_id == $_SESSION['user_id']) : ?>
					<form action="add_parameters.php" method="POST">
						<input type="hidden" name="culture_id" value="<?= $active_culture[0]->id; ?>">
						<input type="hidden" name="culture_name" value="<?= $active_culture[0]->name; ?>">
            <!--<a href="#" class="u-black u-btn u-button-style u-hover-palette-1-dark-1 u-btn-2">Add parameters</a>
            <input type="submit" value="submit" class="u-form-control-hidden">-->
						<input type="submit" class="u-black u-btn u-button-style u-hover-palette-1-dark-1 u-btn-2" value="Add Parameters" />
					</form>
				<?php endif ?>
        
      </div>
    </section>
    <?php endif ?>
    

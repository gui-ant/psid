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

function edit_culture_data($culture_id, $culture_name)
{
	$conn = db_connect();

	$number_updates = 0;

	if ($conn) {
		$sql = "call spUpdateCultureName($culture_id, '$culture_name');";
		$result = mysqli_query($conn, $sql);
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
}
?>

<?php if (!is_null($user_cultures)) : ?>
	<form name="Combobox" action="index.php" method="post">
		<div class="input-group">
			<select name="culture_id" id="itens" onchange="this.form.submit()">
				<option>Escolha uma cultura:</option>
				<?php foreach ($user_cultures as $i => $piece) : ?>
					<?php if (isset($_POST['culture_id'])) : ?>
						<option value="<?= $piece->id; ?>" <?= $_POST['culture_id'] == $piece->id ? 'selected' : ''; ?>><?= $piece->name; ?></option>
					<?php else : ?>
						<option value="<?= $piece->id; ?>"><?= $piece->name; ?></option>
					<?php endif; ?>
				<?php endforeach; ?>
			</select>
		</div>
		<!-- <input class="btn" type="submit" name="btnEnvia" value="Ver">-->
		<input class="btn" type="hidden" name="goback" value="<?php echo $_SERVER['REQUEST_URI'] ?>">
	</form>
<?php else : ?>
	<p>Não tem culturas atribuidas.</p>
<?php endif; ?>



<?php if (isset($active_culture)) : ?>
	Dados da Cultura
	<form action="index.php" method="POST" class="form-group">
		<br>
		<div class="input-group">
			<label>ID cultura:</label>
			<input type="text" name="culture_id" value="<?= $active_culture[0]->id; ?>" readonly>
		</div>
		<div class="input-group">
			<label>Nome cultura:</label>
			<input type="text" name="culture_name" value="<?= $active_culture[0]->name; ?>" <?= $active_culture[0]->manager_id == $_SESSION['user_id'] ? : "readonly" ?>>
		</div>
		
		<?php if ($active_culture[0]->manager_id == $_SESSION['user_id']) : ?>
			<input class="btn input-group" type="submit" name="submit" value="Gravar">
			<input type="hidden" name="update_culture">
			<input type="hidden" name="goback" value="<?php echo $_POST['goback']; ?>">
		<?php endif ?>
	</form>

	<?php if ($active_culture[0]->manager_id == $_SESSION['user_id']) : ?>
		<form action="add_parameters.php" method="POST">
			<input type="hidden" name="culture_id" value="<?= $active_culture[0]->id; ?>">
			<input type="hidden" name="culture_name" value="<?= $active_culture[0]->name; ?>">
			<input class="btn" type="submit" value="Adicionar Parâmetros"/>
		</form>
	<?php endif ?>

<?php endif ?>
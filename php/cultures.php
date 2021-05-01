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

if ($culture_id != "") {
	$url = "http://localhost/psid/php/db/getStoredProcData.php?sp=spGetCultureById&p=" . $culture_id . "&json=true";
	$client = curl_init($url);
	curl_setopt($client, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($client, CURLOPT_POST, true);
	curl_setopt($client, CURLOPT_POSTFIELDS, "username=".$_SESSION['user_email']."&password=".$_SESSION['user_pass']);
	$response = curl_exec($client);
	$pieces = json_decode($response);
	
	// echo var_dump($response);
	// echo var_dump($pieces);
}

function edit_culture_data($culture_id, $culture_name)
{
	$conn = db_connect();

	$number_updates = 0;

	if ($conn) {
		$sql = "UPDATE cultures SET name='$culture_name' WHERE id=$culture_id;";
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

<?php if (!is_null($pieces)) : ?>
	Dados da Cultura
	<form action="index.php" method="POST">
		<br>
		<div class="input-group">
			<label>ID cultura:</label>
			<input type="text" name="culture_id" value="<?= $pieces[0]->id; ?>" readonly>
		</div>
		<div class="input-group">
			<label>Nome cultura:</label>
			<input type="text" name="culture_name" value="<?= $pieces[0]->name; ?>" <?= $pieces[0]->manager_id == $_SESSION['user_id'] ? : "readonly" ?>>
			
		</div>
		
		<?php if ($pieces[0]->manager_id == $_SESSION['user_id']) : ?>
			<input class="btn input-group" type="submit" name="submit" value="Gravar">
			<input type="hidden" name="update_culture">
			<input type="hidden" name="goback" value="<?php echo $_POST['goback']; ?>">
		<?php endif ?>
	</form>

	<?php if ($pieces[0]->manager_id == $_SESSION['user_id']) : ?>
		<form action="add_parameters.php" method="POST">
			<input type="hidden" name="culture_id" value="<?= $pieces[0]->id; ?>">
			<input type="hidden" name="culture_name" value="<?= $pieces[0]->name; ?>">
			<input class="btn" type="submit" value="Adicionar Parâmetros"/>
		</form>
	<?php endif ?>

<?php endif ?>
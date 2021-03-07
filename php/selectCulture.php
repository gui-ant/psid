<?php
$url = "http://localhost/psid/php/dbGetStorProcData.php?sp=spGetCultureByUserId&p=" . $_SESSION['user_id'] . "&json=true";
$client = curl_init($url);
curl_setopt($client, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($client);
$pieces = json_decode($response);
?>
<form name="Combobox" action="index.php" method="post">
	<select name="culture_id" id="itens" class="input-group">
		<option value=" " <?php !isset($_POST['culture_id']) ? 'selected=selected' : '' ?>>Escolha uma cultura:</option>
		<?php
		foreach ($pieces as $i => $piece) {
			if (isset($_POST['culture_id'])) {
				echo '<option value="' . $piece->id . '" ' . ($_POST['culture_id'] == $piece->id ? 'selected="selected"' : '') . '>' . $piece->name . '</option>';
			} else {
				echo '<option value="' . $piece->id . '">' . $piece->name . '</option>';
			}
		}
		?>
	</select>
	<input type="submit" name="btnEnvia" value="Ver" class="btn">
	<input type="hidden" name="goback" value="<?php echo $_SERVER['REQUEST_URI'] ?>">
</form>
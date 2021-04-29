<?php
$url = "http://localhost/psid/php/db/getStoredProcData.php?sp=spGetCulturesByUserId&p=" . $_SESSION['user_id'] . "&json=true";
//echo $url . "<br>";
$client = curl_init($url);
curl_setopt($client, CURLOPT_RETURNTRANSFER, true);
curl_setopt($client, CURLOPT_POST, true);
curl_setopt($client, CURLOPT_POSTFIELDS, "username=".$_SESSION['user_email']."&password=".$_SESSION['user_pass']);
$response = curl_exec($client);
$pieces = json_decode($response);


//echo "pieces: " . var_dump($pieces) . "<br>";
//echo "response: " . var_dump($response) . "<br>";
?>

<?php if (!is_null($pieces)) : ?>
	<form name="Combobox" action="index.php" method="post">
		<div class="input-group">
			<select name="culture_id" id="itens">
				<option value=" " <?php !isset($_POST['culture_id']) ? 'selected=selected' : '' ?>>Escolha uma cultura:</option>
				<?php
				foreach ($pieces as $i => $piece) {
				    
					if (isset($_POST['culture_id'])) {
						//echo "<option value='" . $piece->id . "' " .($_POST['culture_id'] == $piece->id ? "selected='selected'" : "") . ">" . $piece->name . "</option>";

						echo '<option value="' . $piece->id . '" ' . ($_POST['culture_id'] == $piece->id ? 'selected="selected"' : '') . '>' . $piece->name . '</option>';

						//echo "<option value='". $data['city_name'] ."'>" .$data['city_name'] ."</option>";
					} else {
						//echo "<option value='" . $piece->id . "'>" . $piece->name . "</option>";
						echo '<option value="' . $piece->id . '">' . $piece->name . '</option>';
					}
				}
				?>
			</select>
		</div>
		<input class="btn" type="submit" name="btnEnvia" value="Ver">
		<input class="btn" type="hidden" name="goback" value="<?php echo $_SERVER['REQUEST_URI'] ?>">

	</form>
<?php else : ?>
	<p>Não tem culturas atribuidas.</p>
<?php endif; ?>
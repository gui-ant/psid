<!DOCTYPE html>
<html>

<body>

	Dados da Cultura
	<?php
	$selectedOption = $_POST['culture_id'];
	$url = "http://localhost/psid/php/dbGetStorProcData.php?sp=spGetCultureById&p=" .$selectedOption. "&json=true";
	$client = curl_init($url);
	curl_setopt($client, CURLOPT_RETURNTRANSFER, true);
	$response = curl_exec($client);
	$pieces = json_decode($response);
	?>
</body>
<form action="bd_UpdateCliente.php" method="POST">
	<br>
	ID cultura: <input type="text" name="name" value="<?= $pieces[0]->id; ?>" readonly>
	Nome cultura: <input type="text" name="nome_cliente" value="<?= $pieces[0]->name ?>"><br>
	<input type="hidden" name="goback" value="<?php echo $_POST['goback']; ?>">
</form>

</html>
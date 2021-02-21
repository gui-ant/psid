<!DOCTYPE html>
<html>
<body>

Edição de Cliente
<?php
	$selectedOption = $_GET['nitens'];
	$url = "http://localhost/ClienteExemplo/bd_GetCliente.php?where=&json=false";
	$client = curl_init($url);
	curl_setopt($client,CURLOPT_RETURNTRANSFER,true);
	$response = curl_exec($client);	
	$pieces = explode(";", $response);
	for ($i = 0; $i <= sizeof($pieces)-2; $i++) {
		$str=$pieces[$i];
		$str2=  explode(",",$str);
		$str3 = $str2[1];
		$str5 = $str2[0];
		$str4=  explode(":",$str3);	
		$str6=  explode(":",$str5);	
		if ($str6[1]==$selectedOption)
		{
		echo $str4[1]; 
		$i=sizeof($pieces)-2;
		}
	}
?>
</body>	
<form action="bd_UpdateCliente.php" method="POST">
<br>
Número Cliente: <input type="text" name="numero_cliente" value="<?=$selectedOption;?>" readonly >
Nome Cliente: <input type="text" name="nome_cliente" value="<?=$str4[1]?>"><br>
<input type="hidden" name="goback" value="<?php echo $_GET['goback']; ?>">
<input type="Submit" value="Alterar"> 
</form>
</html>
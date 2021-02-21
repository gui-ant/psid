<!DOCTYPE html>
<html>
<body>

<?php
	$url = "http://localhost/ClienteExemplo/bd_GetCliente.php?where=&json=false";
	$client = curl_init($url);
	curl_setopt($client,CURLOPT_RETURNTRANSFER,true);
	$response = curl_exec($client);		
	$pieces = explode(";", $response);
	echo '<form name="Combobox" action="ui_AlteraCliente.php" method="GET">';
	echo '<select name="nitens" id="itens">
	<option value=" " selected="selected">Escolha um Cliente:</option>';
	for ($i = 0; $i <= sizeof($pieces)-2; $i++) {
		$str=$pieces[$i];
		$str2=  explode(",",$str);
		$str3 = $str2[1];
		$str5 = $str2[0];
		$str4=  explode(":",$str3);	
		$str6=  explode(":",$str5);			
		$nomeItem = $elemento['Nome_Cliente'];
		echo '<option value="'.$str6[1].'">'.$str4[1].'</option>'; 
	}
	echo '</select>';
	echo '<input type="submit" name="btnEnvia" value="Editar">';
	echo '<input type="hidden" name="goback" value="'. $_SERVER['REQUEST_URI']. '">';
	echo '</form>';			
?>
</body>		
</html>






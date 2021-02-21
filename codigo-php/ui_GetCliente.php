<!DOCTYPE html>
<html>
<body>
<form name="form" action="" method="get">
Nome Cliente:<input name="nome" type="text"/>
Json (true/false):<input name="json" type="text"/>
<input type="submit" name="select" value="select" />
</form>
<?php
		if(!empty($_GET['nome'])) $param = $_GET['nome'];
		else $param = '';	
		if(!empty($_GET['json'])) $json = $_GET['json'];
		else $json = 'false';										
		select($param, $json);		
	function select($param, $json){
		$url = "http://localhost/ClienteExemplo/bd_GetCliente.php?where=".$param."&json=".$json;
		$client = curl_init($url);
		curl_setopt($client,CURLOPT_RETURNTRANSFER,true);
		$response = curl_exec($client);		
		echo "<br>";
		echo "<br>";
		echo $response;		
}
?>
</body>		
</html>

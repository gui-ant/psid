<?php
header("Content-Type:application/json");

if(!empty($_POST['numero_cliente'])){
	$numero_cliente=$_POST['numero_cliente'];
	if(!empty($_POST['nome_cliente']))
		$nome_cliente=$_POST['nome_cliente'];
	else $nome_cliente= " ";
	$result=edit_data($numero_cliente, $nome_cliente);	
	echo $result . " records updated."; 
}
else echo "Missing Pamameter";	
header('Location:'.$_POST['goback']);
exit();
			
function edit_data($numero_cliente, $nome_cliente)
{
	$url="127.0.0.1";
	$database="hotel";
	$username="root";
	$password="";
	$number_updates = 0;

	$conn = mysqli_connect($url, $username, $password, $database);
	if ($conn){		
		$sql = "update cliente set Nome_Cliente= '$nome_cliente' where Numero_cliente=$numero_cliente;";
		$result = mysqli_query($conn, $sql);
		$rows = array();			
		$res = mysqli_query($conn, $sql);
			if(!$res){
				$result = new stdClass();
				$result->status = false;
				$result->msg = mysqli_error($conn);
				echo mysqli_error($conn);
				exit;
			}
			$number_updates = 1;
		
	mysqli_close ($conn);
	}
	return $number_updates;
}
	
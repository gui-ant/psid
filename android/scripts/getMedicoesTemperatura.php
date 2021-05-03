	<?php
	$url="127.0.0.1";
	$database="pisid2021"; // Alterar nome da BD se necessario
    $conn = mysqli_connect($url,$_POST['username'],$_POST['password'],$database);
	// Completar o select para ir buscar a hora e leituras de temperatura da tabela Medicao dos ultimos 5 minutos
	$sql = "SELECT ... where ... AND Hora >= now() - interval 5 minute ORDER BY Hora ASC";
	$result = mysqli_query($conn, $sql);
	$response["medicoes"] = array();
	if ($result){
		if (mysqli_num_rows($result)>0){
			while($r=mysqli_fetch_assoc($result)){
				$ad = array();
				// Adicionar ao array a hora e leitura
				$ad["Hora"] = $r['Hora'];
				$ad["Leitura"] = $r['Leitura'];
				array_push($response["medicoes"], $ad);
			}
		}	
	}
	$json = json_encode($response["medicoes"]);
	echo $json;
	mysqli_close ($conn);
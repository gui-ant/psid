	<?php
	$url="127.0.0.1";
	$database="g07_local"; // Alterar nome da BD se necessario	
    $conn = mysqli_connect($url,$_POST['username'],$_POST['password'],$database);	
	// Alterar nome da tabela Alerta e nome do campo Hora se necessario
	$sql = "SELECT * from alerts as a, culture_params as cp, cultures as c where DATE(a.created_at) = '" . $_POST['date'] . "';";	
	$result = mysqli_query($conn, $sql);
	$response["avisos"] = array();
	if ($result){
		if (mysqli_num_rows($result)>0){
			while($r=mysqli_fetch_assoc($result)){
				$ad = array();
				// Completar com todos os campos da tabela alerta
				$ad["Zona"] = $r['Zona'];
				$ad["Sensor"] = $r['Sensor'];
				$ad["Hora"] = $r['Hora'];
				$ad["Leitura"] = $r['Leitura'];
				...
				array_push($response["avisos"], $ad);
			}
		}	
	}
	$json = json_encode($response["avisos"]);
	echo $json;
	mysqli_close ($conn);
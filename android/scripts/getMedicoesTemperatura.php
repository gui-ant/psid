	<?php
	$url="127.0.0.1";
	$database="g07_local"; // Alterar nome da BD se necessario
	
	$url2="194.210.86.10";
	$database2="aluno_g07_cloud";
	
   // $conn = mysqli_connect($url,$_POST['username'],$_POST['password'],$database);
	$conn = mysqli_connect($url,'root','',$database);
	
	$conn_cloud = mysqli_connect($url2,'aluno','aluno',$database2);
	$get_temp_ids_sql="SELECT id FROM sensors where substring(name,1,1) = 'T'";
	$ids_res = mysqli_query($conn_cloud, $get_temp_ids_sql);
	
	$array_id = array();
	while($sns_id=mysqli_fetch_assoc($ids_res)){
		// Adicionar ao array o id de sensor de temperatura
		array_push($array_id, $sns_id['id']);
	}
	

	
	// Completar o select para ir buscar a hora e leituras de temperatura da tabela Medicao dos ultimos 5 minutos
	$sql = "SELECT date, value FROM measurements where sensor_id IN (".Join(",",$array_id).") AND date >= now() - interval 5 minute ORDER BY date ASC";
	echo $sql;
	$result = mysqli_query($conn, $sql);
	$response["medicoes"] = array();
	if ($result){
		if (mysqli_num_rows($result)>0){
			while($r=mysqli_fetch_assoc($result)){
				$ad = array();
				// Adicionar ao array a hora e leitura
				$ad["date"] = $r['date'];
				$ad["value"] = $r['value'];
				array_push($response["medicoes"], $ad);
			}
		}	
	}
	$json = json_encode($response["medicoes"]);
	echo $json;
	mysqli_close ($conn);
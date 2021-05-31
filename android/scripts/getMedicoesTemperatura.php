	<?php
	$url="127.0.0.1";
	$database="g07_local"; // BD local 
	
	$url2="194.210.86.10"; // BD cloud com dados dos sensores
	$database2="aluno_g07_cloud";
	
	$conn = mysqli_connect($url,'root','',$database);

	
	
	$conn_cloud = mysqli_connect($url2,'aluno','aluno',$database2);

	
	
	// query que devolve zonas disponiveis para o user, i.e., Zonas onde existem culturas associadas a este user
	$get_temp_zones = "SELECT DISTINCT cultures.zone_id FROM cultures WHERE cultures.id IN (SELECT culture_users.culture_id FROM culture_users WHERE culture_users.user_id = (SELECT users.id FROM users WHERE users.email = '".$_POST['username']."'))";
	$zones_ids = mysqli_query($conn, $get_temp_zones);
	
	$zones_array = array();	
	while($aux_zone=mysqli_fetch_assoc($zones_ids)){
		// Adicionar ao array os ids das zonas disponíveis para este user
		array_push($zones_array, $aux_zone['zone_id']);
	}
	
	
	

	
	$selected_zone_id = array_values($zones_array)[0];
	if($_POST['selectedZone']!='-1'){
		$selected_zone_id = $_POST['selectedZone'];
	}
	
	//
	// INJECTAR NA QUERY ABAIXO PARAMETRO DE ENTRADA VINDO DO ANDROID!!!
	//
	
	// query vai buscar os ids dos sensores de temperatura e adiciona-os ao $array_id
	$get_temp_ids_sql="SELECT id FROM sensors where name = 'T".$selected_zone_id."'";
	$ids_res = mysqli_query($conn_cloud, $get_temp_ids_sql);
	
	$array_id = array();
	while($sns_id=mysqli_fetch_assoc($ids_res)){
		// Adicionar ao array o id de sensor de temperatura
		array_push($array_id, $sns_id['id']);
	}
	
	// Completar o select para ir buscar a hora e leituras de temperatura da tabela Medicao dos ultimos 5 minutos
	$sql = "SELECT date, value FROM measurements where sensor_id IN (".Join(",",$array_id).") AND date >= now() - interval 5 minute ORDER BY date ASC";
	$result = mysqli_query($conn, $sql);
	$response["medicoes"] = array();
	if ($result){
		if (mysqli_num_rows($result)>0){
			while($r=mysqli_fetch_assoc($result)){
				$ad = array();
				// Adicionar ao array a hora e leitura
				$ad["zonas"] = $zones_array;
				$ad["date"] = $r['date'];
				$ad["value"] = $r['value'];				
				array_push($response["medicoes"], $ad);
			}
		}	
	}
	$json = json_encode($response["medicoes"]);
	echo $json;
	mysqli_close ($conn);
	mysqli_close ($conn_cloud);
	<?php
	$url="127.0.0.1";
	$database="g07_local"; // Alterar nome da BD se necessario	
    $conn = mysqli_connect($url,'root','',$database);	
	
	// Alterar nome da tabela Alerta e nome do campo Hora se necessario
	//$sql = "SELECT alerts.created_at, alerts.message FROM alerts, rel_culture_params_set  where DATE(a.created_at) = '" . $_POST['date'] . "';";	
	
	
		$sql = "SELECT alerts.created_at, alerts.message 
FROM alerts 
WHERE ( (DATE(alerts.created_at) = '".$_POST['date']."') AND (	
    alerts.parameter_set_id IN (
		SELECT id 
		FROM `culture_params_sets` 
		WHERE culture_params_sets.culture_id IN (
			SELECT culture_users.culture_id 
			FROM culture_users, users 
			WHERE culture_users.user_id = users.id AND users.email = '".$_POST['username']."'
		)
	)

OR 
(alerts.parameter_set_id IS NULL AND (
        alerts.param_id IN(
        	SELECT culture_params.id
            FROM culture_params
            WHERE culture_params.id IN(
            	SELECT rel_culture_params_set.culture_param_id
                FROM rel_culture_params_set
                WHERE rel_culture_params_set.set_id IN(
                	SELECT culture_params_sets.id
                    FROM culture_params_sets
                    WHERE culture_params_sets.culture_id IN(
                    	SELECT culture_users.culture_id
                        FROM culture_users
                        WHERE culture_users.user_id IN(
                        	SELECT users.id
                            FROM users
                            WHERE users.email = '".$_POST['username']."'
                        )
                    )
                )
            )
        )
    )
)))";	
	
	
	
	
	$result = mysqli_query($conn, $sql);
	$response["avisos"] = array();
	if ($result){
		if (mysqli_num_rows($result)>0){
			while($r=mysqli_fetch_assoc($result)){
				$ad = array();
				// Completar com todos os campos da tabela alerta
				$ad["created_at"] = $r['created_at'];
				$ad["message"] = $r['message'];
				array_push($response["avisos"], $ad);
			}
		}	
	}
	$json = json_encode($response["avisos"]);
	
	echo $json;
	mysqli_close ($conn);


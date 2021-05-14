	<?php
	$url="127.0.0.1";
	$database="g07_local"; // Alterar nome da BD se necessario
    $conn = mysqli_connect($url,$_POST['username'],$_POST['password'],$database);
	$query_select = "SELECT current_role() as role";
	$result_query_select = mysqli_query($conn, $query_select); 
	$conn->next_result();
	$role = mysqli_fetch_assoc($result_query_select)['role'];
	if($role == "group_researcher") { // Alterar role se necessario
		$response["valid"] = array();
		array_push($response["valid"],"boa!");
		$json = json_encode($response["valid"]);
		echo $json;
	}
	$result_query_select->close();
	mysqli_close ($conn);
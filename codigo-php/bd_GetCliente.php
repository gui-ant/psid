<?php
header("Content-Type:application/json");
	if(!empty($_GET['where'])){
		$whereclause=$_GET['where'];
	}
	else{
		$whereclause='';
	}	
	if(!empty($_GET['json'])){
		$json=$_GET['json'];
	}
	else{
		$json='false';
	}		
	$data = get_data($whereclause,$json);	
	if(empty($data)){
		echo 'Data Not Found';
	}
	else{
		echo $data;		
	}
		

function get_data($whereclause, $json)
{
	$url="127.0.0.1";
	$database="hotel";
	$username="root";
	$password="";
	$conn = mysqli_connect($url, $username, $password, $database);
	if (!$conn){
		die ("Connection Failled: ".$conn->connect_error);
	}
	$sql = "call sp_GetCliente("."'%".$whereclause."%'".")";
	$result = mysqli_query($conn, $sql);
	$rows = array();
	if ($result) {		
		if ($json=="true"){
			if (mysqli_num_rows($result)>0){
				while($r=mysqli_fetch_assoc($result)){
					array_push($rows, $r);
				}	
			}	
		}
	}
	mysqli_close ($conn);
	if ($json=="true"){	
		return json_encode($rows);
	}		
	else {	
		$str="";
		while ($row = $result->fetch_assoc()) {		
			foreach($row as $column => $value) {
				$str = $str.$column.":".$value.", ";
			}
			$str = $str.";\n";
		}
		return ($str);
	}	
}	
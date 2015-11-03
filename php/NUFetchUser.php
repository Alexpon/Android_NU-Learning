<?php
	$con = mysqli_connect("address", "un", "pw", "db");
	
	$password = $_POST["password"];
	$username = $_POST["username"];
	
	$statement = mysqli_prepare($con, "SELECT * FROM table WHERE username = ? AND password = ?");	
	mysqli_stmt_bind_param($statement, "ss",  $username, $password);
	mysqli_stmt_execute($statement);
	
	mysqli_stmt_store_result($statement);
	mysqli_stmt_bind_result($statement, $id, $username, $password, $name, $department, $email);
	
	$student = array();
	
	while (mysqli_stmt_fetch($statement)) {
		$student[] = array("name" => $name, "department" => $department, "email" => $email);
	}
	
	echo json_encode($student);
	
	mysqli_stmt_close($statement);
	
	mysqli_close($con);
?>

	

<meta http-equiv="content-type" content="text/html; charset=utf-8">

<?php
	$con = mysqli_connect("address", "un", "pw", "db");

	$name = $_POST["name"];
	$username = $_POST["username"];
	$password = $_POST["password"];
	$department = $_POST["department"];
	$email = $_POST["email"];
	
	$statement = mysqli_prepare($con, "INSERT INTO table (username, password, name, department, email) VALUES (?, ?, ?, ?, ?)");
	mysqli_stmt_bind_param($statement, "sssss", $username, $password, $name, $department, $email);
	mysqli_stmt_execute($statement);
	mysqli_stmt_close($statement);
	mysqli_close($con);
?>
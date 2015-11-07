<?php
	$con = mysqli_connect("localhost", "root", "atms62x07ship", "nu_learning");
	
	$student_id = $_POST["student_id"];
	$student_name = $_POST["student_name"];
	$department = $_POST["department"];
	$year = $_POST["year"];
	$month = $_POST["month"];
	$date = $_POST["date"];
		
	$day=$year.'-'.$month.'-'.$date;
	$statement = mysqli_prepare($con, "INSERT INTO nu_nfc_quiz (student_id, student_name, department, quiz_date) 
			VALUES ('$student_id', '$student_name', '$department', '$day')");

	mysqli_stmt_execute($statement);
	mysqli_stmt_close($statement);
	mysqli_close($con);
?>
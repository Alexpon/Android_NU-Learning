<?php
	$con = mysqli_connect("address", "un", "pw", "db");
	
	$student_id = $_POST["student_id"];
	$student_name = $_POST["student_name"];
	$department = $_POST["department"];
	$class_name = $_POST["class_name"];
	$year = $_POST["year"];
	$month = $_POST["month"];
	$date = $_POST["date"];
	$hour = $_POST["hour"];
	$minute = $_POST["minute"];
	$second = $_POST["second"];
		
	$day=$year.'-'.$month.'-'.$date;
	$time=$hour.':'.$minute.':'.$second;
	$statement = mysqli_prepare($con, "INSERT INTO table (student_id, student_name, department, class_name, date, time) 
			VALUES ('$student_id', '$student_name', '$department', '$class_name', '$day', '$time')");

	mysqli_stmt_execute($statement);
	mysqli_stmt_close($statement);
	mysqli_close($con);
?>
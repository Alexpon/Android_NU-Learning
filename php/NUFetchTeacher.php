<?php
	$con = mysqli_connect("address", "un", "pw", "db");
	
	$teacherID = $_POST["teacher_id"];
	
	$statement = mysqli_prepare($con, "SELECT * FROM table WHERE teacher_id = ?");	
	mysqli_stmt_bind_param($statement, "s",  $teacherID);
	mysqli_stmt_execute($statement);
	
	mysqli_stmt_store_result($statement);
	mysqli_stmt_bind_result($statement, $id, $teacher_id, $teacher_name, $question_num);
	
	$quiz = array();
	
	while (mysqli_stmt_fetch($statement)) {
		$quiz[] = array("question" => $question_num);
	}
	
	echo json_encode($quiz);
	
	mysqli_stmt_close($statement);
	
	mysqli_close($con);
?>

	

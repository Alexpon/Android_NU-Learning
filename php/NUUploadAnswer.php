<?php
	$con = mysqli_connect("localhost", "root", "atms62x07ship", "nu_learning");
	
	$student_id = $_POST["student_id"];
	$year = $_POST["year"];
	$month = $_POST["month"];
	$date = $_POST["date"];
	$question_no = $_POST["question_no"];
	$answer = $_POST["answer"];
	$str = 'question_';
	$index = $str.$question_no;
	
	$day=$year.'-'.$month.'-'.$date;
	$statement = mysqli_prepare($con, "UPDATE nu_nfc_quiz SET `$index` = '$answer' WHERE `student_id` = '$student_id' AND `quiz_date` = '$day'");

	mysqli_stmt_execute($statement);
	mysqli_stmt_close($statement);
	mysqli_close($con);
?>
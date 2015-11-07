<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<html>
<head>
<style>
h1{font-family:"Comic Sans MS";
width:200px;}
h2{font-family:"Comic Sans MS";
width:200px;}
h3{font-family:"Comic Sans MS";
width:200px;}
</style>
</head>
<body align="center" >

	
	<table border="1" align="center" style="border: 5px  dashed rgb(109, 2, 107); background-color: rgb(255, 255, 255);">
		<tr>
		<td><h3>學號</h3></td><td><h3>姓名</h3></td><td><h3>系級</h3></td><td><h3>考試時間</h3></td>
		<td><h3>Q1</h3></td><td><h3>Q2</h3></td><td><h3>Q3</h3></td><td><h3>Q4</h3></td><td><h3>Q5</h3></td>
		</tr>
		
<?php	
		include("conf.php");
		$result = mysql_query("SELECT * FROM `table`");
		
		while($row = mysql_fetch_row($result) ){
			print "<tr><td><h3>".$row[3]."</h3></td><td><h3>".$row[4].
			"</h3></td><td><h3>".$row[5]."</h3></td><td><h3>".$row[6].
			"</h3></td><td><h3>".$row[7]."</h3></td><td><h3>".$row[8].
			"</h3></td><td><h3>".$row[9]."</h3></td><td><h3>".$row[10].
			"</h3></td><td><h3>".$row[11]."</h3></td></tr> ";
		}
?>

  </body>
  <meta content='3'; url=http://address/print' http-equiv='refresh'>
</html>
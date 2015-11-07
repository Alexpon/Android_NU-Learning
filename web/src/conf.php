<?php
//新版
$host="";
$user="";
$upwd="";
$db="";

$link=mysql_connect($host,$user,$upwd) or die ("Unable to connect!");
mysql_select_db($db, $link) or die ("Unable to select database!");

?>
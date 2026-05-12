<?php
// 模拟请求参数
$_GET['id'] = 1;
$_POST['name'] = 'test';

// 1. mysql/mysqli_query 风险
$conn = mysqli_connect("localhost", "root", "123456", "test");
mysql_query("SELECT * FROM user WHERE id = {$_GET['id']}"); // 触发 mysql_query 规则
mysqli_query($conn, "SELECT * FROM user WHERE name = '{$_POST['name']}'"); // 触发 mysqli_query 规则

// 2. SQL 字符串拼接（SELECT/INSERT/UPDATE + $_GET/$_POST）
$sql1 = "SELECT * FROM user WHERE id = " . $_GET['id']; // 触发 SELECT + $_GET 规则
$sql2 = "SELECT * FROM user WHERE name = '" . $_POST['name'] . "'"; // 触发 SELECT + $_POST 规则
$sql3 = "INSERT INTO user(name) VALUES ('" . $_GET['id'] . "')"; // 触发 INSERT + $_GET 规则
$sql4 = "UPDATE user SET age = 20 WHERE id = " . $_POST['name']; // 触发 UPDATE + $_POST 规则

// 3. PDO 相关风险
$pdo = new PDO("mysql:host=localhost;dbname=test", "root", "123456");
$pdo->exec("DELETE FROM user WHERE id = {$_GET['id']}"); // 触发 PDO::exec 规则
$stmt = $pdo->prepare("SELECT * FROM user WHERE id = ?");
$stmt->execute(); // 触发 PDOStatement::execute() 规则

// 4. mysqli_real_escape_string （虽有转义但仍可能存在风险）
$unsafe = $_GET['id'];
$safe = mysqli_real_escape_string($conn, $unsafe); // 触发 mysqli_real_escape_string 规则
mysqli_query($conn, "SELECT * FROM user WHERE id = $safe");

mysqli_close($conn);
?>
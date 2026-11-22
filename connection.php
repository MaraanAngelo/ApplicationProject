<?php
$host = 'localhost';
$user = 'root';
$password = '';
$db = 'todo_listdb';

$conn = @mysqli_connect($host, $user, $password, $db);

if (!$conn) {
    header("Content-Type: application/json");
    echo json_encode([
        "success" => false,
        "message" => "Database connection failed: " . mysqli_connect_error()
    ]);
    exit;
}
?>

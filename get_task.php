<?php
header("Content-Type: application/json");
include 'connection.php';

$sql = "SELECT id, title, description, category_id, priority, due_date, due_time, status, created_at, updated_at FROM tasks ORDER BY due_date ASC, due_time ASC";
$result = mysqli_query($conn, $sql);

if ($result) {
    $tasks = [];
    while ($row = mysqli_fetch_assoc($result)) {
        $row['id'] = (int)$row['id'];
        $row['category_id'] = $row['category_id'] !== null ? (int)$row['category_id'] : null;
        $tasks[] = $row;
    }
    echo json_encode($tasks);
} else {
    echo json_encode([
        "success" => false,
        "message" => mysqli_error($conn)
    ]);
}

mysqli_close($conn);
?>

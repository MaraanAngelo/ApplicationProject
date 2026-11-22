<?php
header("Content-Type: application/json");
include 'connection.php';

$title = trim($_POST['title'] ?? '');
$description = trim($_POST['description'] ?? '');
$category_id = (!empty($_POST['category_id']) && is_numeric($_POST['category_id']) && intval($_POST['category_id']) > 0) ? intval($_POST['category_id']) : null;
$priority = $_POST['priority'] ?? 'Medium';
$due_date = trim($_POST['due_date'] ?? '');
$due_time = (!empty($_POST['due_time']) && trim($_POST['due_time']) !== '') ? trim($_POST['due_time']) : null;
$status = $_POST['status'] ?? 'Pending';

if (empty($title) || empty($due_date)) {
    echo json_encode([
        "success" => false,
        "message" => "Title and due date are required."
    ]);
    exit;
}

$sql = "INSERT INTO tasks (title, description, category_id, priority, due_date, due_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
$stmt = mysqli_prepare($conn, $sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => mysqli_error($conn)
    ]);
    exit;
}

mysqli_stmt_bind_param(
    $stmt,
    "ssissss",
    $title,
    $description,
    $category_id,
    $priority,
    $due_date,
    $due_time,
    $status
);

if (mysqli_stmt_execute($stmt)) {
    echo json_encode([
        "success" => true,
        "message" => "Task added successfully!",
        "task_id" => (int)mysqli_insert_id($conn)
    ]);
} else {
    echo json_encode([
        "success" => false,
        "message" => mysqli_stmt_error($stmt)
    ]);
}

mysqli_stmt_close($stmt);
mysqli_close($conn);
?>

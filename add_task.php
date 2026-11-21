
<?php

header("Content-Type: application/json");

include 'connection.php';

// Receive data from Android or test form
$title = $_POST['title'] ?? '';
$description = $_POST['description'] ?? '';
$category_id = $_POST['category_id'] ?? null;
$priority = $_POST['priority'] ?? 'Medium';
$due_date = $_POST['due_date'] ?? '';
$due_time = $_POST['due_time'] ?? null;

// Check required fields
if (empty($title) || empty($due_date)) {
    echo json_encode([
        "success" => false,
        "message" => "Title and due date are required."
    ]);
    exit;
}

// Insert task into database
$sql = "INSERT INTO tasks
        (title, description, category_id, priority,
         due_date, due_time, status)
        VALUES (?, ?, ?, ?, ?, ?, 'Pending')";

$stmt = mysqli_prepare($conn, $sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => mysqli_error($conn)
    ]);
    exit;
}

// Bind parameters
mysqli_stmt_bind_param(
    $stmt,
    "ssisss",
    $title,
    $description,
    $category_id,
    $priority,
    $due_date,
    $due_time
);

if (mysqli_stmt_execute($stmt)) {

    echo json_encode([
        "success" => true,
        "message" => "Task added successfully!",
        "task_id" => mysqli_insert_id($conn)
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

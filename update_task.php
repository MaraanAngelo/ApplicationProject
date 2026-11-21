
<?php

header("Content-Type: application/json");

include 'connection.php';

// Receive data
$id = $_POST['id'] ?? '';
$title = $_POST['title'] ?? '';
$description = $_POST['description'] ?? '';
$category_id = $_POST['category_id'] ?? null;
$priority = $_POST['priority'] ?? 'Medium';
$due_date = $_POST['due_date'] ?? '';
$due_time = $_POST['due_time'] ?? null;

// Check required fields
if (empty($id) || empty($title) || empty($due_date)) {
    echo json_encode([
        "success" => false,
        "message" => "Task ID, title, and due date are required."
    ]);
    exit;
}

// Update task
$sql = "UPDATE tasks SET
        title = ?,
        description = ?,
        category_id = ?,
        priority = ?,
        due_date = ?,
        due_time = ?,
        updated_at = CURRENT_TIMESTAMP
        WHERE id = ?";

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
    "ssisssi",
    $title,
    $description,
    $category_id,
    $priority,
    $due_date,
    $due_time,
    $id
);

// Execute update
if (mysqli_stmt_execute($stmt)) {

    if (mysqli_stmt_affected_rows($stmt) >= 0) {
        echo json_encode([
            "success" => true,
            "message" => "Task updated successfully!"
        ]);
    }

} else {

    echo json_encode([
        "success" => false,
        "message" => mysqli_stmt_error($stmt)
    ]);

}

mysqli_stmt_close($stmt);
mysqli_close($conn);

?>

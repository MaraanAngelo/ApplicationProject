<?php
header("Content-Type: application/json");
include 'connection.php';

$id = $_POST['id'] ?? '';
$title = trim($_POST['title'] ?? '');
$description = trim($_POST['description'] ?? '');
$category_id = (!empty($_POST['category_id']) && is_numeric($_POST['category_id']) && intval($_POST['category_id']) > 0) ? intval($_POST['category_id']) : null;
$priority = $_POST['priority'] ?? 'Medium';
$due_date = trim($_POST['due_date'] ?? '');
$due_time = (!empty($_POST['due_time']) && trim($_POST['due_time']) !== '') ? trim($_POST['due_time']) : null;
$status = $_POST['status'] ?? null;

if (empty($id) || !is_numeric($id) || $id <= 0 || empty($title) || empty($due_date)) {
    echo json_encode([
        "success" => false,
        "message" => "Task ID, title, and due date are required."
    ]);
    exit;
}

if ($status !== null && in_array($status, ['Pending', 'Completed'])) {
    $sql = "UPDATE tasks SET title = ?, description = ?, category_id = ?, priority = ?, due_date = ?, due_time = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    $stmt = mysqli_prepare($conn, $sql);
    if (!$stmt) {
        echo json_encode(["success" => false, "message" => mysqli_error($conn)]);
        exit;
    }
    mysqli_stmt_bind_param($stmt, "ssissssi", $title, $description, $category_id, $priority, $due_date, $due_time, $status, $id);
} else {
    $sql = "UPDATE tasks SET title = ?, description = ?, category_id = ?, priority = ?, due_date = ?, due_time = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    $stmt = mysqli_prepare($conn, $sql);
    if (!$stmt) {
        echo json_encode(["success" => false, "message" => mysqli_error($conn)]);
        exit;
    }
    mysqli_stmt_bind_param($stmt, "ssisssi", $title, $description, $category_id, $priority, $due_date, $due_time, $id);
}

if (mysqli_stmt_execute($stmt)) {
    echo json_encode([
        "success" => true,
        "message" => "Task updated successfully!"
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

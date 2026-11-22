<?php
header("Content-Type: application/json");
include 'connection.php';

$id = $_POST['id'] ?? '';

if (empty($id) || !is_numeric($id) || $id <= 0) {
    echo json_encode([
        "success" => false,
        "message" => "A valid task ID is required."
    ]);
    exit;
}

mysqli_begin_transaction($conn);

try {
    $reminder_sql = "DELETE FROM reminders WHERE task_id = ?";
    $reminder_stmt = mysqli_prepare($conn, $reminder_sql);
    if ($reminder_stmt) {
        mysqli_stmt_bind_param($reminder_stmt, "i", $id);
        mysqli_stmt_execute($reminder_stmt);
        mysqli_stmt_close($reminder_stmt);
    }

    $task_sql = "DELETE FROM tasks WHERE id = ?";
    $task_stmt = mysqli_prepare($conn, $task_sql);
    if (!$task_stmt) {
        throw new Exception(mysqli_error($conn));
    }

    mysqli_stmt_bind_param($task_stmt, "i", $id);
    mysqli_stmt_execute($task_stmt);

    if (mysqli_stmt_affected_rows($task_stmt) > 0) {
        mysqli_commit($conn);
        echo json_encode([
            "success" => true,
            "message" => "Task deleted successfully!"
        ]);
    } else {
        mysqli_rollback($conn);
        echo json_encode([
            "success" => false,
            "message" => "Task not found."
        ]);
    }
    mysqli_stmt_close($task_stmt);

} catch (Exception $e) {
    mysqli_rollback($conn);
    echo json_encode([
        "success" => false,
        "message" => $e->getMessage()
    ]);
}

mysqli_close($conn);
?>

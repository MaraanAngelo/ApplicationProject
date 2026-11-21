
<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
header("Content-Type: application/json");
include 'connection.php';

$id = $_POST['id'] ?? '';

if (empty($id) || !is_numeric($id) || $id <= 0) {
    echo json_encode([
        "success" => false,
        "message" => "A valid category ID is required."
    ]);
    exit;
}

// Check if category is being used by tasks
$check_sql = "SELECT COUNT(*) AS total FROM tasks WHERE category_id = ?";
$check_stmt = mysqli_prepare($conn, $check_sql);

if (!$check_stmt) {
    echo json_encode([
        "success" => false,
        "message" => mysqli_error($conn)
    ]);
    exit;
}

mysqli_stmt_bind_param($check_stmt, "i", $id);
mysqli_stmt_execute($check_stmt);

$result = mysqli_stmt_get_result($check_stmt);
$row = mysqli_fetch_assoc($result);

mysqli_stmt_close($check_stmt);

if ($row['total'] > 0) {
    echo json_encode([
        "success" => false,
        "message" => "Cannot delete this category because it is being used by tasks."
    ]);
    mysqli_close($conn);
    exit;
}

// Delete category
$sql = "DELETE FROM categories WHERE id = ?";
$stmt = mysqli_prepare($conn, $sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => mysqli_error($conn)
    ]);
    exit;
}

mysqli_stmt_bind_param($stmt, "i", $id);

if (mysqli_stmt_execute($stmt)) {
    if (mysqli_stmt_affected_rows($stmt) > 0) {
        echo json_encode([
            "success" => true,
            "message" => "Category deleted successfully!"
        ]);
    } else {
        echo json_encode([
            "success" => false,
            "message" => "Category not found."
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

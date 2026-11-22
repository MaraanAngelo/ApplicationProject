<?php
header("Content-Type: application/json");
include 'connection.php';

$id = $_POST['id'] ?? '';
$category_name = trim($_POST['category_name'] ?? '');

if (empty($id) || !is_numeric($id) || $id <= 0 || empty($category_name)) {
    echo json_encode([
        "success" => false,
        "message" => "Valid category ID and category name are required."
    ]);
    exit;
}

$check_sql = "SELECT id FROM categories WHERE category_name = ? AND id != ?";
$check_stmt = mysqli_prepare($conn, $check_sql);
if (!$check_stmt) {
    echo json_encode(["success" => false, "message" => mysqli_error($conn)]);
    exit;
}

mysqli_stmt_bind_param($check_stmt, "si", $category_name, $id);
mysqli_stmt_execute($check_stmt);
mysqli_stmt_store_result($check_stmt);

if (mysqli_stmt_num_rows($check_stmt) > 0) {
    echo json_encode([
        "success" => false,
        "message" => "Category name already exists."
    ]);
    mysqli_stmt_close($check_stmt);
    mysqli_close($conn);
    exit;
}
mysqli_stmt_close($check_stmt);

$sql = "UPDATE categories SET category_name = ? WHERE id = ?";
$stmt = mysqli_prepare($conn, $sql);
if (!$stmt) {
    echo json_encode(["success" => false, "message" => mysqli_error($conn)]);
    exit;
}

mysqli_stmt_bind_param($stmt, "si", $category_name, $id);

if (mysqli_stmt_execute($stmt)) {
    echo json_encode([
        "success" => true,
        "message" => "Category updated successfully!"
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

<?php
header("Content-Type: application/json");
include 'connection.php';

$category_name = trim($_POST['category_name'] ?? '');

if (empty($category_name)) {
    echo json_encode([
        "success" => false,
        "message" => "Category name is required."
    ]);
    exit;
}

$check_sql = "SELECT id FROM categories WHERE category_name = ?";
$check_stmt = mysqli_prepare($conn, $check_sql);

if (!$check_stmt) {
    echo json_encode([
        "success" => false,
        "message" => mysqli_error($conn)
    ]);
    exit;
}

mysqli_stmt_bind_param($check_stmt, "s", $category_name);
mysqli_stmt_execute($check_stmt);
mysqli_stmt_store_result($check_stmt);

if (mysqli_stmt_num_rows($check_stmt) > 0) {
    echo json_encode([
        "success" => false,
        "message" => "Category already exists."
    ]);
    mysqli_stmt_close($check_stmt);
    mysqli_close($conn);
    exit;
}
mysqli_stmt_close($check_stmt);

$sql = "INSERT INTO categories (category_name) VALUES (?)";
$stmt = mysqli_prepare($conn, $sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => mysqli_error($conn)
    ]);
    exit;
}

mysqli_stmt_bind_param($stmt, "s", $category_name);

if (mysqli_stmt_execute($stmt)) {
    echo json_encode([
        "success" => true,
        "message" => "Category added successfully!",
        "category_id" => (int)mysqli_insert_id($conn)
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

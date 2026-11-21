
<?php
header("Content-Type: application/json");
include 'connection.php';

$sql = "SELECT id, category_name FROM categories ORDER BY category_name ASC";
$result = mysqli_query($conn, $sql);

if ($result) {
    $categories = [];

    while ($row = mysqli_fetch_assoc($result)) {
        $categories[] = $row;
    }

    echo json_encode($categories);
} else {
    echo json_encode([
        "success" => false,
        "message" => mysqli_error($conn)
    ]);
}

mysqli_close($conn);
?>

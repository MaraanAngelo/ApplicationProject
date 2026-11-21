<?php

header("Content-Type: application/json");

include 'connection.php';

$sql = "SELECT * FROM tasks ORDER BY due_date ASC";
$result = mysqli_query($conn, $sql);

$tasks = [];

if ($result) {

    while ($row = mysqli_fetch_assoc($result)) {
        $tasks[] = $row;
    }

    echo json_encode($tasks);

} else {

    echo json_encode([
        "error" => mysqli_error($conn)
    ]);

}

mysqli_close($conn);

?>

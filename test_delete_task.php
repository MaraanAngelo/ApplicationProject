
<!DOCTYPE html>
<html>
<head>
    <title>Test Delete Task</title>
</head>
<body>

    <h2>Delete a Task</h2>

    <form action="delete_task.php" method="POST">
        <label>Task ID:</label>
        <input type="number" name="id" required>

        <button type="submit">Delete Task</button>
    </form>

</body>
</html>


<!DOCTYPE html>
<html>
<head>
    <title>Test Update Task</title>
</head>
<body>

<h2>Update Task</h2>

<form action="update_task.php" method="POST">

    <label>Task ID:</label><br>
    <input type="number" name="id" required>
    <br><br>

    <label>Title:</label><br>
    <input type="text" name="title" required>
    <br><br>

    <label>Description:</label><br>
    <textarea name="description"></textarea>
    <br><br>

    <label>Category ID:</label><br>
    <input type="number" name="category_id" required>
    <br><br>

    <label>Priority:</label><br>
    <select name="priority">
        <option value="Low">Low</option>
        <option value="Medium">Medium</option>
        <option value="High">High</option>
    </select>
    <br><br>

    <label>Due Date:</label><br>
    <input type="date" name="due_date" required>
    <br><br>

    <label>Due Time:</label><br>
    <input type="time" name="due_time">
    <br><br>

    <button type="submit">Update Task</button>

</form>

</body>
</html>

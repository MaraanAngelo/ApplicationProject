
<form action="add_task.php" method="POST">

    <label>Title:</label>
    <input type="text" name="title" required>

    <br><br>

    <label>Description:</label>
    <textarea name="description"></textarea>

    <br><br>

    <label>Category ID:</label>
    <input type="number" name="category_id" value="1" required>

    <br><br>

    <label>Priority:</label>
    <select name="priority">
        <option value="Low">Low</option>
        <option value="Medium">Medium</option>
        <option value="High">High</option>
    </select>

    <br><br>

    <label>Due Date:</label>
    <input type="date" name="due_date" required>

    <br><br>

    <label>Due Time:</label>
    <input type="time" name="due_time">

    <br><br>

    <button type="submit">Add Task</button>

</form>


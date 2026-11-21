
<!DOCTYPE html>
<html>
<head>
    <title>Test Update Category</title>
</head>
<body>

    <h2>Update Category</h2>

    <form action="update_category.php" method="POST">

        <label>Category ID:</label>
        <input type="number" name="id" required>

        <br><br>

        <label>New Category Name:</label>
        <input type="text" name="category_name" required>

        <br><br>

        <button type="submit">Update Category</button>

    </form>

</body>
</html>

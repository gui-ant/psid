<?php include('server.php') ?>
<!DOCTYPE html>
<html>

<head>
    <title>PSID</title>
    <link rel="stylesheet" type="text/css" href="../style.css">
</head>

<body>
    <div class="header">
        <h2>Login</h2>
    </div>

    <form method="post" action="login.php">
        <?php if (count($errors) > 0) : ?>
            <div class="error">
                <?php foreach ($errors as $error) : ?>
                    <p><?php echo $error ?></p>
                <?php endforeach ?>
            </div>
        <?php endif ?>
        <div class="input-group">
            <label>Email</label>
            <input type="email" name="email" value="<?= isset($_SESSION['email']) ? $_SESSION['email'] : "" ?>" autocomplete="off">
        </div>
        <div class="input-group">
            <label>Password</label>
            <input type="password" name="pass" value="">
        </div>
        <div class="input-group">
            <button type="submit" class="btn" name="login_user">Login</button>
        </div>
        <p>
            <a href="register.php">Registar</a>
        </p>
    </form>
</body>

</html>
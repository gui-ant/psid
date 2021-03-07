<?php include('server.php') ?>
<!DOCTYPE html>
<html>

<head>
    <title>PSID</title>
    <link rel="stylesheet" type="text/css" href="../style.css">
</head>

<body>
    <div class="header">
        <h2>Registo</h2>
    </div>

    <form method="post" action="register.php">
        <?php if (count($errors) > 0) : ?>
            <div class="error">
                <?php foreach ($errors as $error) : ?>
                    <p><?php echo $error ?></p>
                <?php endforeach ?>
            </div>
        <?php endif ?>
        <div class="input-group">
            <label>Nome</label>
            <input type="text" name="name" value="<?php echo $username; ?>">
        </div>
        <div class="input-group">
            <label>Email</label>
            <input type="text" name="email" value="<?php echo $email; ?>">
        </div>
        <div class="input-group">
            <label>Password</label>
            <input type="password" name="pass">
        </div>
        <div class="input-group">
            <label>Confirmar password</label>
            <input type="password" name="pass_">
        </div>
        <div class="input-group">
            <button type="submit" class="btn" name="reg_user">Submeter</button>
        </div>
        <p>
           <a href="login.php">Iniciar sessão</a>
        </p>
    </form>
</body>

</html>
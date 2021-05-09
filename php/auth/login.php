<?php include('auth.php') ?>
<!DOCTYPE html>
<html>

<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-wEmeIV1mKuiNpC+IOBjI7aAzPcEZeedi5yW5f2yOq55WWLwNGmvvx4Um1vskeMj0" crossorigin="anonymous">
    <link href="../css/styles.css" rel="stylesheet" type="text/css" media="screen">
    <link id="u-theme-google-font" rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:100,100i,300,300i,400,400i,500,500i,700,700i,900,900i|Open+Sans:300,300i,400,400i,600,600i,700,700i,800,800i">
</head>

<body class="body-signin">
    <section class="form-signin">
        <div class="container">
            <form method="post" action="login.php">
                <h2 class="display-1">LOGIN</h2>
                <?php if (count($errors) > 0) : ?>
                    <?php foreach ($errors as $error) : ?>
                        <div class="alert alert-danger"><?= $error ?></div>
                    <?php endforeach ?>
                <?php endif ?>
                <div class="form-group">
                    <label for="emaiInput" class="form-label">Email</label>
                    <input type="email" name="email" value="<?= isset($_SESSION['email']) ? $_SESSION['email'] : "" ?>" class="form-control form-input" id="emaiInput" placeholder="Enter your e-mail">
                </div>
                <div class="form-group">
                    <label for="passwordInput" class="form-label">Password</label>
                    <input type="password" name="pass" value="" class="form-control form-input" id="passwordInput" placeholder="Enter your password">
                </div>
                <div class="d-grid col-4 mx-auto">
                    <button type="submit" class="btn btn-lg btn-primary mt-2" name="login_user">Login</button>
                </div>
            </form>
            <p class="mt-5 mb-3 text-muted text-center">&copy; PISID 2021 - G07</p>
        </div>
    </section>
</body>
<script class="u-script" type="text/javascript" src="../js/jquery.js" defer=""></script>

</html>
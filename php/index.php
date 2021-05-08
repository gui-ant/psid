<?php
include("db/config.php");
session_start();

if (!isset($_SESSION['user_name'])) {
    $_SESSION['msg'] = "Inicie sessão";
    header("location: auth/login.php");
}
if (isset($_GET['logout'])) {
    session_destroy();
    unset($_SESSION['user_name']);
    header("location: auth/login.php");
}
?>
<!DOCTYPE html>
<html>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home</title>
    <meta name="description" content="None">
    <meta name="author" content="">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-wEmeIV1mKuiNpC+IOBjI7aAzPcEZeedi5yW5f2yOq55WWLwNGmvvx4Um1vskeMj0" crossorigin="anonymous">
    <link rel="stylesheet" href="css/styles.css">
    <link id="u-theme-google-font" rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:100,100i,300,300i,400,400i,500,500i,700,700i,900,900i|Open+Sans:300,300i,400,400i,600,600i,700,700i,800,800i">
</head>

<body class="bg-dark">
    <nav class="navbar navbar-expand-lg navbar-dark fixed-top">
        <div class="container"><button type="button" data-toggle="collapse" data-target="#navbarResponsive" aria-controls="navbarResponsive" aria-expanded="false" aria-label="Toggle navigation" class="navbar-toggler"><span class="navbar-toggler-icon"></span></button>
            <div id="navbarResponsive" class="collapse navbar-collapse">
                <ul class="navbar-nav ml-auto">
                    <li class="nav-item"></li><a href="index.php" class="nav-link">Home</a>
                    <li class="nav-item"><a href="?logout='1'" class="nav-link">Logout</a></li>
                </ul>
            </div>
        </div>
    </nav>
    <div class="container">

        <?php if (isset($_SESSION['user_email'])) : ?>

            <div class="container-fluid py-5"></div>
            <h2 class="display-1">Welcome <b><?php echo $_SESSION['user_name']; ?></b></h2>

            <?php if ($_SESSION['user_role'] == 'group_researcher') : ?>
                <!-- Researcher View -->
                <p>
                    <?php include("cultures.php"); ?>
                </p>

            <?php elseif ($_SESSION['user_role'] == 'group_admin') : ?>
                <!-- Admin View -->
                <p class="u-align-center u-text u-text-2">Administration Panel</p>
            <?php elseif ($_SESSION['user_role'] == NULL) : ?>
                <!-- User with no roles View -->
                <p>
                    You don't have a profile yet. Please contact an administrator.
                </p>
            <?php endif ?>
        <?php endif ?>
    </div>

    <!--Bootstrap Scripts-->
    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js" integrity="sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js" integrity="sha384-OgVRvuATP1z7JjHLkuOU7Xw704+h835Lr+6QL9UvYjZE3Ipu6Tp75j7Bh/kR0JKI" crossorigin="anonymous"></script>
</body>

</html>
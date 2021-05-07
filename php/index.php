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
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css" integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk"
        crossorigin="anonymous">
    <link rel="stylesheet" href="css/styles_index.css">
    <meta name="theme-color" content="#2F3BA2">
</head>

<body>

<!--Bootstrap Scripts-->
    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js" integrity="sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous">
    </script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js" integrity="sha384-OgVRvuATP1z7JjHLkuOU7Xw704+h835Lr+6QL9UvYjZE3Ipu6Tp75j7Bh/kR0JKI" crossorigin="anonymous">
    </script>

    <nav class="navbar navbar-expand-lg navbar-dark fixed-top">
        <div class="container"><button type="button" data-toggle="collapse" data-target="#navbarResponsive" aria-controls="navbarResponsive" aria-expanded="false" aria-label="Toggle navigation"
                class="navbar-toggler"><span class="navbar-toggler-icon"></span></button>
            <div id="navbarResponsive" class="collapse navbar-collapse">
                <ul class="navbar-nav ml-auto">
                    <li class="nav-item"></li><a href="index.php" class="nav-link">Home</a>
                    <li class="nav-item"><a href="?logout='1'" class="nav-link">Logout</a></li>
                </ul>
            </div>
        </div>
    </nav>
  
        <!-- notification message -->
        <?php if (isset($_SESSION['success'])) : ?>
            <div style="color:#3c763d;background:#dff0d8;border:1px,solid,#3c763d;margin-bottom:20px;">
                <h3>
                    <?php
                    echo $_SESSION['success'];
                    unset($_SESSION['success']);
                    ?>
                </h3>
            </div>
        <?php endif ?>

        <?php if (isset($_SESSION['user_email'])) : ?>

            <div class="container-fluid py-5"></div>
                <h2 class="display-1">Welcome <?php echo $_SESSION['user_name']; ?></h2>
                <h1 id="ivk9j" class="text-light"></h1>
  
                    <?php if ($_SESSION['user_role'] == 'group_researcher') : ?>
                        <!-- Researcher View -->
                        <p>
                            <?php include("cultures.php");?>
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
                    </div>

            </section>
         <?php endif ?>
        
  </body>
</html>
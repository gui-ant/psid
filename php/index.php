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
    <title>Home</title>
    <link rel="stylesheet" type="text/css" href="style.css">
</head>

<body>

    <div class="header">
        <h2>Home</h2>
    </div>
    <div class="content">
        <!-- notification message -->
        <?php if (isset($_SESSION['success'])) : ?>
            <div class="error success">
                <h3>
                    <?php
                    echo $_SESSION['success'];
                    unset($_SESSION['success']);
                    ?>
                </h3>
            </div>
        <?php endif ?>

        <?php if (isset($_SESSION['user_email'])) : ?>
            <p>
                Bem-vindo <strong><?php echo $_SESSION['user_name']; ?></strong>&nbsp;<a href="?logout='1'">Sair</a>
            </p>


            <?php if ($_SESSION['user_role'] == 'group_researcher') : ?>
                <!-- Researcher View -->
                <p>
                    <?php include("cultures.php");?>
                </p>

            <?php elseif ($_SESSION['user_role'] == 'group_admin') : ?>
                <!-- Admin View -->
                <p>Painel de Admnistração</p>
            <?php elseif ($_SESSION['user_role'] == NULL) : ?>
                <!-- User with no roles View -->
                <p>
                    Ainda não tem perfil atribuído. Contacte um administrador.
                </p>
            <?php endif ?>
        <?php endif ?>
    </div>

</body>

</html>
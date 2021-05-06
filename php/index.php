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
<html style="font-size: 16px;">
  <head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="utf-8">
    <meta name="keywords" content="Udpate Culture">
    <meta name="description" content="">
    <meta name="page_type" content="np-template-header-footer-from-plugin">
    <title>Home</title>
    <link rel="stylesheet" href="nicepage.css" media="screen">
    <link rel="stylesheet" href="index-cultures.css" media="screen">
    <script class="u-script" type="text/javascript" src="jquery.js" defer=""></script>
    <script class="u-script" type="text/javascript" src="nicepage.js" defer=""></script>
    <meta name="generator" content="Nicepage 3.9.3, nicepage.com">
    <link id="u-theme-google-font" rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:100,100i,300,300i,400,400i,500,500i,700,700i,900,900i|Open+Sans:300,300i,400,400i,600,600i,700,700i,800,800i">
    
    <script type="application/ld+json">{
		"@context": "http://schema.org",
		"@type": "Organization",
		"name": "Home",
		"url": "index.html"
    }</script>
    <meta property="og:title" content="update culture">
    <meta property="og:type" content="website">
    <meta name="theme-color" content="#478ac9">
    <link rel="canonical" href="index.html">
    <meta property="og:url" content="index.html">
  </head>
  <body class="u-body u-palette-1-base">
  
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
            <section class="u-align-center u-clearfix u-grey-90 u-section-1" id="carousel_b2e3">

                <div class="u-clearfix u-sheet u-sheet-1">

                    <h2 class="u-text u-text-body-alt-color u-text-1">Welcome <?php echo $_SESSION['user_name']; ?></h2>
                    <a href="?logout='1'" class="u-black u-btn u-button-style u-hover-palette-1-dark-1 u-btn-1">Logout<br></a>
                    
                    
                    
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
                            Ainda não tem perfil atribuído. Contacte um administrador.
                        </p>
                    <?php endif ?>
                    </div>

            </section>
         <?php endif ?>
        
  </body>
</html>
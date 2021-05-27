<?php
include('parameters_handler.php');

if (!isset($_SESSION['user_name'])) {
  $_SESSION['msg'] = "Inicie sessão";
  header('location: auth/login.php');
}
if (isset($_GET['logout'])) {
  session_destroy();
  unset($_SESSION['user_name']);
  header("location: auth/login.php");
}

if (isset($_GET['culture_id'])) {
  $url = "localhost/psid/php/db/getStoredProcData.php?sp=spGetCultureById&p=" . $_GET['culture_id'] . "&json=true";
  $res = db_curl_request($url);
  $active_culture = json_decode($res)[0];
}

?>

<!DOCTYPE html>
<html style="font-size: 16px;">

<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta charset="utf-8">
  <meta name="keywords" content="Update Culture">
  <meta name="description" content="">
  <meta name="page_type" content="np-template-header-footer-from-plugin">
  <title>Add Parameters</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-wEmeIV1mKuiNpC+IOBjI7aAzPcEZeedi5yW5f2yOq55WWLwNGmvvx4Um1vskeMj0" crossorigin="anonymous">
  <link href="css/styles.css" rel="stylesheet" type="text/css" media="screen">

  <script class="u-script" type="text/javascript" src="js/jquery.js" defer=""></script>
  <!--<script class="u-script" type="text/javascript" src="nicepage.js" defer=""></script>-->
  <meta name="generator" content="Nicepage 3.9.3, nicepage.com">
  <link id="u-theme-google-font" rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:100,100i,300,300i,400,400i,500,500i,700,700i,900,900i|Open+Sans:300,300i,400,400i,600,600i,700,700i,800,800i">

  <script type="application/ld+json">
    {
      "@context": "http://schema.org",
      "@type": "Organization",
      "name": "Add Parameters",
      "url": "index.html"
    }
  </script>
  <meta property="og:title" content="update culture">
  <meta property="og:type" content="website">
  <meta name="theme-color" content="#478ac9">
  <link rel="canonical" href="index.html">
  <meta property="og:url" content="index.html">
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
    <div class="container-fluid py-5"></div>
    <h2 class="display-1">Add parameters to <?= $active_culture->name ?></b></h2>
    <h6 class="section-title text-light text-center">Click the checkboxes of the parameters you want to link together.</h6>
    <?php if ($error) : ?>
      <div class="alert alert-danger"><?= $error ?></div>
    <?php endif ?>
    <?php if ($warning) : ?>
      <div class="alert alert-warning"><?= $warning ?></div>
    <?php endif ?>
    <?php if ($success) : ?>
      <div class="alert alert-success"><?= $success ?></div>
    <?php endif ?>
    <section class="section-params my-5">
      <form action="add_parameters.php?culture_id=<?= $active_culture->id ?>" method="POST" style="padding: 0;" source="custom" name="form">

        <div class="row py-3">
          <div class="col-12">
            <input type="checkbox" id="name-f2a8" name="hum" value="On" class="form-check-input">
            <label for="name-f2a8" class="form-label">Humidity</label>
          </div>
          <div class="col-md-4">
            <label for="date-4441" class="form-label">Minimum Value</label>
            <input type="number" min="0" placeholder="Enter min tolerated" id="date-4441" name="min_h" value="0" class="form-control">
          </div>
          <div class="col-md-4">
            <label for="phone-447e" class="form-label">Maximum Value</label>
            <input type="number" min="0" placeholder="Enter max tolerated" id="phone-447e" name="max_h" value="0" class="form-control">
          </div>
          <div class="col-md-4">
            <label for="text-02a9" class="form-label">Tolerance</label>
            <input type="number" min="0" placeholder="Enter a tolerance in seconds (optional)" id="text-02a9" name="tol_h" value="0" class="form-control">
          </div>
        </div>
        <div class="row py-3">
          <div class="col-12">
            <input type="checkbox" id="checkbox-2df2" name="temp" value="On" class="form-check-input">
            <label for="checkbox-2df2" class="form-label">Temperature</label>
          </div>
          <div class="col-md-4">
            <label for="text-6dba" class="form-label">Minimum Value</label>
            <input type="number" min="0" placeholder="Enter min tolerated" id="text-6dba" name="min_t" value="0" class="form-control">
          </div>
          <div class="col-md-4">
            <label for="text-12ad" class="form-label">Maximum Value</label>
            <input type="number" min="0" placeholder="Enter max tolerated" id="text-12ad" name="max_t" value="0" class="form-control">
          </div>
          <div class="col-md-4">
            <label for="text-960b" class="form-label">Tolerance</label>
            <input type="number" min="0" placeholder="Enter a tolerance in seconds (optional)" id="text-960b" name="tol_t" value="0" class="form-control">
          </div>
        </div>
        <div class="row py-3">
          <div class="col-12">
            <input type="checkbox" id="checkbox-b369" name="luz" value="On" class="form-check-input">
            <label for="checkbox-b369" class="form-label">Light</label>
          </div>
          <div class="col-md-4">

            <label for="text-ff66" class="form-label">Minimum Value</label>
            <input type="number" min="0" placeholder="Enter min tolerated" id="text-ff66" name="min_l" value="0" class="form-control">

          </div>
          <div class="col-md-4">

            <label for="text-38c5" class="form-label">Maximum Value</label>
            <input type="number" min="0" placeholder="Enter max tolerated" id="text-38c5" name="max_l" value="0" class="form-control">

          </div>
          <div class="col-md-4">
            <label for="text-8204" class="form-label">Tolerance</label>
            <input type="number" min="0" placeholder="Enter a tolerance in seconds (optional)" id="text-8204" name="tol_l" value="0" class="form-control">
          </div>
        </div>
        <div class="d-grid gap-2 d-md-flex justify-content-md-center">
          <!--<a href="#" class="u-black u-btn u-btn-rectangle u-btn-submit u-button-style u-btn-1">Submit</a>-->

          <a href="index.php?culture_id=<?= $active_culture->id ?>" class="btn btn-primary">Go Back</a>

          <input type="hidden" name="culture_id" value="<?= $active_culture->id ?>">
          <input type="hidden" name="culture_name" value="<?= $active_culture->name ?>">
          <input type="submit" value="Submit" name="submit" class="btn btn-primary">


        </div>

        <!--<div class="u-form-send-message u-form-send-success"> Your form has been successfully submitted :) </div>
        <div class="u-form-send-error u-form-send-message"> Unable to send your data. Please fix errors then try again. </div>
        <input type="hidden" value="" name="recaptchaResponse">-->

      </form>
  </div>
</body>

</html>
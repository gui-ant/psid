<?php
session_start();
include('add_parameters_handler.php');

if (!isset($_SESSION['username'])) {
    $_SESSION['msg'] = "Inicie sessão";
    header('location: auth/login.php');
}
if (isset($_GET['logout'])) {
    session_destroy();
    unset($_SESSION['username']);
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
    <title>Add Parameters</title>
    <link rel="stylesheet" href="nicepage.css" media="screen">
<link rel="stylesheet" href="add_parameters.css" media="screen">
    <script class="u-script" type="text/javascript" src="jquery.js" defer=""></script>
    <script class="u-script" type="text/javascript" src="nicepage.js" defer=""></script>
    <meta name="generator" content="Nicepage 3.9.3, nicepage.com">
    <link id="u-theme-google-font" rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:100,100i,300,300i,400,400i,500,500i,700,700i,900,900i|Open+Sans:300,300i,400,400i,600,600i,700,700i,800,800i">
    
    
    
    <script type="application/ld+json">{
		"@context": "http://schema.org",
		"@type": "Organization",
		"name": "Add Parameters",
		"url": "index.html"
}</script>
    <meta property="og:title" content="update culture">
    <meta property="og:type" content="website">
    <meta name="theme-color" content="#478ac9">
    <link rel="canonical" href="index.html">
    <meta property="og:url" content="index.html">
  </head>
  
  <body class="u-body">
    <section class="u-align-center u-clearfix u-grey-90 u-section-1" id="carousel_49f6">
      <div class="u-clearfix u-sheet u-sheet-1">
        <h3 class="u-text u-text-body-alt-color u-text-1">Add parameters to <?php echo $_POST['culture_name'] ?></h3>
        <h6 class="u-align-center u-text u-text-grey-10 u-text-2">Click the checkboxes of the parameters you want to link together.</h6><br><br><br>
        <div class="u-form u-form-1">
		
          <form action="add_parameters.php" method="POST" class="u-clearfix u-form-spacing-12 u-form-vertical u-inner-form" style="padding: 0;" source="custom" name="form">
            <div class="u-form-checkbox u-form-group u-form-partition-factor-3 u-form-group-2">
			
              <input type="checkbox" id="name-f2a8" name="hum" class="u-block-d8e4-33 u-border-2 u-border-grey-75 u-border-no-left u-border-no-right u-border-no-top u-input-rectangle" style="">
              <label for="name-f2a8" class="u-label u-label-2">Humidity</label>
			  
            </div>
            <div class="u-form-group u-form-partition-factor-2 u-form-group-3">
			
              <label for="date-4441" class="u-label u-label-3">Minimum Humidity</label>
              <input type="number" min="0" placeholder="Enter min tolerated" id="date-4441" name="min_h" value="0" class="u-border-2 u-border-grey-75 u-border-no-left u-border-no-right u-border-no-top u-input u-input-rectangle">
            
			</div>
            <div class="u-form-group u-form-partition-factor-2 u-form-group-4">
              
			  <label for="phone-447e" class="u-label u-label-4">Maximum Humidity</label>
              <input type="number" min="0" placeholder="Enter max tolerated" id="phone-447e" name="max_h" value="0" class="u-border-2 u-border-grey-75 u-border-no-left u-border-no-right u-border-no-top u-input u-input-rectangle">
            
			</div>
            <div class="u-form-group u-form-group-5">
             
			 <label for="text-02a9" class="u-label u-label-5">Tolerance</label>
              <input type="number" min="0" placeholder="Enter a tolerance in seconds (optional)" id="text-02a9" name="tol_h" value="0" class="u-border-2 u-border-grey-75 u-border-no-left u-border-no-right u-border-no-top u-input u-input-rectangle">
            
			</div>
            <div class="u-form-checkbox u-form-group u-form-partition-factor-2 u-form-group-6">
              
			  <input type="checkbox" id="checkbox-2df2" name="temp" value="On">
              <label for="checkbox-2df2" class="u-label u-label-6">Temperature</label>
            
			</div>
            <div class="u-form-group u-form-partition-factor-2 u-form-group-7">
              
			  <label for="text-6dba" class="u-label u-label-7">Minimum Temperature</label>
              <input type="number" min="0" placeholder="Enter min tolerated" id="text-6dba" name="min_t" value="0" class="u-border-2 u-border-grey-75 u-border-no-left u-border-no-right u-border-no-top u-input u-input-rectangle">
            
			</div>
            <div class="u-form-group u-form-partition-factor-2 u-form-group-8">
              
			  <label for="text-12ad" class="u-label u-label-8">Maximum Temperature</label>
              <input type="number" min="0" placeholder="Enter max tolerated" id="text-12ad" name="max_t" value="0" class="u-border-2 u-border-grey-75 u-border-no-left u-border-no-right u-border-no-top u-input u-input-rectangle">
            
			</div>
            <div class="u-form-group u-form-group-9">
              
			  <label for="text-960b" class="u-label u-label-9">Tolerance</label>
              <input type="number" min="0" placeholder="Enter a tolerance in seconds (optional)" id="text-960b" name="tol_t" value="0" class="u-border-2 u-border-grey-75 u-border-no-left u-border-no-right u-border-no-top u-input u-input-rectangle">
            
			</div>
            <div class="u-form-checkbox u-form-group u-form-group-10">
              
			  <input type="checkbox" id="checkbox-b369" name="luz" value="On">
              <label for="checkbox-b369" class="u-label u-label-10">Light</label>
            
			</div>
            <div class="u-form-group u-form-partition-factor-2 u-form-group-11">
              
			  <label for="text-ff66" class="u-label u-label-11">Minimum Light</label>
              <input type="number" min="0" placeholder="Enter min tolerated" id="text-ff66" name="min_l" value="0" class="u-border-2 u-border-grey-75 u-border-no-left u-border-no-right u-border-no-top u-input u-input-rectangle">
            
			</div>
            <div class="u-form-group u-form-partition-factor-2 u-form-group-12">
              
			  <label for="text-38c5" class="u-label u-label-12">Maximum Light</label>
              <input type="number" min="0" placeholder="Enter max tolerated" id="text-38c5" name="max_l" value="0" class="u-border-2 u-border-grey-75 u-border-no-left u-border-no-right u-border-no-top u-input u-input-rectangle">
            
			</div>
            <div class="u-form-group u-form-group-13">
              
			  <label for="text-8204" class="u-label u-label-13">Tolerance</label>
              <input type="number" min="0" placeholder="Enter a tolerance in seconds (optional)" id="text-8204" name="tol_l" value="0" class="u-border-2 u-border-grey-75 u-border-no-left u-border-no-right u-border-no-top u-input u-input-rectangle">
            
			</div>
            <div class="u-align-center u-form-group u-form-submit u-form-group-14">
              
			  <a href="#" class="u-black u-btn u-btn-rectangle u-btn-submit u-button-style u-btn-1">Submit</a>
              <input type="submit" value="submit" class="u-form-control-hidden">
            </div>

            <?php if(!isset($_POST['hum']) && !isset($_POST['temp']) && !isset($_POST['luz'])) : ?>
            <div class="u-form-send-message u-form-send-message"> Please select at least one of the checkboxes (Humidity, Temperature, Light) </div>
            <?php endif ?>

            <div class="u-form-send-message u-form-send-success"> Your form has been successfully submitted :) </div>
            <div class="u-form-send-error u-form-send-message"> Unable to send your data. Please fix errors then try again. </div>
            <input type="hidden" value="" name="recaptchaResponse">
          </form>
        </div>
      </div>
    </section>
    
  </body>
</html>
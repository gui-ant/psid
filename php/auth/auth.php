<?php
include('../db/config.php');
session_start();

$errors = array();

// LOGIN USER
if (isset($_POST['login_user'])) {
    if (!login($_POST['email'], $_POST['pass']))
        array_push($errors, "Email ou password incorretos");
    else
        header('location: ../index.php');
}

function login($email, $password)
{

    $errors = $GLOBALS['errors'];

    if (empty($email)) array_push($errors, "Email é obrigatório");
    if (empty($password)) array_push($errors, "Password é obrigatória");

    if (count($errors) == 0) {

        $conn = db_connect($email, $password);

        if (!$conn) {
            return false;
        }

        $sql = "SELECT *, CURRENT_ROLE() as role FROM users WHERE email= ?";
        $stmt = mysqli_stmt_init($conn);
        if (!mysqli_stmt_prepare($stmt, $sql)) {
            header("location: /login.php?error=stmtfalhou ");
            exit();
        }
        mysqli_stmt_bind_param($stmt, "s", $email);
        mysqli_stmt_execute($stmt);
        $results = mysqli_stmt_get_result($stmt);


        if (mysqli_num_rows($results) == 1) {
            $row = mysqli_fetch_array($results);
            $_SESSION['user_id'] = $row['id'];
            $_SESSION['user_email'] = $email;
            $_SESSION['user_name'] = $row['username'];
            $_SESSION['user_role'] = $row['role'];
            $_SESSION['user_pass'] = $password;
            $_SESSION['success'] = "Sessão iniciada";

            mysqli_close($conn);

            return true;
        } else {

            mysqli_close($conn);
            return false;
        }
    }
}

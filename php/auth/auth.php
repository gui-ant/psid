<?php
include('../db/config.php');
session_start();

// initializing variables
//$username = "";
//$email    = "";
$errors = array();

// connect to the database
//$db = db_connect();
// REGISTER USER
// if (isset($_POST['reg_user'])) {
//     // receive all input values from the form
//     $username = mysqli_real_escape_string($db, $_POST['name']);
//     $email = mysqli_real_escape_string($db, $_POST['email']);
//     $password_1 = mysqli_real_escape_string($db, $_POST['pass']);
//     $password_2 = mysqli_real_escape_string($db, $_POST['pass_']);
//
//     // form validation: ensure that the form is correctly filled ...
//     // by adding (array_push()) corresponding error unto $errors array
//     if (empty($username)) {
//         array_push($errors, "Nome é obrigatório");
//     }
//     if (empty($email)) {
//         array_push($errors, "Email é obrigatório");
//     }
//     if (empty($password_1)) {
//         array_push($errors, "Password é obrigatória");
//     }
//     if ($password_1 != $password_2) {
//         array_push($errors, "As passwords não correspondem");
//     }
//
//     // first check the database to make sure
//     // a user does not already exist with the same username and/or email
//     $user_check_query = "SELECT * FROM users WHERE email='$email' LIMIT 1";
//     $result = mysqli_query($db, $user_check_query);
//     $user = mysqli_fetch_assoc($result);
//
//     if ($user) { // if user exists
//         if ($user['email'] === $email) {
//             array_push($errors, "Email já existente");
//         }
//     }
//
//     // Finally, register user if there are no errors in the form
//     if (count($errors) == 0) {
//         $password = md5($password_1); //encrypt the password before saving in the database
//
//         $query = "INSERT INTO users (name, email, pass, role_id)
//   			  VALUES('$username', '$email', '$password',null)";
//         $result = mysqli_query($db, $query);
//
//         if ($result)
//             login($email, $password_1);
//         header('location: ../index.php');
//     }
// }

// LOGIN USER
if (isset($_POST['login_user'])) {
    if (!login($_POST['email'], $_POST['pass']))
        array_push($errors, "Email ou password incorretos");
    else
        header('location: ../index.php');

}

function login($email, $password)
{

//    $db = $GLOBALS['db'];
    $errors = $GLOBALS['errors'];
//    $email = mysqli_real_escape_string($db, $email);
//    $password = mysqli_real_escape_string($db, $pass);

    if (empty($email)) array_push($errors, "Email é obrigatório");
    if (empty($password)) array_push($errors, "Password é obrigatória");

    if (count($errors) == 0) {
        //$password = md5($password);
        //$query = "SELECT * FROM users WHERE email='$email' AND pass='$password'";
		//$GLOBALS['mysql_user'] = $email;
        //$GLOBALS['mysql_pass'] = $password;
        $conn = db_connect($email, $password);
        //$results = mysqli_query($db, $query);

//         if (mysqli_num_rows($results) == 1) {
//             $row = mysqli_fetch_array($results);
//             $_SESSION['user_id'] = $row['id'];
//             $_SESSION['username'] = $row['name'];
//             $_SESSION['user_role'] = $row['role_id'];
//             $_SESSION['success'] = "Sessão iniciada";
//             return true;
//         } else {
//             return false;

        if (!$conn) {
          die("Connection failed: " .$conn->connect_error);
          return false;
        }

        $query = "SELECT * FROM users WHERE email='$email'";
        $results = mysqli_query($conn, $query);

        if (mysqli_num_rows($results) == 1) {
            $row = mysqli_fetch_array($results);
            $_SESSION['user_id'] = $row['id'];
			$_SESSION['user_email'] = $email;
            $_SESSION['username'] = $row['username'];
            $_SESSION['user_role'] = $row['role_id'];
            //$_SESSION['user_role'] = 'group_researcher';
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

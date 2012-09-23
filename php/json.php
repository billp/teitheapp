<?php
    include_once('mysql.php');
  
    $action = $_GET['action'];
    
    echo $action;

    if ($action == "update_bus_line") {
        $starting_point = $_GET['starting_point'];
        $progress = $_GET['progress'];
        
        $link = Database::connect_to_database();
        Database::execute("INSERT INTO `bus_line` (`update_time`, `starting_point`, `progress`) VALUES (now(), $starting_point, $progress)", $link);
    }
?>

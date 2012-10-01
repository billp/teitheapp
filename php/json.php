<?php
    include_once('mysql.php');
  
    header('Content-type: application/json');
    
    $action = $_GET['action'];
	$mode = $_GET['mode'];
	
    $link = Database::connect_to_database();
    
    if ($action == "bus_line") {
    	if ($mode == "") {
    		$sql = "SELECT `id`, DATE_FORMAT(`update_time`, '%e/%c %H:%i') as `update_time`, `starting_point`, `progress` 
    				FROM `bus_line` ORDER BY `id` DESC limit 5";
    		$result = mysql_query($sql, $link);
    		
    		$bus_lines = array();
    		
  			
    		
    		while ($row = mysql_fetch_assoc($result)) {
    			$json_row = array();
    			
    			$json_row['id'] = (int)$row['id'];
    			$json_row['starting_point'] = (int)$row['starting_point'];
    			$json_row['update_time'] = $row['update_time'];
    			$json_row['progress'] = (int)$row['progress'];
    		
    			array_push($bus_lines, $json_row);
    		}
    		echo json_encode($bus_lines);
    	}
    	else if ($mode == "add") {
        	$starting_point = $_GET['starting_point'];
       	 	$progress = $_GET['progress'];
        
        	$sql = "INSERT INTO `bus_line` (`update_time`, `starting_point`, `progress`) VALUES (now(), $starting_point, $progress)";
    	    Database::execute($sql, $link);
    	    
    	    if (mysql_affected_rows($link) > 0) {
    			$json['status'] = "success";
    			$json['id'] = mysql_insert_id();
    			echo json_encode($json);
    		} else {
    			$json['status'] = "failed";
    			echo json_encode($json);
    		}

    	}
    	
    	else if ($mode == "update") {
    		$id = $_GET['id'];
        	$starting_point = $_GET['starting_point'];
       	 	$progress = $_GET['progress'];
        
        	$sql = "UPDATE `bus_line` SET `starting_point` = $starting_point,
        								  `update_time` = now(),
        								  `progress` = $progress WHERE `id` = $id";
    	    Database::execute($sql, $link);
    	    
    	    if (mysql_affected_rows($link) > 0) {
    			$json['status'] = "success";
    			$json['id'] = (int)$id;
    			echo json_encode($json);
    		} else {
    			$json['status'] = "failed";
    			echo json_encode($json);
    		}

    	}
    	
    	else if ($mode == "delete") {
    		$id = $_GET['id'];
    		$sql = sprintf("DELETE FROM `bus_line` WHERE `id` = %s", $id);
        	
    		Database::execute($sql, $link);
    		if (mysql_affected_rows($link) > 0) { 
    			$json['status'] = "success";

    			echo json_encode($json);
    		} else {
    			$json['status'] = "failed";
    			echo json_encode($json);
    		}
    	}
    }
    
    Database::close_database($link);
?>

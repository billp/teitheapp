<?php
    include_once('mysql.php');
  
    header('Content-type: application/json');
    
    $action = $_GET['action'];
	$mode = $_GET['mode'];
	
    $link = Database::connect_to_database();
    
    if ($action == "bus_line") {
    	if ($mode == "") {
    		$sql = "SELECT `id`,UNIX_TIMESTAMP(`update_time`) as `update_time`, `starting_point`, `progress` 
    				FROM `bus_line` ORDER BY `update_time` DESC limit 5";
    		$result = mysql_query($sql, $link);
    		
    		$bus_lines = array();
    		
  			
    		
    		while ($row = mysql_fetch_assoc($result)) {
    			$json_row = array();
    			
    			$json_row['id'] = (int)$row['id'];
    			$json_row['starting_point'] = (int)$row['starting_point'];
    			$json_row['update_time'] = (int)$row['update_time'];
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
    else if ($action == "students_online") {
    	if ($mode == "") {
    		$sql = "SELECT `id`, UNIX_TIMESTAMP(`update_time`) as `update_time`, `name`, `surname` 
    				FROM `students_online` ORDER BY `surname`, `name` ASC";
    		$result = mysql_query($sql, $link);
    		
    		$students_rows = array();
    		
    		while ($row = mysql_fetch_assoc($result)) {
    			$json_row = array();
    			
    			$json_row['id'] = (int)$row['id'];
    			$json_row['name'] = $row['name'];
    			$json_row['surname'] = $row['surname'];
    			$json_row['update_time'] = (int)$row['update_time'];
    		
    			array_push($students_rows, $json_row);
    		}
    		echo json_encode($students_rows);
    	}
    	else if ($mode == "checkin") {
        	$name = $_GET['name'];
       	 	$surname = $_GET['surname'];
        
        	//Remove old rows
        	$sql = "DELETE FROM `students_online` WHERE UNIX_TIMESTAMP(NOW())-UNIX_TIMESTAMP(`update_time`) > 60 * 10";
        	Database::execute($sql, $link);
     
        	$rows_deleted = mysql_affected_rows($link);
        
        	$sql = "INSERT INTO `students_online` (`name`, `surname`, `update_time`) VALUES ('$name', '$surname', now())";
    	    Database::execute($sql, $link);
    	    
    	    if (mysql_affected_rows($link) > 0) {
    			$json['status'] = "success";
    			$json['rows_deleted'] = $rows_deleted;
    			echo json_encode($json);
    		} else {
    			$json['status'] = "failed";
    			echo json_encode($json);
    		}

    	}
    }
    else if ($action == "chat") {
    	if ($mode == "") {
    		$sql = "SELECT `id`, UNIX_TIMESTAMP(`update_time`) as `update_time`, `student_name`, `text` 
    				FROM `chat` ORDER BY `update_time` ASC";
    		$result = mysql_query($sql, $link);
    		
    		$chat_rows = array();
    		
    		while ($row = mysql_fetch_assoc($result)) {
    			$json_row = array();
    			
    			$json_row['id'] = (int)$row['id'];
    			$json_row['student_name'] = $row['student_name'];
    			$json_row['text'] = $row['text'];
    			$json_row['update_time'] = (int)$row['update_time'];
    		
    			array_push($chat_rows, $json_row);
    		}
    		echo json_encode($chat_rows);
    	}
    	else if ($mode == "add") {
        	$student_name = $_GET['student_name'];
       	 	$text = $_GET['text'];
        
       
        	$sql = "INSERT INTO `chat` (`student_name`, `text`, `update_time`) VALUES ('$student_name', '$text', now())";
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

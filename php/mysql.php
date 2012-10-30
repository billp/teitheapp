<?php

require_once("./config.php");

class Database {


    public static function connect_to_database() {
    	global $db;
    	$link = mysql_connect($db['sock'], $db['user'], $db['pass']);
        if (!$link) {
            die('Could not connect: ' . mysql_error());
        }

        mysql_select_db($db['db_name'], $link);        

        return $link;
    }

    function close_database($link) {
        mysql_close($link);
    }

    function execute($sql, $link) {
        mysql_query($sql, $link);    
    }
}

?>

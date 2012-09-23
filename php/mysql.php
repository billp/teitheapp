<?php

require_once("./config.php");

class Database {
   // global $mysql_sock, $mysql_user, $mysql_

    public static function connect_to_database() {
        $link = mysql_connect('localhost:/home/student/x0809/vpanag/mysql/run/mysql.sock', 'teitheapp', 'runtothehills');
        if (!$link) {
            die('Could not connect: ' . mysql_error());
        }

        mysql_select_db("teitheapp", $link);        

        return $link;
    }

    function close_database() {
        mysql_close($link);
    }

    function execute($sql, $link) {
        mysql_query($sql, $link);    
    }

    
}

?>

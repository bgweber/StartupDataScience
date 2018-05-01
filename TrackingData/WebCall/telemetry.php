<?php
 $dataFile = fopen("tracking.log", "a");

 $message = $_GET['message'];

 if ($message != '') {
   fwrite($dataFile, "$message\n");
 }

 fflush($dataFile);
 fclose($dataFile);
?>

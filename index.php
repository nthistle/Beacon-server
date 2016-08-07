<?php
  // $link = mysqli_connect ("localhost:8000", "yash1", "forest");
   $t = $_GET["img"];
   $x_cor = $_GET["x_cor"];
   $y_cor = $_GET["y_cor"];
   $event_name = $_GET["event_name"];
   $date = $_GET["date"];
   $ran = "images/" . (string)$x_cor . "," . (string)$y_cor . "," . (string)$event_name . "," . (string)$date;
   $command = "echo \"" . $t . "\" > " . $ran;
   system($command);
     
?>

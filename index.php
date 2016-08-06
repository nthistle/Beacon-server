<?php
  // $link = mysqli_connect ("localhost:8000", "yash1", "forest");
   $t = $_GET["img"];
   $x_cor = $_GET["x_cor"];
   $y_cor = $_GET["y_cor"];
   $ran = "images/" . (string)$x_cor . "," . (string)$y_cor;
   $command = "echo \"" . $t . "\" > " . $ran;
   system($command);
     
?>

<?php
  // $link = mysqli_connect ("localhost:8000", "yash1", "forest");
   $t = $_GET["img"];
   $ran = (string)md5(mt_rand());
   $ran = "images/" . $ran;
   $command = "echo \"" . $t . "\" > " . $ran . ".jpg";
   system($command);
     
?>

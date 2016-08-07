<?php
   $dir = "../beacon-server/images/";
   $x_c = (int)$_GET["x"];
   $y_c = (int)$_GET["y"];
   $files = scandir ($dir);
   $cnt = 0; 
   foreach ($files as $item){
     if (strcmp ($item, "..") === 0){
       continue; 
     }
     if (strcmp ($item, ".") === 0){
       continue; 
     }
     $lis = explode(",", $item);
     $x_cc = (int)$lis["0"];
     $y_cc = (int)$lis["1"];
     $event_n = $lis["2"];
     $dat = $lis["3"];
     if (($x_cc - $x_c) * ($x_cc - $x_c) + ($y_cc - $y_c) * ($y_cc - $y_c) <= 100){
      print (trim(file_get_contents("images/" . $item)));
      print (",");
      print ((string)$x_cc);
      print (",");
      print ((string)$y_cc);
      print ("\n");
     } 
   }
?>

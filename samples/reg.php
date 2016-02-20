<?php
	$file_path = "gcm.dat";

	if (isset($_POST['regid']))
		$regid = $_POST['regid'];
	else
        exit('Failed to get ID.');

	$fp = fopen($file_path, 'c+');
	if (!$fp)
        exit('File access failed.');
	if (!flock($fp, LOCK_EX)) {
		fclose($fp);
		exit('File lock failed.');
	}

	while ($line = fgets($fp)) {
		if ($line == $regid . "\n") {
			flock($fp, LOCK_UN);
			fclose($fp);
			exit('ID already registered.');
		}
	}

	if (fwrite($fp, $regid . "\n"))
		echo 'ID successfully added.';
	else
		echo 'Failed to add ID.';

	fflush($fp);
	flock($fp, LOCK_UN);
	fclose($fp);
?>

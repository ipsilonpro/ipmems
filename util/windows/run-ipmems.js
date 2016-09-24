var sh = WScript.createObject("WScript.Shell");
sh.CurrentDirectory = ".";
sh.Exec("javaw -jar ipmems.jar");

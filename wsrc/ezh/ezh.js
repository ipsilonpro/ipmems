var xmlReq = null;
var timer = null;

function mkUri(file, params) {
	var s = file + "?";
	var first = true;
	for (var n in params) if (first) {
		s += encodeURIComponent(n) + "=" + encodeURIComponent(params[n]);
		first = false;
	} else {
		s += "&" + encodeURIComponent(n) + "=" + encodeURIComponent(params[n]);
	}
	return s;
}

function cmd_state_change(id, r) {
	if (r.readyState == 4) {
		log("INFO", id + ": " + r.statusText);
		try {
			var data = JSON.parse(r.responseText);
			if (data.error != null) {
				log("WARNING", data.error);
			}
			for (var i in data.ca) {
				document.getElementById(data.ca[i]).disabled = false;
			}
		} catch (x) {
			log("WARNING", x);
		}
	}
}

function mon_state_change(r) {
	if (r.readyState == 4) try {
		var data = JSON.parse(r.responseText);
		for (var i in data) {
			var e = document.getElementById(i);
			e.textContent = data[i];
		}
	} catch (x) {
		log("WARNING", x);
	}
}

function timer_func(r) {
	r.open("GET", "values.groovy", true);
	r.send(null);
}

function clear_log() {
	document.getElementById("results").innerHTML = "";
}

function onload() {
	xmlReq = new XMLHttpRequest();
	xmlReq.onreadystatechange = function() {
		mon_state_change(xmlReq);
	}
	timer = setInterval(function() {
		timer_func(xmlReq);
	}, 1000);
}

function log(level, msg) {
	var res = document.getElementById("results");
	var e = document.createElement("p");
	e.textContent = new Date().toLocaleString() + " " +
		level.toUpperCase() + " " + msg;
	res.appendChild(e);
}

function send_cmd(id, ctrl_array, tago, tagi, val, ctrl) {
	var req = new XMLHttpRequest();
	req.onreadystatechange = function() {
		cmd_state_change(id, req);
	};
	for (var i in ctrl_array) {
		document.getElementById(ctrl_array[i]).disabled = true;
	}
	var uri = mkUri("sendcmd.groovy", {
		ca: ctrl_array,
		to: tago,
		ti: tagi,
		val: val,
		ctrl: ctrl
	});
	req.open("GET", uri, true);
	req.send(null);
}

function valve_open(ev) {
	var re = /valve([\d]+)_open/;
	var id = ev.target.id;
	var n = parseInt(id.match(re)[1]);
	var ctrl_array = [
		"valve" + n + "_open",
		"valve" + n + "_semiopen",
		"valve" + n + "_semiclose",
		"valve" + n + "_close"
	];
	var ctrl = "V" + n;
	var tago = "/ezhkov/valve" + n + "/open_o";
	var tagi = "/ezhkov/valve" + n + "/open_i";
	send_cmd(id, ctrl_array, tago, tagi, 1, ctrl);
}
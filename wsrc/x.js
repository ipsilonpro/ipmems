var xml = new XMLHttpRequest();

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

function timer() {
	var ns = document.getElementsByTagName("text");
	var k = 0;
	var l = [];
	for (var i = 0; i < ns.length; i++) {
		var id = ns[i].getAttribute("id");
		if (id.match(/id:.+/)) l[k++] = id;
	}
	try {
		xml.open("GET", mkUri("x.groovy", {ids: l.toString()}), false);
		xml.send(null);
		var txt = xml.responseText;
		var obj = JSON.parse(txt);
		for (var n in obj) {
			var e = document.getElementById(n);
			e.firstChild.textContent = obj[n];
		}
	} catch (x) {
		alert(x);
	}
}

window.setInterval(timer, 10000);
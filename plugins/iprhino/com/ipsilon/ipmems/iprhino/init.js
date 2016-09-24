importPackage(Packages.org.ipsilon.ipmems);
importPackage(Packages.org.ipsilon.ipmems.json);
importPackage(Packages.org.ipsilon.ipmems.logging);
importPackage(Packages.org.ipsilon.ipmems.net);
importPackage(Packages.org.ipsilon.ipmems.prot);
importPackage(Packages.org.ipsilon.ipmems.scripting);
importPackage(Packages.org.ipsilon.ipmems.web);

function curry(fn) {
	var slice = Array.prototype.slice;
	var args = slice.apply(arguments, [1]);
	return function() {
		return fn.apply(null, args.concat(slice.apply(arguments)));
	};
}

function composed(f1, f2) {
	return function() {
		var t = f1.apply(null, arguments);
		return (t instanceof Array) ? f2.apply(null, t) : f2.apply(null, [t]);
	};
}

function print() {
	for (var i = 0; i < arguments.length; i++) out.print(arguments[i]);
}

function println() {
	for (var i = 0; i < arguments.length; i++) out.println(arguments[i]);
	if (arguments.length == 0) out.println();
}

function getContent() {
	switch (arguments.length) {
		case 1:
			return IpmemsStrings.content(arguments[0]);
		case 2:
			return IpmemsStrings.content(arguments[0], arguments[1]);
		default:
			return null;
	}
}

function ef(file) {
	return eval(substituted(file));
}

function sysProp() {
	switch (arguments.length) {
		case 1:
			return Ipmems.get(arguments[0]);
		case 2:
			return Ipmems.get(arguments[0], arguments[1]);
	}
}

function info(l, m) {
	if (arguments.length == 2) {
		IpmemsLoggers.info(l, m);
	} else {
		var tercero = arguments[3];
		var t = tercero.javaException;
		if (t == null) t = tercero.rhinoException;
		var a, i;
		if (t == null) {
			a = new Array(arguments.length - 2);
			for (i = 0; i < a.length; i++) a[i] = arguments[2 + i];
			IpmemsLoggers.info(l, m, a);
		} else {
			a = new Array(arguments.length - 3);
			for (i = 0; i < a.length; i++) a[i] = arguments[3 + i];
			IpmemsLoggers.info(l, m, t, a);
		}
	}
}

function warning(l, m) {
	if (arguments.length == 2) {
		IpmemsLoggers.warning(l, m);
	} else {
		var tercero = arguments[3];
		var t = tercero.javaException;
		if (t == null) t = tercero.rhinoException;
		var a, i;
		if (t == null) {
			a = new Array(arguments.length - 2);
			for (i = 0; i < a.length; i++) a[i] = arguments[2 + i];
			IpmemsLoggers.warning(l, m, a);
		} else {
			a = new Array(arguments.length - 3);
			for (i = 0; i < a.length; i++) a[i] = arguments[3 + i];
			IpmemsLoggers.warning(l, m, t, a);
		}
	}
}

function severe(l, m) {
	if (arguments.length == 2) {
		IpmemsLoggers.severe(l, m);
	} else {
		var tercero = arguments[3];
		var t = tercero.javaException;
		if (t == null) t = tercero.rhinoException;
		var a, i;
		if (t == null) {
			a = new Array(arguments.length - 2);
			for (i = 0; i < a.length; i++) a[i] = arguments[2 + i];
			IpmemsLoggers.severe(l, m, a);
		} else {
			a = new Array(arguments.length - 3);
			for (i = 0; i < a.length; i++) a[i] = arguments[3 + i];
			IpmemsLoggers.severe(l, m, t, a);
		}
	}
}

function config(l, m) {
	if (arguments.length == 2) {
		IpmemsLoggers.config(l, m);
	} else {
		var tercero = arguments[3];
		var t = tercero.javaException;
		if (t == null) t = tercero.rhinoException;
		var a, i;
		if (t == null) {
			a = new Array(arguments.length - 2);
			for (i = 0; i < a.length; i++) a[i] = arguments[2 + i];
			IpmemsLoggers.config(l, m, a);
		} else {
			a = new Array(arguments.length - 3);
			for (i = 0; i < a.length; i++) a[i] = arguments[3 + i];
			IpmemsLoggers.config(l, m, t, a);
		}
	}
}

function fine(l, m) {
	if (arguments.length == 2) {
		IpmemsLoggers.fine(l, m);
	} else {
		var tercero = arguments[3];
		var t = tercero.javaException;
		if (t == null) t = tercero.rhinoException;
		var a, i;
		if (t == null) {
			a = new Array(arguments.length - 2);
			for (i = 0; i < a.length; i++) a[i] = arguments[2 + i];
			IpmemsLoggers.fine(l, m, a);
		} else {
			a = new Array(arguments.length - 3);
			for (i = 0; i < a.length; i++) a[i] = arguments[3 + i];
			IpmemsLoggers.fine(l, m, t, a);
		}
	}
}

function finer(l, m) {
	if (arguments.length == 2) {
		IpmemsLoggers.finer(l, m);
	} else {
		var tercero = arguments[3];
		var t = tercero.javaException;
		if (t == null) t = tercero.rhinoException;
		var a, i;
		if (t == null) {
			a = new Array(arguments.length - 2);
			for (i = 0; i < a.length; i++) a[i] = arguments[2 + i];
			IpmemsLoggers.finer(l, m, a);
		} else {
			a = new Array(arguments.length - 3);
			for (i = 0; i < a.length; i++) a[i] = arguments[3 + i];
			IpmemsLoggers.finer(l, m, t, a);
		}
	}
}

function finest(l, m) {
	if (arguments.length == 2) {
		IpmemsLoggers.finest(l, m);
	} else {
		var tercero = arguments[3];
		var t = tercero.javaException;
		if (t == null) t = tercero.rhinoException;
		var a, i;
		if (t == null) {
			a = new Array(arguments.length - 2);
			for (i = 0; i < a.length; i++) a[i] = arguments[2 + i];
			IpmemsLoggers.finest(l, m, a);
		} else {
			a = new Array(arguments.length - 3);
			for (i = 0; i < a.length; i++) a[i] = arguments[3 + i];
			IpmemsLoggers.finest(l, m, t, a);
		}
	}
}

function evaluate(o) {
	return IpmemsScriptEngines.eval(o);
}

function substituted(o) {
	return Ipmems.substituted(o);
}

function locString(k) {
	var a = new Array(arguments.length - 1);
	for (var i = 0; i < a.length; i++) a[i] = arguments[1 + i];
	return IpmemsIntl.string(k, a);
}

function locMessage(k) {
	var a = new Array(arguments.length - 1);
	for (var i = 0; i < a.length; i++) a[i] = arguments[1 + i];
	return IpmemsIntl.message(k, a);
}

function json(o) {
	return IpmemsJsonUtil.json(o);
}

function ioTask(t, r) {
	return IpmemsProtUtil.ioTask(t, r);
}

function ioVectorTask(t, r) {
	return IpmemsProtUtil.ioVectorTask(t, r);
}

function locStr(l, k) {
	var a = new Array(arguments.length - 1);
	for (var i = 0; i < a.length; i++) a[i] = arguments[2 + i];
	return IpmemsIntl.locString(l, k, a);
}

function locMsg(l, k) {
	var a = new Array(arguments.length - 1);
	for (var i = 0; i < a.length; i++) a[i] = arguments[2 + i];
	return IpmemsIntl.locMessage(l, k, a);
}

function mkUrl(p, m) {
	return IpmemsNet.mkUrl(p, m);
}

function exceptionText(o) {
	if (o instanceof java.lang.Throwable) {
		return IpmemsStrings.exceptionText(o);
	} else if (o.javaException != null) {
		return IpmemsStrings.exceptionText(o.javaException);
	} else if (o.rhinoException != null) {
		return IpmemsStrings.exceptionText(o.rhinoException);
	} else return "";
}

function bind(k, v) {
	IpmemsScriptEngines.bind(k, v);
}

ipmems = IpmemsLib;
userMap = IpmemsScriptEngines.userMap;
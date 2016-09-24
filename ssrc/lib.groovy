universalIO = {q, r ->
	if (q.containsKey("io")) {
		def io = q.get("io");
		if (q.get("regen", true) && !io.active) {
			info("usr", "{0} Regen I/O", q.name)
			q.remove("io");
			io.close();
		}
	}
	if (!q.containsKey("io")) {
		def io = q.ioClass.newInstance();
		io.map.putAll(q.get("ioParams", [:]));
		info("usr", "{0} Establishing {1}", q.name, io);
		io.connect();
		q.put("io", io);
		info("usr", "{0} Established {1}", q.name, io);
	}
	return r;
}
println("Content-Type: text/plain; charset=UTF-8");
println();

def ctrlArray = uriProps.ca.split(",");

try {
	def t = System.currentTimeMillis();
	def ds = dbServer.gate("dataStore", "db");
	def ru = ds.store(rows: [[uriProps.to, t, uriProps.val]]);
	if (ru.containsKey("error")) throw ru.error;
	def u = ru.updateCount;
	if (u <= 0) throw new IllegalStateException("Update count <= 0");
	for (def i = 0; i < 30; i++) {
		Thread.sleep(1000L);
		def rs = ds.values(id: uriProps.ti, t: t).rows;
		if (!rs.empty) {
			def v = null;
			rs.each{v = it.val};
			print(json([
				ca: ctrlArray,
				to: uriProps.to,
				ti: uriProps.ti,
				val: v,
				ctrl: uriProps.ctrl,
				error: null
			]));
			return;
		}
	}
	throw new IllegalStateException("Uncommitted");
} catch (x) {
	print(json([
		ca: ctrlArray,
		to: uriProps.to,
		ti: uriProps.ti,
		val: null,
		ctrl: uriProps.ctrl,
		error: x.toString()
	]));
}
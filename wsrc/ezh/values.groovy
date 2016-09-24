println("Content-Type: text/plain; charset=UTF-8");
println();

def ds = dbServer.gate("dataStore", "db");

def value = {fmt, tag ->
	def v = null;
	def rs = ds.values(dyn: true, id: tag, max: 1).rows;
	rs.each{v = it.val};
	return String.format(fmt, v);
};

def data = [
	"t1": value("%.3f", "/demoObject/device1/temp"),
	"p1": value("%.3f", "/demoObject/device1/pressure")
];

print(json(data));
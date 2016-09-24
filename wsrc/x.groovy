println("Content-Type: text/html; charset=UTF-8");
println();

def ids = uriProps.getProperty("ids", "").split(",");
def m = [:];
def ds = dbServer.gate("dataStore", "db");
for (def id in ids) if (!id.empty) {
	def dbid = id.trim().substring(3).replace("_", "/");
	def rows = ds.values(id: dbid).rows;
	rows.each{row -> m[id] = row.val};
}

info("usr", "{0}", json(m));
print(json(m));
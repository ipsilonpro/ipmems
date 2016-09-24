info("db", "DB server: {0}", dbServer);
def ds = dbServer.gate("dataStore", "db");
def tss = [
	new Date(112, 0, 1, 0, 0, 0),
	new Date(112, 1, 2, 0, 30, 0),
	new Date(112, 2, 3, 1, 45, 10),
	new Date(112, 2, 3, 1, 45, 20)
];

def data = [
	[1, tss[0], 2.3],
	[2, tss[1], 4.5],
	[3, tss[2], 6.6], 
	[3, tss[3], 4.3]
];

def data2 = [
	[id: "/xx/yy/zz1", t: tss[0], val: 2.3],
	[id: "/xx/yy/zz2", t: tss[1], val: 4.5],
	[id: "/xx/yy/zz3", t: tss[2], val: 6.6],
	[id: "/xx/yy/zz3", t: tss[3], val: 4.3]
];

info("db", "TSS: {0}", tss);
info("db", "DATA: {0}", data);
info("db", "DATA2: {0}", data2);

info("db", "Storing DATA result: {0}", ds.store(rows: data));
info("db", "Storing DATA2 result: {0}", ds.store(rows: data2));

info("db", "Values: {0}", ds.values(ids: [1, 2, 3]));
info("db", "Values2: {0}", ds.values(ids: ["/xx/yy/zz1", "/xx/yy/zz2", "/xx/yy/zz3"]));

info("db", "Update nt=new Date(): {0}", ds.update(rows: [[id: 3, nt: new Date(), val: 7.8]]))
info("db", "Values: {0}", ds.values(ids: [1, 2, 3]));
info("db", "Delete2 id=/xx/yy/zz3, t=tss[2]: {0}", ds.delete(rows: [[id: "/xx/yy/zz3", t: tss[2]]]));
info("db", "Values2: {0}", ds.values(ids: ["/xx/yy/zz1", "/xx/yy/zz2", "/xx/yy/zz3"]));
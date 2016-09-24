/*
 * Data acquisition example.
 * (c) 2012 Ipsilon-Pro L.L.C.
 * Author: Dmitry Ovchinnikov
 */

DbSyncLogger.start(dbServer, "db");

[
	queues: [
		/*
		ctrl: [
			tasks: [
				ctrl1: [
					func: {t, r ->
						def ds = dbServer.gate("dataStore", "db");
						def u = ds.store(rows: [[t.tag, new Date(), 1]]);
						info("usr", "{0} {1}", t, u);
					},
					period: 10000,
					params: [tag: "/ctrl1c"]
				],
				ctrl2: [
					func: {t, r ->
						def ds = dbServer.gate("dataStore", "db");
						def ts = System.currentTimeMillis() - 60 * 1000;
						def rows = ds.after(id: t.tag, t: ts).rows;
						def u = 0;
						rows.each{
							def rs = ds.values(id: t.rtag, t: it.t).rows;
							if (rs.empty) {
								u += ds.store(
									rows: [[t.rtag, it.t, 1]]).updateCount;
							}
						}
						if (u > 0) info("usr", "{0} {1}", t, u);
					},
					period: 3000,
					params: [tag: "/ctrl1c", rtag: "/ctrl1r"]
				]
			]
		],*/
		modbusRTU: [ // Modbus RTU data acquisition queue
			func: universalIO,
			params: [
				ioParams: [port: 20011],
				ioClass: IpmemsSocketIO
			],
			groups: [
				device1: [ // Device1 transactions group
					params: [addr: 0x23],
					tasks: [
						temperature: [ // Get-temperature transaction
							func: [ioTask, ipmems_db_write],
							period: 3000,
							params: [
								tags: [val: "/demoObject/device1/temp"],
								reg: 0x0001,
								input: [[
									dev: [l: 1, c: {it.v[0]}],
									func: [l: 1, c: {it.v[0]}],
									val: [l: 4, c: {it.xfloat()}],
									crc: [l: 2, c: {it.ccrc16("le")}]
								]],
								output: [
									dev: {it.obj.addr},
									func: 0x03,
									reg: {it.bin(it.obj.reg as short)},
									count: [0, 2], // Query for 2 registers
									crc: {it.crc16("le")}
								],
								guards: [[8]] // Incoming bytes count: 8
							]
						],
						pressure: [ // Get-pressure transaction
							func: [ioTask, ipmems_db_write],
							period: 6000,
							params: [
								reg: 0x0003,
								tags: [val: "/demoObject/device1/pressure"],
								input: [[
									dev: [l: 1, c: {it.v[0]}],
									func: [l: 1, c: {it.v[0]}],
									val: [l: 4, c: {it.xfloat()}],
									crc: [l: 2, c: {it.ccrc16("le")}]
								]],
								output: [
									dev: {it.obj.addr},
									func: 0x03,
									reg: {it.bin(it.obj.reg as short)},
									count: [0, 2], // Query for 2 registers
									crc: {it.crc16("le")}
								],
								guards: [[8]] // Incoming bytes count: 8
							]
						]
					]
				]
			]
		],
		sync_db: [
			tasks: [
				cur_sync: [
					period: 10000,
					func: ipmems_db_sync,
					params: [
						ldbname: "db",
						password: "ipapepticus",
						cmap: [
							db: "arch"
						]
					]
				]
			]
		]
	]
]
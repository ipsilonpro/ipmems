def value = {dev, addr ->
	switch (dev) {
		case 0x23:
			switch (addr) {
				case 1: return 23.0;
				case 3: return 101.0;
				default: return 0.0;
			}
			break;
		default:
			return -1;
	}
};

[
	demoModbusRTU: [
		ioParams: [port: 20011],
		ioClass: IpmemsServerSocketIO,
		params: [
			processor: [
				inputs: [
					readFloat: [
						dev: [l: 1, c: {it.v[0]}],
						func: [l: 1, c: {it.v[0] == 3 ? 3 : null}],
						addr: [l: 2, c: {it.xshort()}],
						count: [l: 2, c: {it.xshort() == 2 ? 2 : null}],
						crc: [l: 2, c: {it.ccrc16("le")}]
					]
				],
				outputs: [
					readFloat: [
						dev: {it.obj.data.readFloat.dev},
						func: {it.obj.data.readFloat.func},
						val: {it.bin(value(
									it.obj.data.readFloat.dev,
									it.obj.data.readFloat.addr) as float)
						},
						crc: {it.crc16("le")}
					]
				]
			]
		]
	]
]
static def info() {[
	rtxt_init: [
		port: [
			name: "Communication port",
			type: String.class,
			defValue: "/dev/ttyS0"
		],
		baud: [
			name: "Baud rate",
			type: Integer.class,
			defValue: 9600
		]
	],
	ipmems_tcp_init: [
		url: [
			name: "Target URL",
			type: String.class,
			defValue: null
		],
		port: [
			name: "TCP port",
			type: Integer.class,
			defValue: null
		]
	]
]}
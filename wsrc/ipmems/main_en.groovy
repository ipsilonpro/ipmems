{xml ->
	xml.p("IPMEMS is a cross-platform data acquisition and visualisation software with an embedded HTTP/HTTPS-server, binary protocol parsing library, remote secure administration server, embedded Groovy and JavaScript scripting facilities.")
	xml.p("IPMEMS contains the following modules: ")
	xml.table(cellspacing: "5px") {
		tr() {
			td() {
				img(src: "db.png", alt: "dbms")
			}
			td() {
				mkp.yield("Embedded ");
				a("HSQLDB 2.X", [href: "http://www.hsqldb.org"])
				mkp.yield(", reliable and robust DBMS for data storing and manipulation");
			}
		}
		tr() {
			td() {
				img(src: "webServer.png", alt: "webServer")
			}
			td("Embedded Web-server (HTTPS is supported) with Groovy and JavaScript server scripts and virtual hosts support")
		}
		tr() {
			td() {
				img(src: "scheduler.png", alt: "scheduler")
			}
			td("Multithreaded user-configurable real-time scheduler (built on top of java.util.concurrent package)")
		}
		tr() {
			td() {
				img(src: "parser.png", alt: "parser")
			}
			td("Embedded binary and ASCII device protocol parsing library (works fine with the embedded scheduler)")
		}
		tr() {
			td() {
				img(src: "adm.png", alt: "adm")
			}
			td("RCLI-server for easy and secure administration of the IPMEMS (there are console and GUI clients)")
		}
		tr() {
			td() {
				img(src: "intl.png", alt: "intl")
			}
			td("Easy localisation and internationalisation via UTF-8 encoded text resources")
		}
		tr() {
			td() {
				img(src: "telnet.png", alt: "telnet")
			}
			td("Embedded TELNET-server for easy but unsecure access from any OS")
		}
		tr() {
			td() {
				img(src: "dbfi.png", alt: "dbfi")
			}
			td("Embedded DBFI-server for easy access to the IPMEMS data without SQL from external systems")
		}
	}
}
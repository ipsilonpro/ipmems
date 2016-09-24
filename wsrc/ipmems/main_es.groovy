{xml ->
	xml.p("IPMEMS es un programa multiplataforma para adquisición y vizualisación de datos con HTTP/HTTPS servidor, analizador sintáctico de protocoles de equipo, servidor seguro para administrar el software de forma remota, Groovy y JavaScript guiones integrados en el programa.")
	xml.p("IPMEMS incluye los modules siguientes: ")
	xml.table(cellspacing: "5px") {
		tr() {
			td() {
				img(src: "db.png", alt: "dbms")
			}
			td() {
				mkp.yield("Integrado SGBD ");
				a("HSQLDB 2.X", [href: "http://www.hsqldb.org"])
				mkp.yield(" , un software maduro, sólido y fuerte para guardar y manipular los datos");
			}
		}
		tr() {
			td() {
				img(src: "webServer.png", alt: "webServer")
			}
			td("Integrado servidor Web (HTTPS está soportado) con Groovy y JavaScript guiones del lado del servidor y soporto de hosts virtuales")
		}
		tr() {
			td() {
				img(src: "scheduler.png", alt: "scheduler")
			}
			td("Multihilo planificador de tareas, configurado por usuario (construido a través de java.util.concurrent paquete) y integrado en el programa")
		}
		tr() {
			td() {
				img(src: "parser.png", alt: "parser")
			}
			td("Integrado analizador de datos binarios y ASCII desde un equipo (funciona bien con el planificador multihilo)")
		}
		tr() {
			td() {
				img(src: "adm.png", alt: "adm")
			}
			td("RCLI-servidor para acceso de administrador de forma remota y segura (hay clientes de consola y GUI)")
		}
		tr() {
			td() {
				img(src: "intl.png", alt: "intl")
			}
			td("Internacionalización y localización fácil por medio de recursos textuales con la codificación UTF-8")
		}
		tr() {
			td() {
				img(src: "telnet.png", alt: "telnet")
			}
			td("Integrado servidor TELNET que da una oportunidad de tener acceso fácil desde cualquier sistema operativo")
		}
		tr() {
			td() {
				img(src: "dbfi.png", alt: "dbfi")
			}
			td("Integrado servidor DBFI para tener acceso fácil a los datos de IPMEMS sin SQL desde sistemas externos")
		}
	}
}
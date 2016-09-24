import groovy.xml.MarkupBuilder;
import java.sql.Timestamp;

println("Content-Type: text/html; charset=UTF-8");
println();

def acl = queryProps.getProperty("Accept-Language", "en");

def mainPage(b) {
	b.html() {
		head() {
			title(ipmems.logo)
		}
		frameset(rows: "45,*", cols: "*") {
			frame(src: "$uriPath?frame=top", name: "topFrame")
			frame(src: "$uriPath?frame=bottom", name: "bottomFrame")
		}
	}
}

def topPage(b) {
	b.html() {
		head() {
			link(href: "ipmems.css", rel: "stylesheet", type: "text/css")
		}
		body() {
			table(width: "100%") {
				tr() {
					for (def k in ipmems.ls().getLoggerNames())
						th() {
							a(k, [href: "$uriPath?logkey=${k}",
								  target: "bottomFrame"])
						}
				}
			}
		}
	}
}

def logPage(b, k, l) {
	b.html() {
		head() {
			link(href: "ipmems.css", rel: "stylesheet", type: "text/css")
		}
		body() {
			table(width: "100%") {
				tr() {
					th("${locStr(l, "Type")}")
					th("${locStr(l, "Timestamp")}")
					th("${locStr(l, "Message")}")
				}
				for (def r in ipmems.ls().getLogger(k).memoryHandler.records) {
					def type = r.getLevelName(r.level);
					tr() {
						td(type)
						td("${new Timestamp(r.timestamp)}")
						if (r.thrown == null)
							td("${locMsg(l, r.message, r.params)}")
						else {
							def uuid = UUID.randomUUID().toString();
							if (!userMap.containsKey("throwns"))
								userMap.put("throwns",
									new WeakHashMap().asSynchronized());
							userMap.throwns.put(uuid, r.thrown);
							td() {
								a("${locMsg(l, r.message, r.params)}",
								  [href: "$uriPath?uuid=${uuid}",
								   target: "_blank"])
							}
						}
					}
				}
			}
		}
	}
}

def errItem(b, t) {
	b.ul() {
		li() {
			b("${t}")
		}
		for (def e in t.stackTrace) li("${e}")
		if (t.cause != null) {
			li() {errItem(b, t.cause)}
		}
	}
}

def errPage(b, uuid) {
	b.html() {
		head() {
			link(href: "ipmems.css", rel: "stylesheet", type: "text/css")
		}
		body() {
			errItem(b, userMap.throwns.get(uuid))
		}
	}
}

def xml = new MarkupBuilder(queryData.addWriter("main"));

if (!uriProps.containsKey("frame") && !uriProps.containsKey("logkey") &&
	!uriProps.containsKey("uuid")) mainPage(xml);
else if (uriProps.containsKey("frame")) {
	switch (uriProps.frame) {
		case "top":
			topPage(xml);
			break;
		case "bottom":
			logPage(xml, "sys", acl);
			break;
	}
} else if (uriProps.containsKey("logkey")) logPage(xml, uriProps.logkey, acl);
else if (uriProps.containsKey("uuid")) errPage(xml, uriProps.uuid);

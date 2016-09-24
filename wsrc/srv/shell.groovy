import groovy.xml.MarkupBuilder;

def acl = queryProps.getProperty("Accept-Language", "en");

def mainPage(b) {
	println("Content-Type: text/html; charset=UTF-8");
	println();
	b.html() {
		head() {
			title(ipmems.logo)
		}
		frameset(rows: "55,*", cols: "*") {
			frame(src: "shell.groovy?frame=top", name: "topFrame")
			frame(src: "shell.groovy?frame=bottom", name: "bottomFrame")
		}
	}
}

def formPage(b, l) {
	println("Content-Type: text/html; charset=UTF-8");
	println();
	b.html() {
		head() {
			link(href: "ipmems.css", rel: "stylesheet", type: "text/css")
		}
		body() {
			form(method: "get", action: "shell.groovy", target: "bottomFrame") {
				p() {
					b("${locStr(l, "Command")}: ")
					input(type: "text", size: 50, name: "command")
					input(type: "submit", value: "${locStr(l, "Submit")}")
				}
			}
		}
	}
}

def cmdPage(b, cmd) {
	println("Content-Type: text/plain; charset=UTF-8");
	println();
	def process = cmd.execute();
	def txt = process.inputStream.getText("UTF-8");
	println(txt);
	txt = process.errorStream.getText("UTF-8");
	println(txt);
}

def xml = new MarkupBuilder(queryData.addWriter("main"));

if (!uriProps.containsKey("frame") && !uriProps.containsKey("command"))
	mainPage(xml);
else if (uriProps.containsKey("frame")) {
	switch (uriProps.frame) {
		case "top":
			formPage(xml, acl);
			break;
		case "bottom":
			xml.html() {
				body()
			}
			break;
	}
} else if (uriProps.containsKey("command")) cmdPage(xml, uriProps.command);
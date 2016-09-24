import groovy.xml.MarkupBuilder;

println("Content-Type: text/html; charset=UTF-8");
println();

def acl = queryProps.getProperty("Accept-Language", "en");
def xml = new MarkupBuilder(
	queryData.addResource("w", new OutputStreamWriter(out, "UTF-8")));

xml.html() {
	head() {
		link(rel: "stylesheet", type: "text/css", href: "shop.css")
	}
	body(bgcolor: "#EEEEEE") {
		h1("TOP")
	}
}
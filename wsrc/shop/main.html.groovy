import groovy.xml.MarkupBuilder;

println("Content-Type: text/html; charset=UTF-8");
println();

def acl = queryProps.getProperty("Accept-Language", "en");
def xml = new MarkupBuilder(
	queryData.addResource("w", new OutputStreamWriter(out, "UTF-8")));

if (!("cid" in uriProps)) {
	xml.html() {
		head() {
			link(rel: "stylesheet", type: "text/css", href: "shop.css")
		}
		body(bgcolor: "#CCCCCC") {
			p(locStr(acl, "Start page"))
		}
	}
	return;
}

xml.html() {
	head() {
		link(rel: "stylesheet", type: "text/css", href: "shop.css")
	}
	body(bgcolor: "#CCCCCC") {
		def cats = eval(new File(queryData.file.parent, "goods.groovy"));
		def cat = cats[uriProps.cid];
		h1(cat.name)
		table(border: "1", width: "100%", class: "mt") {
			col(width: "150px")
			col(width: "*")
			col(width: "200px")
			col(width: "80px")
			tr(class: "mrh") {
				th(locStr(acl, "Image"), [class: "mh"])
				th(locStr(acl, "Description"), [class: "mh"])
				th(locStr(acl, "Price"), [class: "mh"])
				th(locStr(acl, "Order"), [class: "mh"])
			}
			cat.items.each{ik, iv ->
				tr(class: "mr") {
					td(align: "center", class: "md") {
						img(src: iv.image)
					}
					td(iv.name, [class: "md"])
					td("${iv.price}", [align: "right", class: "md"])
					td(align: "center", class: "md") {
						def url = mkUrl("basket.html", [
							sid: uriProps.sid,
							cid: uriProps.cid,
							gid: ik,
							op: "add"
						]);
						a(target: "basketFrame", href: url, class: "mda") {
							img(src: "basket.png")
						}
					}
				}
			}
		}
	}
}
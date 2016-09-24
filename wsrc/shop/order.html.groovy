import groovy.xml.MarkupBuilder;

println("Content-Type: text/html; charset=UTF-8");
println();

def acl = queryProps.getProperty("Accept-Language", "en");
def xml = new MarkupBuilder(
	queryData.addResource("w", new OutputStreamWriter(out, "UTF-8")));

// Получаем объект сессии по идентификатору (это будет ассоциативный список).
def session = userMap["webSessions"][uriProps.sid];
// Получаем объект корзины.
def basket = session["basket"];

if ("org" in uriProps) {
	xml.html() {
		head() {
			link(rel: "stylesheet", type: "text/css", href: "shop.css")
		}
		body(bgcolor: "#EEEEEE") {
			def orderDir = new File(sysProp("jarDir"), "orders");
			if (!orderDir.exists()) orderDir.mkdir();
			def orderFile = File.createTempFile("ESS-", ".txt", orderDir);
			def data = [basket: basket, data: uriProps];
			orderFile.text = json(data);
			def n = orderFile.name.replace(".txt", "");
			p("Заказ $n был успешно сформирован. С вами свяжутся.", [
				class: "op"
			])
		}
	}
} else {
	xml.html() {
		head() {
			link(rel: "stylesheet", type: "text/css", href: "shop.css")
		}
		body(bgcolor: "#EEEEEE") {
			def fp = [
				action: "order.html",
				target: "_self",
				method: "get",
				class: "of"
			];
			form(fp) {
				input(type: "hidden", name: "sid", value: uriProps.sid)
				table(width: "100%") {
					tr() {
						td(locStr(acl, "Organisation") + ":")
						td() {
							input(type: "text", size: "80", name: "org")
						}
					}
					tr() {
						td(locStr(acl, "First name") + ":")
						td() {
							input(type: "text", size: "80", name: "fn")
						}
					}
				}
				input(type: "submit")
			}
		}
	}
}
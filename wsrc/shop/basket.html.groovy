import groovy.xml.MarkupBuilder;

println("Content-Type: text/html; charset=UTF-8");
println();

def acl = queryProps.getProperty("Accept-Language", "en");
def xml = new MarkupBuilder(
	queryData.addResource("w", new OutputStreamWriter(out, "UTF-8")));

// Получаем объект сессии по идентификатору (это будет ассоциативный список).
def session = userMap["webSessions"][uriProps.sid];
// Получаем флаг инициализации корзины.
def basketInitFlag = session.get("bif", false);
// Если корзина не присутствует в объекте сессии, то создаем ее.
if (!("basket" in session)) session["basket"] = [].asSynchronized();
// Получаем объект корзины.
def basket = session["basket"];

if ("op" in uriProps) switch(uriProps.op) {
	// Добавляем товар
	case "add":
		synchronized(basket) {
			def updated = false;
			for (def item in basket) {
				if (item.cid == uriProps.cid && item.gid == uriProps.gid) {
					item.count++;
					updated = true;
					break;
				}
			}
			if (!updated) 
				basket << [cid: uriProps.cid, gid: uriProps.gid, count: 1];
		}
		break;
	// Очищаем корзину
	case "clear":
		synchronized(basket) {
			basket.clear();
		}
		break;
	// Удаляем товар
	case "remove":
		synchronized(basket) {
			def di = null;
			for (def item in basket) {
				if (item.cid == uriProps.cid && item.gid == uriProps.gid) {
					item.count--;
					if (item.count == 0) di = item;
					break;
				}
			}
			if (di != null) basket.remove(di);
		}
		break;
}

xml.html() {
	head() {
		link(rel: "stylesheet", type: "text/css", href: "shop.css")
	}
	body(bgcolor: "#AAAAAA") {
		if (!basketInitFlag && uriProps.op == "add") { // Показ корзины
			def st = 'parent.document.getElementById("fs").rows="200,*"';
			script(st, [type: "text/javascript"])
			session["bif"] = true;
		} else if (basketInitFlag && basket.empty) { // Скрытие корзины
			def st = 'parent.document.getElementById("fs").rows="0,*"';
			script(st, [type: "text/javascript"])
			session["bif"] = false;
		}
		if (!basket.empty) {
			// Читаем товары
			def cats = eval(new File(queryData.file.parent, "goods.groovy"));
			synchronized(basket) {
				def sum = 0.0;
				table(border: "1", width: "100%", class: "bt") {
					col(width: "80px")
					col(width: "*")
					col(width: "100px")
					col(width: "100px")
					col(width: "100px")
					tr(class: "br") {
						th(locStr(acl, "Number"), [class: "bh"])
						th(locStr(acl, "Name"), [class: "bh"])
						th(locStr(acl, "Price"), [class: "bh"])
						th(locStr(acl, "Count"), [class: "bh"])
						th(locStr(acl, "Total"), [class: "bh"])
					}
					// Перебираем корзину
					basket.eachWithIndex{item, idx ->
						def price = cats[item.cid].items[item.gid].price;
						def total = price * item.count;
						sum += total;
						tr(class: "br") {
							td("${idx + 1}", [class: "bd"])
							td(cats[item.cid].items[item.gid].name, [
									class: "bd"
							])
							td("${cats[item.cid].items[item.gid].price}", [
								align: "right",
								class: "bd"
							])
							td(class: "bd") {
								table(width: "100%", class: "bet") {
									col(width: "30px")
									col(width: "*")
									col(width: "30px")
									tr(class: "ber") {
										td(align: "left", class: "beminus") {
											a("-", [
												target: "_self",
												class: "bea",
												href: mkUrl("basket.html", [
													sid: uriProps.sid,
													cid: item.cid,
													gid: item.gid,
													op: "remove"
												])
											])
										}
										td("${item.count}", [class: "bed"])
										td(align: "right", class: "beplus") {
											a("+", [
												target: "_self",
												class: "bea",
												href: mkUrl("basket.html", [
													sid: uriProps.sid,
													cid: item.cid,
													gid: item.gid,
													op: "add"
												])
											])
										}
									}
								}
							}
							td("${total}", [align: "right", class: "bd"])
						}
					}
				}
				// Формируем итоговую строчку
				table(width: "100%", class: "bttot") {
					col(width: "*")
					col(width: "100px")
					tr(class: "brtot") {
						td(locStr(acl, "Total") + ":", [class: "bdtotl"])
						td("$sum", [align: "right", class: "bdtot"])
					}
				}
				def clearProps = [
					target: "_self",
					class: "babut",
					href: mkUrl("basket.html", [
						sid: uriProps.sid,
						op: "clear"
					])
				];
				def orderProps = [
					target: "mainFrame",
					class: "babut",
					href: mkUrl("order.html", [
						sid: uriProps.sid
					])
				];
				// Формируем ссылку на команду "Очистить корзину"
				// и "Сформировать заказ"
				table(width: "100%", class: "btbut") {
					tr(class: "brbut") {
						td(class: "bdbut") {
							a(locStr(acl, "Clear basket"), clearProps)
						}
						td(class: "bdbut") {
							a(locStr(acl, "Order"), orderProps)
						}
					}
				}
				// Формируем ссылку на платежную систему PayPal
				form(
					action: "https://www.paypal.com/cgi-bin/webscr",
					method: "post",
					target: "_top"
				) {
					input(
						type: "hidden",
						name: "cmd",
						value: "_s-xclick"
					)
					input(
						type: "hidden",
						name: "hosted_button_id",
						value: "BPJMVVDVJ8HTQ"
					)
					input(
						type: "image",
						name: "submit",
						alt: "PayPal - The safer, easier way to pay online!",
						border: "0",
						src: "https://www.paypalobjects.com/en_US/RU/i/btn/btn_buynowCC_LG.gif"
					)
					img(
						alt: "",
						border: "0",
						width: "1",
						height: "1",
						src: "https://www.paypalobjects.com/ru_RU/i/scr/pixel.gif"
					)
				}
			}
		}
	}
}
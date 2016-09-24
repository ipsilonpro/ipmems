import groovy.xml.MarkupBuilder;

// Печатаем браузеру информацию о содержимом страницы
println("Content-Type: text/html; charset=UTF-8");
println();

// Получаем сведения о наиболее предпочтительном языке для страницы
// Эту информацию передаёт браузер
def acl = queryProps.getProperty("Accept-Language", "en");

// Создаем объект-построитель HTML-файла.
// out - выходной поток (в браузер)
// queryData.addResource(...) - для автоматического закрытия потока
// MarkupBuilder - класс построителя HTML
def xml = new MarkupBuilder(
	queryData.addResource("w", new OutputStreamWriter(out, "UTF-8")));

// Если параметр сессии отсутствует, выдаем ошибку и выходим.
if (!("sid" in uriProps)) {
	xml.html() {
		head() {
			title(locStr(acl, "Electroshop"))
		}
		body() {
			h1(locMsg(acl, "Invalid session"))
		}
	}
	return;
}

// Формируем HTML начальной страницы
xml.html() {
	head() {
		title(locStr(acl, "Electroshop"))
		link(rel: "icon", type: "image/png", href: "favicon.png")
		link(rel: "stylesheet", type: "text/css", href: "shop.css")
	}
	frameset(rows: "100,*,40", cols: "*", border: "0") {
		frame(
			src: mkUrl("top.html", [sid: uriProps.sid]),
			name: "topFrame",
			scrolling: "no",
			noresize: "1"
		)
		frameset(rows: "*", cols: "250,*", border: "0") {
			frame(
				src: mkUrl("left.html", [sid: uriProps.sid]),
				name: "leftFrame",
				scrolling: "no",
				noresize: "1"
			)
			frameset(rows: "0,*", cols: "*", id: "fs", border: "0") {
				frame(
					src: mkUrl("basket.html", [sid: uriProps.sid]),
					name: "basketFrame",
					noresize: "1"
				)
				frame(
					src: mkUrl("main.html", [sid: uriProps.sid]),
					name: "mainFrame",
					noresize: "1"
				)
			}
		}
		frame(
			src: mkUrl("bottom.html", [sid: uriProps.sid]),
			name: "bottomFrame",
			scrolling: "no",
			noresize: "1"
		)
	}
}
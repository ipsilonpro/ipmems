import groovy.xml.MarkupBuilder;

println("Content-Type: text/html; charset=UTF-8");
println();

def acl = queryProps.getProperty("Accept-Language", "en");
def xml = new MarkupBuilder(
	queryData.addResource("w", new OutputStreamWriter(out, "UTF-8")));

xml.printer.println("<!DOCTYPE html>");
xml.html() {
	head() {
		title('АСУТ Магазина "Славный"')
		link(rel: "stylesheet", type: "text/css", href: "ezh.css")
		script("", [type: "text/javascript", src: "ezh.js"])
	}
	body(onload: "onload()") {
		table(class: "pumps", width: "100%") {
			tr() {
				th("Насосы", [colspan: "5"])
			}
			tr(class: "terminator") {
				th("Усл. обозначение")
				th("Состояние")
				th("Температура")
				th("Давление")
				th("Управление")
			}
			tr() {
				td("ЦН")
				td("-", [id: "nc", align: "center"])
				td("-", [id: "tc", align: "right"])
				td("-", [id: "pc", align: "right"])
				td(align: "center") {
					button("Вкл", [id: "pumpc_off"])
					button("Выкл", [id: "pumpc_on"])
				}				
			}
			for (def i = 1; i <= 11; i++) {
				tr() {
					td("Н$i")
					td("-", [id: "n$i", align: "center"])
					td("-", [id: "t$i", align: "right"])
					td("-", [id: "p$i", align: "right"])
					td(align: "center", id: "P$i") {
						button("Вкл", [
							id: "pump${i}_off",
							onclick: "pump_off(event)"
						])
						button("Выкл", [
							id: "pump${i}_on",
							onclick: "pump_on(event)"
						])
					}
				}
			}
		}
		hr()
		table(class: "valves", width: "100%") {
			tr() {
				th("Клапаны", [colspan: "5"])
			}
			tr(class: "terminator") {
				th("Усл. обозначение")
				th("Температура")
				th("Давление до")
				th("Давление после")
				th("Управление")
			}
			for (def i = 1; i <= 2; i++) {
				tr() {
					td("K$i")
					td("-", [id: "T$i", align: "right"])
					td("-", [id: "P1$i", align: "right"])
					td("-", [id: "P2$i", align: "right"])
					td(align: "center", id: "V$i") {
						button("Закрыть", [
							id: "valve${i}_close",
							onclick: "valve_close(event)"
						])
						button("Призакрыть", [
							id: "valve${i}_semiclose",
							onclick: "valve_semiclose(event)"
						])
						button("Приоткрыть", [
							id: "valve${i}_semiopen",
							onclick: "valve_semiopen(event)"
						])
						button("Открыть", [
							id: "valve${i}_open",
							onclick: "valve_open(event)"
						])
					}
				}
			}
		}
		hr()
		table(class: "results", width: "100%") {
			tr(class: "terminator") {
				th("Журнал")
				th() {
					button("Очистить", [onclick: "clear_log()"])
				}
			}
			tr() {
				td(colspan: "2", id: "results")
			}
		}
	}
}
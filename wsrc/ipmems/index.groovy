import groovy.xml.MarkupBuilder;
import org.ipsilon.ipmems.IpmemsIntl;

println("Content-Type: text/html; charset=UTF-8");
println("Content-Encoding: gzip");
println();

def acl = uriProps.get("lang", queryProps.getProperty("Accept-Language", "en"));
def lang = IpmemsIntl.getLocale(acl).language.toLowerCase();
def xml = new MarkupBuilder(queryData.addGzipWriter("main"));
def langsFile = new File(queryData.directory, "langs.txt");
def langs = langsFile.getText("ISO-8859-1").split(";") as List;
lang = lang in langs ? lang : "en";

xml.printer.println("<!DOCTYPE html>");
xml.html() {
	head() {
		title("IPMEMS: " + locStr(acl, "the cross-platform data acquisition software"))
		meta("http-equiv": "Content-Type", content: "text/html; charset=utf-8")
		link(rel: "stylesheet", type: "text/css", href: "ipmems.css")
	}
	body() {
		table(width: "100%", cellspacing: "10px", class: "head") {
			col(width: "*")
			for (def l in langs) col(width: "48px")
			tr() {
				td() {
					table() {
						col(width: "*")
						col(width: "20px")
						col(width: "*")
						col(width: "20px")
						col(width: "*")
						col(width: "20px")
						col(width: "*")
						col(width: "20px")
						col(width: "*")
						tr() {
							td(rowspan: "2") {img(src: "ipmemsic.png", alt: "IPMEMS")}
							td(" ")
							td() {a(locStr(acl, "Initial"), [href: mkUrl("", [s: "main", lang: lang])])}
							td(" ")
							td() {a(locStr(acl, "Examples"), [href: mkUrl("", [s: "examples", lang:lang])])}
							td(" ")
							td() {a(locStr(acl, "Open source"), [href: mkUrl("http://sourceforge.net/projects/ipmems/", [:])])}
							td(" ")
							td() {a(locStr(acl, "Contacts"), [href: mkUrl("", [s: "contacts", lang: lang])])}
						}
						tr() {
							td(" ")
							td() {a(locStr(acl, "News"), [href: mkUrl("", [s: "news", lang: lang])])}
							td(" ")
							td() {a(locStr(acl, "Downloads"), [href: mkUrl("", [s: "downloads", lang: lang])])}
							td(" ")
							td() {a(locStr(acl, "Future plans"), [href: mkUrl("", [s: "fp", lang: lang])])}
							td(" ")
							td() {a(locStr(acl, "About"), [href: mkUrl("", [s: "about", lang: lang])])}
						}
					}
				}
				for (def l in langs) td(valign: "center") {
					def hrefProps = [lang: l];
					if ("s" in uriProps) hrefProps.put("s", uriProps.s);
					a(href: mkUrl("", hrefProps)) {
						img(src: l + ".png", alt: l)
					}
				}
			}
		}
		def section = uriProps.getProperty("s", "main");
		def m = new File(queryData.directory, "${section}_${lang}.groovy");
		def f = new File(queryData.directory, "footer_${lang}.groovy");
		if (m.exists()) eval(m)(xml);
		if (f.exists()) eval(f)(xml);
	}
}
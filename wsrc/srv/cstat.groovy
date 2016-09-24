import groovy.xml.MarkupBuilder;
import java.sql.Timestamp;

println("Content-Type: text/html; charset=UTF-8");
println();

def acl = queryProps.getProperty("Accept-Language", "en");
def xml = new MarkupBuilder(queryData.addWriter("main"));

def period = uriProps.getProperty("p", "60") as int;
def msg = uriProps.getProperty("s", "UPDT_OUT") + " {0}";
def db = uriProps.getProperty("d", "db");

def addrs = [];
def n = 0;
def ds = [];

xml.html() {
	head() {
		link(href: "ipmems.css", rel: "stylesheet", type: "text/css")
	}
	body() {
		table(width: "100%") {
			tr() {
				th(locStr(acl, "Address"))
				th(locStr(acl, "Timestamp"))
				th(locStr(acl, "Delta"))
			}
			dbServer.gate("logging", db).last(msg: msg, dt: period).rows.each{
				def addr = it.aid instanceof String ? it.aid :
					ipmems.da.encode("P8", it.aid);
				def tm = it.t;
				def d = (System.currentTimeMillis() - tm.time) / 1000;
				if (!(addr in addrs)) {
					tr() {
						td(addr)
						td(tm)
						td(d)
					}
					n++;
					addrs << addr;
					ds << d;
				}
			}			
		}
		p() {
			u() {b(locStr(acl, "Total"))}
			b(": ")
			b(n)
		}
		p() {
			u() {b(locStr(acl, "Max delta"))}
			b(": ")
			b(ds.max())
		}
		p() {
			u() {b(locStr(acl, "Min delta"))}
			b(": ")
			b(ds.min())
		}
	}
}

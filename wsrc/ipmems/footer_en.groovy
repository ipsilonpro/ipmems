{xml ->
	xml.hr()
	def txt = "This site runs on IPMEMS ${ipmems.fullVersion}. " +
		"The content is gzipped online on server side. " +
		"The server is connected to Internet by mobile broadband dongle.";
	xml.p(txt, [class: "tinyFont"])
	xml.p("© 2012 Ipsilon-Pro L.L.C.", [class: "tinyFont"])
	xml.p("Feel free to write us at ipsilon.pro at gmail dot com.", [class: "tinyFont"])
}
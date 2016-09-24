{xml ->
	xml.hr()
	def txt = "Este sitio Web funciona por medio de IPMEMS ${ipmems.fullVersion}. " + 
		"El contenido está comprimido por GZIP del lado del servidor el cual está " + 
		"conectado a Internet a través de la Banda Ancha Móvil.";
	xml.p(txt, [class: "tinyFont"])
	xml.p("© 2012 Ipsilon-Pro L.L.C.", [class: "tinyFont"])
	xml.p("Si usted tiene preguntas o propuestas, por favor, escribanos a ipsilon.pro arroba gmail punto com y le responderemos lo más pronto posible.", [class: "tinyFont"])
}
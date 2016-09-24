// Если не сущестует реестра Web-сессий, создаем его.
// Реестр выглядит вначале как синхронизированный ассоциативный список ([:]).
synchronized(userMap) {
	if (!("webSessions" in userMap))
		userMap["webSessions"] = [:].asSynchronized();
}
// Удаляем из реестра все сессии старше 1 часа.
// Для перебора сессий (each) требуется потоковая синхронизация
// для ресстра Веб-сессий.
def curTime = System.currentTimeMillis();
synchronized(userMap["webSessions"]) {
	def dks = [];
	userMap["webSessions"].each{sk, sm ->
		if ((curTime - sm.start) > 3600000) dks += sk;
	}
	for (def dk in dks) userMap["webSessions"].remove(dk);
}
// Создаем идентификатор сессии по умолчанию. Он формируется как
// псевдо-уникальный 16-байтный ключ. Вероятность совпадения этих ключей
// при генерации их в количестве 1 млн. столь же мала,
// как и вероятность падения метеорита на голову.
def sid = UUID.randomUUID().toString();
// Регистрируем новую сессию с идентификатором sid.
userMap["webSessions"][sid] = 
	[start: System.currentTimeMillis()].asSynchronized();
// Перенаправляем страницу на shop.html с параметром sid.
println("Location: " + mkUrl("shop.html", [sid: sid]));
println("Connection: close");
println();
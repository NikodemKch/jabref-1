Build Dokumentation

1. Ausführen aller Unit Tests.

   Um die Unit Tests auszuführen, nutze den Befehl`.\gradlew test`. 

2. Überprüfen, ob der Code den Coding Guidelines entspricht.

   Zur Überprüfung der Coding Guidelines benutzen Sie `.\gradlew checkstyleMain` `.\gradlew checkstyleJmh` `.\gradlew checkstyleTest`.

3. Ein Projekt für Eclipse erstellen

   Ein Projekt für Eclipse erstellen Sie via `.\gradlew eclipseProject`.

4. Erstellen eines Zip Files, welches nach dem Entpacken eine lauffähige Version von JabRef enthält.

   Die Zip Files werden durch `.\gradlew distZip` erstellt. 

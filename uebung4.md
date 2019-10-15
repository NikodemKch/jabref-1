1. CI oder Continous Integration ist eine Hilfe an Softwareentwickler, um das Koordinieren mit anderen Softwareentwicklern zu gewährleisten. Somit können einzelne Entwickler parallel zueinander arbeiten und ihre Changes schnell und einfacher integrieren. Die Changes werden jeweils kompiliert und durch Richtlinien überprüft, ob das zu Problemen führen würde. 

2. Travis, eine Open-Source Software, ist für die CI in Github zuständig. Das Erstellen von Projekten und Testen wird durch Travis vereinfacht. 

3. .travis.yml-Datei beinhaltet die Konfiguration, womit Travis sich Information verschafft wie sie die CI-Pipeline behandlen soll. 

4. Im "Branches" Eintrag werden die Branches, die jeweils in diesem Eintrag stehen, überprüft über Travis CI. 

5. Die Buildmatrix ist dazu da, mehrere sogenannte "Jobs" im selben Build auszuführen. Pro Build erreicht Travis CI etwa 200 Jobs.

6. Nach einem Push auf einen der von Travis CI überprüften Branches wird dieser bearbeitet und bekommt automatisch eine Rückmeldung ob alle Bedinungen über Travis erfüllt worden sind.   


Was macht Travis? Bei kleinen Abänderungen im Code hat sich der Build in Travis beschwert. Alle Checks sind durchgefallen (ausser die die Allowed waren, die habe ich nicht mehr angeschaut). 

Pull-Request Aufgabe: Bei Versuch eines Pull Requests wird unterbrochen. Es steht "Can't automatically merge.".
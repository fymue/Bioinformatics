# FastaGen (Uni Homework project)

## Kompilierung und Ausführung des Programmes

Dieses Programm generiert eine vom Benutzer festgelegte Anzahl an zufälligen Nukleotid- oder Peptidsequenzen, welche eine zufällige Länge zwischen einem ebenfalls vom Benutzer festgelegten Minimal- und Maximalwert besitzen. Zusätzlich werden für Nukleotidsequenzen der GC-Gehalt, das Molekulargewicht sowie die Schmelztemperatur berechnet. Die Generierung bzw. Berechnung der Sequenzen wird auf mehrere Kerne ausgelagert. Die Sequenzen werden standardmäßig auf der Konsole ausgegeben, können aber auch in einer Datei im Fasta-Format gespeichert werden. Das Programm ist außerdem in der Lage, genomische Fasta-Dateien einzulesen, ggf. zu modifizieren und auszugeben bzw. modifiziert zu speichern. Die Benutzung bzw. Ausführung des Programmes kann sowohl über die Kommandozeile als auch über ein GUI erfolgen (wird demnächst hinzugefügt). Zur Ausführung des Programmes muss das Paket *fastagen* zunächst mit dem Befehl
```
javac -d /path/to/bin/ -cp /path/to/junit.jar --module-path path/to/javafx/lib --add-modules javafx.controls,javafx.fxml fastagen/*.java && cp *.fxml path/to/bin/fastagen/
```
kompiliert werden. Der Zusatz "-cp /path/to/junit.jar" ist nötig, da ein Teil des Programmes mittels des JUnit Framework in einem Unit-Test getestet wird (falls gewünscht). Es wird also neben einer aktuellen Version des [JDK](https://www.oracle.com/java/technologies/downloads/) auch das JUnit Framework in Form einer [.jar-Datei](https://search.maven.org/remotecontent?filepath=junit/junit/4.13.2/junit-4.13.2.jar) sowie [Hamcrest-core](https://search.maven.org/remotecontent?filepath=org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar) zur Durchführung des Unit-Tests benötigt. Wird dies nicht gewünscht kann die entsprechende Testklasse *SequenceTest.java* einfach nicht mitkompiliert werden. Zudem wird die [JavaFX-Library](https://gluonhq.com/products/javafx/) für die Ausführung im GUI-Modus benötigt. Im Anschluss kann die Main-Klasse über den Befehl
```
java -cp /path/to/bin/ -module-path path/to/javafx/lib/ --add-modules javafx.controls,javafx.fxml fastagen.Main (read|generate) (Protein|Genome) [Options]
```
ausgeführt werden. Der Unit-Test kann ebenfalls (separat) über die Kommandozeile über den den Befehl
```
java -cp /path/to/bin/:/path/to/junit.jar:/path/to/hamcrest-core.jar org.junit.runner.JUnitCore fastagen.SequenceTest
```
durchgeführt werden.

## Parameter und Optionen

Bei der Ausführung des Programmes können vom Benutzer die folgenden Parameter bzw. Optionen übergeben werden:

### Pflichtparameter

* *read* oder *generate* : Modus des Programmes (Einlesen einer Fasta-Datei oder Generierung einer neuen Fasta-Datei)
* *Protein* oder *Genome* : Das zu verwendende Alphabet für die Sequenzgenerierung bzw. für das Einlesen einer Fasta-Datei (*Genome*: DNA-Basen bzw. -Nukleotide, *Protein*: 1-Buchstaben-Kürzel der Aminosäuren; das Einlesen von Protein-Fastadateien wird noch nicht unterstützt)

### Optionen

#### *generate*-Modus:

* *-e, --entries \<total_entries\>* : Die Anzahl der zu generierenden Einträge bzw. Sequenzen (default: 1)
* *-l, --length \<min_length..max_length\>* : Die minimale und maximale Länge der zufällig generierten Sequenzen (beides inklusive, Format: *zahl1..zahl2*, default: 20..30)
* *-t, --threads < threads>* : Die Anzahl der zu verwendenen Threads/Kerne für die Sequenzgenerierung (Default: 3/4 der verfügbaren Kerne)
* *-o, --out \<file_name\>* : Der Pfad bzw. Name der Datei, in welcher die erzeugten Einträge gespeichert werden sollen (default: Ausgabe auf der Konsole)
* *-q, --quiet* : Wird dieser Parameter mitgegeben, so werden keinerlei Runtime Informationen auf der Konsole ausgegeben
* *--write-properties* : Wird dieser Parameter mitgegeben, so werden neben den Sequenzen auch die Nukleotidsequenz-Eigenschaften (z.B. GC-Gehalt) als Kommentarzeilen in die Fasta-Ausgabedatei geschrieben

#### *read*-Modus:

* *-o, --out* : Wir dieser Parameter mitgegeben, so werden die eingelesenen Einträge in einer neuen Datei mit dem Namen *inputfile_parsed.fna* (Name der Eingabedatei ohne Dateiendung + "_parsed.fna") gespeichert (default: Ausgabe auf der Konsole)
* *-q, --quiet* : Wird dieser Parameter mitgegeben, so werden keinerlei Runtime Informationen auf der Konsole ausgegeben
* *--write-properties* : Wird dieser Parameter mitgegeben, so werden neben den Sequenzen auch die Nukleotidsequenz-Eigenschaften (z.B. GC-Gehalt) als Kommentarzeilen in die Fasta-Ausgabedatei geschrieben

Diese Hilfe kann auch bei der Programmausführung u.a. mit dem Befehl
```
java -cp /path/to/bin/ -module-path path/to/javafx/lib/ --add-modules javafx.controls,javafx.fxml fastagen.Main --help
```
ausgegeben werden.

Nach Durchlauf des Programmes werden die Sequenzen entweder auf der Konsole ausgegeben oder in der Datei *file_name* bzw. im "read"-Modus in der Datei *inputfile_parsed.fna* gespeichert. Wird lediglich ein Dateiname ohne Dateipfad angegeben, wird die Datei im aktuellen Arbeitsverzeichnis gespeichert.






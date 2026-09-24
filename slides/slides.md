---
theme: '@gepardec/slidev-theme-gepardec'
title: Maven 4 – Änderungen & Migration
info: |
  Vortrag über Maven 4: Änderungen gegenüber Maven 3, gezeigt am
  Beispielprojekt maven4-project, und die Migration von Maven 3 auf Maven 4.
transition: fade
mdc: true
---

---
layout: cover
---

# Maven 4

## Änderungen & Migration

Robin P. Fischer

September 2026

---
layout: agenda
---

# Fragen

- Warum gibt es Maven 4?
- Was ist neu im POM?
- Was gibts noch für Features?
- Wie migriere ich von 3 auf 4?

---
layout: default
---

# Status Quo

- Maven 3 ist seit 2010 im Einsatz, das POM-Modell 4.0.0 sogar seit Maven 2 (2005)
- Ein POM hat **zwei Aufgaben**: Bauanleitung für uns *und* Beschreibung für alle, die das Artefakt konsumieren
- Jede Schema-Änderung würde deshalb das Ökosystem brechen (Maven Central, IDEs, Gradle & Co.)
- Maven 4 löst das Dilemma, indem es diese beiden Aufgaben trennt
- Status: **4.0.0-rc-6** (Stand 09/2026) – noch nicht GA, aber die richtige Zeit zum Testen

---
layout: two-cols-header
---

# Die Kernidee: 
# Build-POM / Consumer-POM

::left::

### Build-POM
Die `pom.xml` in unserem Repository

- Darf Model **4.1.0** nutzen
- Enthält Build-Konfiguration, Plugins, Test- und `provided`-Abhängigkeiten
- Darf Abkürzungen nutzen: `<parent/>`, Abhängigkeiten ohne Version, `${revision}`

::right::

### Consumer-POM
Wird beim `install`/`deploy` generiert

- Immer Model **4.0.0**
- Enthält nur, was Konsumenten brauchen
- Alle Werte aufgelöst – lesbar für Maven 3, Gradle und jedes andere Tool

::bottom::

Alle neuen Features leben im Build-POM – das Ökosystem bekommt davon nichts mit. **Genau das sehen wir uns am Beispielprojekt an.**

---
layout: two-cols-header
---

# Das Beispielprojekt:

::left::

```text
maven4-project/
├── pom.xml          example-parent
├── example-bom/     packaging: bom
├── example-domain/  jar
├── example-service/ jar  → domain
└── example-webapp/  war  → service
```

::right::

- **example-parent** – Build-Konfiguration & Plugin-Versionen
- **example-bom** – Versionen für externe Konsumenten
- **domain → service → webapp** – klassische Abhängigkeitskette im Reactor

::bottom::

Alle POMs nutzen das Model 4.1.0. und neue Features

---
layout: default
---

# Root-POM: Projekt-Root & Model 4.1.0

```xml {2,3,4,5}
<project xmlns="http://maven.apache.org/POM/4.1.0"
         xsi:schemaLocation="... https://maven.apache.org/xsd/maven-4.1.0.xsd"
         root="true">
  <modelVersion>4.1.0</modelVersion>
```

- `root="true"` markiert das Rootverzeichnis des Projekts → verfügbar als `${project.rootDirectory}`
- Alternative: ein `.mvn`-Ordner im Rootverzeichnis
- Model 4.1.0 gilt **nur für das Build-POM** – das Consumer-POM wird auf 4.0.0 zurückgestuft
- Model 4.0.0 bleibt voll unterstützt – 4.1.0 ist optional

<!--
Datei: pom.xml (Root), ganz oben.
Beide TODOs: root="true" und modelVersion.
-->

---
layout: two-cols-header
---

# CI-friendly Versionen ohne Plugin

::left::

### Maven 3

```xml
<version>${revision}</version>
...
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>flatten-maven-plugin</artifactId>
  <!-- sonst landet ${revision}
       unaufgelöst im Repository -->
</plugin>
```

::right::

### Maven 4

```xml
<version>${revision}</version>

<properties>
  <revision>1.0-SNAPSHOT</revision>
</properties>
```

::bottom::

`mvn verify -Drevision=1.2.3` setzt die Version für alle Subprojekte – und im Consumer-POM steht der aufgelöste Wert. Das `flatten-maven-plugin` wird überflüssig.

---
layout: two-cols-header
---

# Modules → Subprojects

::left::

### Maven 3

```xml
<modules>
  <module>example-bom</module>
  <module>example-domain</module>
  <module>example-service</module>
  <module>example-webapp</module>
</modules>
```

::right::

### Maven 4 

```xml
<subprojects>
  <subproject>example-bom</subproject>
  <subproject>example-domain</subproject>
  <subproject>example-service</subproject>
  <subproject>example-webapp</subproject>
</subprojects>
```

::bottom::

Neuer Name, um Verwechslungen mit Java-Modulen (JPMS) zu vermeiden. Die Liste könnte sogar ganz entfallen: Bei `packaging pom` ohne `<subprojects>` findet Maven 4 alle direkten Unterordner mit `pom.xml` automatisch.

---
layout: two-cols-header
---

# Subprojekte: Parent-Inferenz

::left::

### Maven 3

```xml
<parent>
  <groupId>com.gepardec.maven-training</groupId>
  <artifactId>example-parent</artifactId>
  <version>1.0-SNAPSHOT</version>
</parent>
<artifactId>example-domain</artifactId>
```

::right::

### Maven 4 

```xml
<parent/>
<artifactId>example-domain</artifactId>
```

::bottom::

groupId, artifactId und Version des Parents werden aus `../pom.xml` gelesen, groupId und Version des Projekts vererbt.

Ein Wunsch aus 2005 (MNG-624) ist endlich erfüllt – und bei einem Release muss die Version nur noch an **einer** Stelle geändert werden.
https://issues.apache.org/jira/browse/MNG-624
---
layout: two-cols-header
---

# Abhängigkeiten innerhalb des Reactors

::left::

### Maven 3

```xml
<!-- im Parent -->
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.gepardec.maven-training</groupId>
      <artifactId>example-service</artifactId>
      <version>${project.version}</version>
    </dependency>
    ...
```

::right::

### Maven 4

```xml
<dependency>
  <groupId>com.gepardec.maven-training</groupId>
  <artifactId>example-service</artifactId>
</dependency>
```

::bottom::

Maven 4 leitet die Version von Projekten im Reactor selbst ab. Im Parent braucht es dafür kein `dependencyManagement` mehr

---
layout: two-cols-header
---

# Neues Packaging: `bom`

::left::

### Maven 4

```xml
<parent/>
<artifactId>example-bom</artifactId>
<packaging>bom</packaging>

<dependencyManagement>
  <!-- domain, service, webapp
       mit ${project.version} -->
</dependencyManagement>
```

::right::

### Nutzung in einem externen Projekt

```xml
<dependency>
  <groupId>com.gepardec.maven-training</groupId>
  <artifactId>example-bom</artifactId>
  <version>1.0-SNAPSHOT</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

::bottom::

`bom` trennt klar: **BOM** = Versionen für Konsumenten, **Parent** = Build-Konfiguration. Im Consumer-POM wird daraus `packaging pom` – Maven 3 kann es normal importieren. Die BOM **nicht** im eigenen Reactor importieren: Maven 4 warnt davor. Im Parent importieren wir deshalb nur die externe `junit-bom`.

---
layout: default
---

# Plugin-Versionen immer fixieren

```xml
<pluginManagement>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <!-- keine Version → Warnung im Log -->
    </plugin>
    <plugin>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>3.5.3</version>
    </plugin>
```

- Das Super-POM von Maven 4 liefert neue Default-Versionen für Core-Plugins
- Ohne fixierte Version kann sich das Build-Verhalten beim Maven-Update unbemerkt ändern
- Maven 4 **warnt**, wenn eine Version aus dem Super-POM kommt

---
layout: statement
---

# Build-POM ist für uns
# **Consumer-POM für Consumer**

---
layout: default
---

# Lifecycle: von der Liste zum Baum

- Maven 3: Lifecycle als lineare, geordnete Liste von Phasen
- Maven 4: Lifecycle als **Baum** – feingranulare Abhängigkeiten zwischen Phasen
- Neuer `concurrent`-Builder (`-b concurrent`) für parallelere Builds
- Jede Phase hat `before:`- und `after:`-Varianten, `pre-*` / `post-*` sind nur noch deprecated Aliase

<br>

**Praxis-Beispiel: Entkoppelte Integrationstests**

**Maven 3:** example-webapp ist blockiert, bis example-service *komplett* durchgelaufen ist – inklusive den Tests in der `verify`-Phase. 

**Maven 4:** Concurrent builder aktivieren: `-b concurrent` 

example-webapp startet `compile` sofort, nachdem example-service `package`durchlaufen hat.
---
layout: default
---

# Workflow & Profile

- `--resume` / `-r` merkt sich automatisch das zuletzt fehlgeschlagene Subprojekt
- Konsistente SNAPSHOT-Timestamps über alle Subprojekte hinweg
- `all` / `each` Phasen für projektweite bzw. subprojektweite Hooks
- `--fail-on-severity WARN` (`-fos`) bricht den Build bei Warnungen ab – z. B. bei fehlenden Plugin-Versionen
- Bedingte Profilaktivierung über `<condition>` (Dateien, Property-Vergleiche, Verknüpfungen)
- Fehlende Profile brechen den Build ab – `-P?profilname` markiert ein Profil als optional
- **Breaking Change:** `deployAtEnd` / `installAtEnd` sind jetzt standardmäßig `true`

<!--
 --resume zeigen -> messageservice error und log
  -fos WARN 
  -P\?blubb
(option+shift+7)
-->
---
layout: quadrants
---

# Tools & Sicherheit

::one::

### mvnenc
Komplett überarbeitete Verschlüsselung statt der alten „Passwort-Verschleierung" aus Maven 3.

::two::

### mvnsh (Maven Shell)
Hält einen Maven-Prozess dauerhaft offen – spart wiederholten JVM-Start.

::three::

### Maven Resolver 2.0
150+ Fixes, nativer Java-HTTP-Client, besseres Caching ...

Kurz: Es wird schneller

::four::

### Maven Upgrade Tool (mvnup)
Automatisiert große Teile der Migration von Maven 3 auf 4.

<!--
   mvnsh (mvn compile) exit
   mvnup check
-->

---
layout: default
---

# Voraussetzung: Java 17

- Maven 4 benötigt **Java 17**, um selbst ausgeführt zu werden
- Die Zielversion ist davon unabhängig
- Für Builds gegen ein anderes JDK: Maven Toolchains verwenden


---
layout: default
---

# Der 3-Stufen-Ansatz

- **Prepare** – Voraussetzungen mit aktuellem Maven 3 schaffen
- **Test** – parallel mit Maven 4 (RC) bauen, nur minimale Fixes
- **Migrate** – Maven 3 fallen lassen, optionale Maven-4-Features nutzen

---
layout: default
---

# Stufe 1 & 2: Prepare und Test

- Neueste Maven-3.9.x-Version verwenden
- Plugins auf die neueste **Maven-3-kompatible** Version heben (`versions:display-plugin-updates`)
- Build-Umgebung (lokal + CI) auf Java 17 umstellen, Maven 4 (RC) parallel installieren
- Typische Stolpersteine:
  - Doppelte Plugin-Deklarationen – jetzt Build-Fehler statt Warnung
  - Entfernte Properties `executionRootDirectory`, `multiModuleProjectDirectory` → `${project.rootDirectory}`
  - Bindungen an `pre-`/`post-`-Phasen → auf `before:`/`after:` umstellen

---
layout: default
---

# Stufe 3: Migrate

- Erst produktiv auf Maven 4 laufen, **danach** optionale Features einführen
- `deployAtEnd`/`installAtEnd` explizit auf `false` setzen, falls das alte Verhalten gewünscht ist
- Projekt-Root definieren (`root="true"` oder `.mvn`-Ordner)
- Schrittweise auf Model 4.1.0 wechseln – genau das, was wir im Projekt gesehen haben:
  `<subprojects>`, `<parent/>`, Reactor-Versionen, `${revision}`, `bom`-Packaging
- `mvnup check` zeigt, was sich ändern würde – `mvnup apply` setzt es um

---
layout: default
---

# Best Practices

- Jede Plugin-Version explizit deklarieren – Maven 4 hilft mit Warnungen
- `mvnsh` verwenden -> viel schneller
- `-r` verwenden für grosse Projekte
- Eigene BOMs mit `packaging bom` für Konsumenten erstellen

---
layout: statement
---

# Migration ist ein **Prozess,** 

# **kein Big-Bang-Schritt.**

---
layout: default
---

# Zusammenfassung

- Maven 4 trennt **Build-POM** und **Consumer-POM** – neue Features ohne Bruch im Ökosystem
- Es verschwindet viel Boilerplate: `<parent/>`, keine Versionen für Reactor-Abhängigkeiten, `${revision}` ohne Plugin
- `bom`-Packaging und `<subprojects>` machen die POMs klarer
- Migration funktioniert schrittweise – `mvnup` nimmt viel Arbeit ab
- Aktuell RC-Phase: jetzt testen, bevor GA kommt

---
layout: section
---

# Live-Demo

## Ab in die IDE

<!--
Zeigen pom.xml:
  example-parent
  example-bom
  example-webapp

  setMaven4 (mvn 4 setzen in terminal)

  mvn package -Dmaven.consumer.pom.flatten=true
  
example-webapp poms vergleichen
  
  --resume zeigen 
    -> messageservice error machen
    mvn package
    log zeigen
    fixen -> mvn package -r

    mvn package -fos WARN 
    mvn package -Pblubb (option+shift+7)
    mvn package -P\?blubb

    mvnsh (mvn compile) exit
    mvnup check
-->
---
layout: contact
name: Robin P. Fischer
role: DevOps-Engineer
email: robin.fischer@gepardec.com
photo: /contact.jpg
---

# Fragen?
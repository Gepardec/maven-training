---
theme: '@gepardec/slidev-theme-gepardec'
title: Maven 4 – Änderungen & Migration
info: |
  Vortrag über Maven 4: Änderungen gegenüber Maven 3, Best Practices
  und die Migration von Maven 3 auf Maven 4.
transition: fade
mdc: true
---

---
layout: cover
---

# Maven 4

## Änderungen  & Migration

Robin P. Fischer

September 2026

---
layout: agenda
---

# Agenda

- Warum Maven 4?
- Änderungen gegenüber Maven 3
- Best Practices
- Migration von 3 auf 4
- Zusammenfassung & Fragen

---
layout: section
---

# Einstieg

## Warum Maven 4?

---
layout: default
---

# Status Quo

- Maven 3 seit 2010 im Einsatz – das POM-Schema ist seit über 10 Jahren eingefroren
- Build-Informationen und Consumer-Informationen sind im selben POM vermischt
- Jede Schema-Änderung würde das ganze Ökosystem brechen (Maven Central, IDEs, andere Build-Tools)
- Maven 4 trennt diese Anliegen sauber – ohne das Ökosystem zu brechen
- Status: **4.0.0-rc-6** (Stand 08/2026) – noch nicht GA, aber bereits produktionsnah

---
layout: section
---


## Änderungen in Maven 4

---
layout: default
---

# Voraussetzung: Java 17

- Maven 4 benötigt **Java 17**, um selbst ausgeführt zu werden
- Kann gegen ältere Java-Versionen kompilieren
- Für andere JDK-Versionen: Maven Toolchains verwenden

---
layout: quadrants
---

# POM-Änderungen im Überblick

::one::

### Consumer-POM
Schlanke, generierte POM-Variante fürs Repository – ohne Build-Details wie Plugin-Konfiguration.

::two::

### Model Version 4.1.0
Neues, optionales Modell mit neuen Elementen. Model 4.0.0 bleibt weiterhin voll unterstützt.

::three::

### Module → Subprojects
`<modules>` wird zu `<subprojects>` – reduziert Verwechslung mit Java Platform Modules.

::four::

### Neues Packaging: bom
Eigener `bom`-Packaging-Typ trennt BOM-POMs klar von Parent-POMs.

---
layout: two-cols-header
---

# Automatische Versionierung

::left::

### Maven 3 (bisher)

```xml
<parent>
  <groupId>demo.maven</groupId>
  <artifactId>parent</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</parent>
```

::right::

### Maven 4 (Model 4.1.0)

```xml
<parent/>
```

::bottom::

Version, groupId und artifactId werden automatisch aus dem Dateisystem abgeleitet – ein 20 Jahre alter Wunsch (MNG-624, seit 2005!) wird endlich erfüllt.

---
layout: default
---

# Weitere Subproject-Verbesserungen

- Automatische Subproject-Discovery (kein explizites `<modules>` nötig)
- Volle Unterstützung für CI-friendly Variablen (`${revision}`) – **ohne** `flatten-maven-plugin`
- Konsistente SNAPSHOT-Timestamps über alle Subprojects hinweg
- `--resume` / `-r` merkt sich automatisch den zuletzt fehlgeschlagenen Build
- **Breaking Change:** `deployAtEnd` ist jetzt standardmäßig `true`

---
layout: default
---

# Lifecycle: vom Graph zum Baum

- Maven 3: Lifecycle als lineare, geordnete Liste von Phasen
- Maven 4: Lifecycle als **Baum** – feingranulare Abhängigkeiten zwischen Phasen
- Neuer `concurrent`-Builder (`-b concurrent`) nutzt das für parallele Builds
- Jede Phase hat jetzt `before:` und `after:` Varianten
- `pre-*` / `post-*` sind nur noch Aliase (deprecated)

```xml {2}
<execution>
  <phase>before:integration-test</phase>
  <goals><goal>setup-data</goal></goals>
</execution>
```

---
layout: default
---

# Weitere Workflow-Neuerungen

- `all` / `each` Phasen für projektweite bzw. subproject-weite Hooks
- `--fail-on-severity WARN` (Kurzform `-fos`) bricht den Build bei Warnungen ab
- Bedingte Profilaktivierung über `<condition>` (Dateien, Property-Vergleiche, Verknüpfungen)
- Fehlende Profile erzeugen einen Build-Abbruch
- Optionale Profile mit `-P?profilname` für altes Maven-3 Verhalten

---
layout: quadrants
---

# Tools & Sicherheit

::one::

### mvnenc
Komplett überarbeitete Verschlüsselung statt der alten "Passwort-Verschleierung" aus Maven 3.

::two::

### mvnsh (Maven Shell)
Hält einen Maven-Prozess dauerhaft offen – spart wiederholten JVM-Start.

::three::

### Maven Resolver 2.0
150+ Fixes, nativer Java-HTTP-Client, nicht mehr direkt von Plugins nutzbar.

::four::

### Maven Upgrade Tool (mvnup)
Automatisiert große Teile der Migration von Maven 3 auf 4.

---
layout: section
---

# Teil 2

## Best Practices

---
layout: default
---

# Plugin-Versionen immer fixieren

- Super-POM liefert neue Default-Versionen für Core-Plugins
- Ohne fixierte Version kann sich das Build-Verhalten unbemerkt ändern
- Maven 4 warnt, wenn eine Default-Version verwendet wird
- Empfehlung: **jede** Plugin-Version explizit deklarieren

---
layout: default
---

# Weitere Best Practices

- `mvn verify` statt `mvn clean install` für den Alltag verwenden
- `--resume` / `-r` statt manuellem `--resume-from :<modul>`
- Projekt-Root explizit definieren (`.mvn`-Ordner oder `root="true"`-Attribut)
- Verbose Plugin-Validierung aktivieren: `-Dmaven.plugin.validation=verbose`
- Nur offizielle Maven-BOMs für die Plugin-Entwicklung nutzen
- Migration schrittweise angehen: erst ohne Model-4.1.0-Features, dann optional nachziehen

---
layout: section
---

# Teil 3

## Migration von 3 zu 4

---
layout: default
---

# Der 3-Stufen-Ansatz

- **Prepare** – Voraussetzungen mit aktuellem Maven 3 schaffen
- **Test** – parallel mit Maven 4 (RC) bauen, minimale Fixes vornehmen
- **Migrate** – Maven 3 fallen lassen, optionale Maven-4-Features nutzen

---
layout: default
---

# Schritt 1: Prepare

- Neueste Maven-3.9.x-Version verwenden
- Alle Plugins auf die neueste **Maven-3-kompatible** Version heben
- `versions-maven-plugin` (`display-plugin-updates`) hilft bei der Analyse
- Noch **nicht** auf Plugin-Versionen aktualisieren, die Maven 4 voraussetzen

---
layout: default
---

# Schritt 2: Test

- Build-Umgebung (lokal + CI) auf Java 17 vorbereiten
- Maven 4 (RC) parallel installieren
- Notwendige Plugins auf Maven-4-taugliche Version aktualisieren
- Typische Stolpersteine:
  - Doppelte Plugin-Deklarationen (jetzt Build-Fehler statt Warnung)
  - Entfernte Properties: `executionRootDirectory`, `multiModuleProjectDirectory`
  - Bindungen an `pre-`/`post-`-Phasen → auf `before:`/`after:` umstellen

---
layout: default
---

# Schritt 3: Migrate

- Erst produktiv auf Maven 4 laufen, **danach** optionale Features einführen
- `deployAtEnd`/`installAtEnd` explizit setzen, falls `false` gewünscht (sonst neuer Default `true`)
- Projekt-Root definieren
- Schrittweise auf Model 4.1.0 wechseln: `<subprojects>`, automatische Versionierung, `bom`-Packaging
- **Maven Upgrade Tool (`mvnup`)** automatisiert die meisten dieser Schritte

---
layout: two-cols-header
---

# Migration in der Praxis

::left::

### Vorher (Maven 3, Model 4.0.0)

```xml
<modules>
  <module>modul-a</module>
  <module>modul-b</module>
</modules>
```

::right::

### Nachher (Maven 4, Model 4.1.0)

```xml
<subprojects>
  <subproject>modul-a</subproject>
  <subproject>modul-b</subproject>
</subprojects>
```

::bottom::

`<modules>` bleibt vorerst nutzbar (deprecated) – kein Zeitdruck, aber die Migration lohnt sich.

---
layout: statement
---

# Migration ist ein **Prozess, kein Big-Bang-Schritt.**

---
layout: default
---

# Zusammenfassung

- Maven 4 trennt Build- und Consumer-Informationen sauber
- Viele Boilerplate-Probleme (Versionierung, Module) sind gelöst
- Migration funktioniert schrittweise – `mvnup` nimmt viel Arbeit ab
- Aktuell: RC-Phase – gute Zeit zum Testen, bevor GA kommt

---
layout: contact
name: Robin P. Fischer
role: DevOps-Engineer
email: robin.fischer@gepardec.com
photo: /contact.jpg
---

# Fragen?
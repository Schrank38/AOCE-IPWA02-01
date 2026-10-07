# Ghost Net Fishing – Webanwendung (IPWA02-01)

Ein Spring-Boot-Prototyp zur Erfassung, Verwaltung und Koordination der Bergung von Geisternetzen für die Non-Profit-Organisation **Shea Shepherd**. Das Projekt entstand als Fallstudie im Modul **IPWA02-01 (Programmierung von industriellen Informationssystemen mit Java EE)** an der IU Internationalen Hochschule.

---

## Inhaltsverzeichnis
1. [Fachlicher Hintergrund](#fachlicher-hintergrund)
2. [Technologiestack](#technologiestack)
3. [Umgesetzte User Stories (Sprint 1)](#umgesetzte-user-stories-sprint-1)
4. [Softwarearchitektur & Datenmodell](#softwarearchitektur--datenmodell)
5. [Projektstruktur](#projektstruktur)
6. [HTTP-Endpunkte & Routing](#http-endpunkte--routing)
7. [Installation und Anwendungsstart](#installation-und-anwendungsstart)
8. [Datenbankkonfiguration & Persistenz](#datenbankkonfiguration--persistenz)
9. [Manuelle Testabläufe](#manuelle-testabläufe)
10. [Projektdaten](#projektdaten)

---

## Fachlicher Hintergrund

Geisternetze sind im Meer verbliebene oder verlorene Fischernetze, die ungehindert weiterfischen und maritime Ökosysteme gefährden. Die Webanwendung ermöglicht es Bürgern und Bergungsteams, Standorte zu melden und den Bearbeitungsstatus zentral nachzuverfolgen.

### Geschäftsregeln & Fachlogik
* **Anonymitätsregel:** Fundmeldungen können anonym (ohne Name und Telefonnummer) eingereicht werden. Sofern ein Name angegeben wird, ist eine Telefonnummer für Rückfragen verpflichtend.
* **Verschollen-Meldung:** Um unnötige Suchfahrten zu vermeiden, dürfen Netze **nicht** anonym als verschollen gemeldet werden. Name und Telefonnummer sind hierbei Pflichtfelder.
* **Zuordnung:** Ein Netz kann zu jedem Zeitpunkt von maximal einer bergenden Person übernommen werden. Eine bergende Person kann jedoch mehrere Netze gleichzeitig verwalten.
* **Statusübergänge:** `GEMELDET` $\rightarrow$ `BERGUNG_BEVORSTEHEND` $\rightarrow$ `GEBORGEN` bzw. `VERSCHOLLEN`.

---

## Technologiestack

* **Laufzeitumgebung:** Java 21 (LTS)
* **Framework:** Spring Boot 3.2.5 (Spring MVC, Spring Data JPA, Bean Validation)
* **Persistenz-Provider:** Hibernate (ORM)
* **Datenbank:** H2 Database (dateibasiert via `jdbc:h2:file:./ghostnetdb`)
* **Frontend:** Thymeleaf Templates & Bootstrap 5.3 (über CDN eingebunden)
* **Build-Tool:** Apache Maven

---

## Umgesetzte User Stories (Sprint 1)

Im ersten Entwicklungs-Sprint wurden nach der MoSCoW-Priorisierung folgende Kernanforderungen umgesetzt:

| User Story | Priorität | Beschreibung |
|---|---|---|
| **US-1** | MUST | Anonyme und namentliche Erfassung von Geisternetzen mit Standort (Breitengrad, Längengrad) und geschätzter Größe. |
| **US-2** | MUST | Ankündigung/Übernahme einer Bergung durch eine bergende Person (Statuswechsel zu `BERGUNG_BEVORSTEHEND`). |
| **US-3** | MUST | Tabellarische Übersicht aller noch zu bergenden, offenen Geisternetze. |
| **US-4** | MUST | Bestätigung und Abschluss der Bergung durch die eingetragene bergende Person (Statuswechsel zu `GEBORGEN`). |
| **US-7** | COULD | Meldung eines Netzes als unauffindbar durch eine beliebige namentlich bekannte Person (Statuswechsel zu `VERSCHOLLEN`). |

---

## Softwarearchitektur & Datenmodell

Die Anwendung folgt dem klassischen 3-Schichten-Architekturmuster (Model-View-Controller):

1. **Presentation Layer:** `GeisternetzController` verarbeitet Formulareingaben, nutzt `@InitBinder` zur Typkonvertierung von Fließkommazahlen und leitet Benachrichtigungen via Flash-Attributes weiter.
2. **Business Layer:** `GeisternetzService` steuert transaktionale Abläufe (`@Transactional`), setzt Anonymitätsregeln durch und verhindert Datenredundanz bei Personen.
3. **Data Access Layer:** `GeisternetzRepository` und `PersonRepository` (Spring Data JPA) abstrahieren Datenbankzugriffe.
4. **Domain Layer:** 
   * `Geisternetz`: Entität mit Koordinaten, Größe, Status, Versionsfeld (`@Version` für Optimistic Locking) und Fremdschlüssel-Beziehungen zu Personen (`meldendePerson`, `bergendePerson`, `verschollenMelder`).
   * `Person`: Entität zur Speicherung von Kontaktdaten (Name, Telefonnummer).
   * `NetzStatus`: Enum mit den Werten `GEMELDET`, `BERGUNG_BEVORSTEHEND`, `GEBORGEN`, `VERSCHOLLEN`.

---

## Projektstruktur

```text
ghostnet/
├── src/
│   ├── main/
│   │   ├── java/com/sheasepherd/ghostnet/
│   │   │   ├── controller/
│   │   │   │   ├── GeisternetzController.java
│   │   │   │   └── HomeController.java
│   │   │   ├── model/
│   │   │   │   ├── Geisternetz.java
│   │   │   │   ├── NetzStatus.java
│   │   │   │   └── Person.java
│   │   │   ├── repository/
│   │   │   │   ├── GeisternetzRepository.java
│   │   │   │   └── PersonRepository.java
│   │   │   ├── service/
│   │   │   │   └── GeisternetzService.java
│   │   │   └── GhostnetApplication.java
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── netz-erfassen.html
│   │       │   └── netze-liste.html
│   │       └── application.properties
│   └── test/
│       └── java/com/sheasepherd/ghostnet/
│           └── GhostnetApplicationTests.java
├── pom.xml
└── README.md
```

---

## HTTP-Endpunkte & Routing

HTTP-Methode|URL-Pfad|Beschreibung
|---|---|---|
GET| /| Weiterleitung auf /netze
GET|/netze|Zeigt die Übersicht aller offenen Geisternetze an
GET|/netze/neu|Öffnet das Formular zur Neuerfassung eines Geisternetzes
POST|/netze/speichern|Verarbeitet das Erfassungsformular und speichert das Netz
POST|/netze/bergung/{id}|Trägt eine bergende Person ein (GEMELDET → BERGUNG_BEVORSTEHEND)
POST|/netze/geborgen/{id}|Bestätigt die Bergung (BERGUNG_BEVORSTEHEND → GEBORGEN)
POST|/netze/verschollen/{id}|Markiert ein Netz als verschollen (VERSCHOLLEN)

---

## Installation und Anwendungsstart
 Voraussetzungen
* Java Development Kit (JDK 21 oder höher)

* Apache Maven (oder Verwendung des mitgelieferten Maven Wrappers)

1. Repository klonen
git clone [https://github.com/Schrank38/AOCE-IPWA02-01.git](https://github.com/Schrank38/AOCE-IPWA02-01.git)
cd AOCE-IPWA02-01

2. Anwendung bauen und starten
  Über Maven Wrapper (Kommandozeile):
  ./mvnw spring-boot:run
  
  Unter Windows (cmd/PowerShell):
  mvnw.cmd spring-boot:run

  Über eine IDE (z. B. Eclipse / IntelliJ):

  1. Projekt als Existing Maven Project importieren.

  2. Klasse com.sheasepherd.ghostnet.GhostnetApplication ausführen (Run as -> Spring Boot App).

3. Anwendung öffnen
Nach erfolgreichem Start ist die Anwendung im Browser aufrufbar unter:

  http://localhost:8081/netze

---

## Datenbankkonfiguration & Persistenz
Die Anwendung ist für eine dateibasierte H2-Datenbank konfiguriert. Dadurch bleiben eingetragene Datensätze auch nach Beendigung und Neustart der Anwendung erhalten.

Speicherort: ./ghostnetdb.mv.db (im Arbeitsverzeichnis des Projekts)

Browser-Zugriff (H2-Konsole): http://localhost:8081/h2-console

JDBC URL: jdbc:h2:file:./ghostnetdb

User Name: sa

Password: (leer)

---

## Manuelle Testabläufe
1.  Anonyme Erfassung: Auf /netze/neu Breitengrad (54,32), Längengrad (10,12) und Größe (15,0) eingeben. Namensfelder leer lassen und abschicken. Das Netz erscheint in der Liste als Anonym.

2.  Validierungstest: Auf /netze/neu Name ausfüllen, aber Telefonnummer leer lassen. Die Anwendung weist das Formular mit einem Hinweis ab.

3.  Bergung ankündigen: In der Liste bei einem gemeldeten Netz Name und Telefonnummer des Bergers eingeben und auf Für Bergung eintragen klicken. Der Status wechselt zu BERGUNG_BEVORSTEHEND.

4.  Bergung abschließen: Bei einem Netz im Status BERGUNG_BEVORSTEHEND dieselben Kontaktdaten erneut eingeben und auf Als geborgen markieren klicken. Das Netz wechselt den Status zu GEBORGEN und verschwindet aus der Liste der offenen Netze.

5.  Verschollen-Meldung: Bei einem offenen Netz Name und Telefonnummer unter Verschollen melden eingeben. Der Status wechselt zu VERSCHOLLEN.

---

## Projektdaten

* Modul: IPWA02-01 – Programmierung von industriellen Informationssystemen mit Java EE

* Hochschule: IU Internationale Hochschule

* Repository: https://github.com/Schrank38/AOCE-IPWA02-01
   

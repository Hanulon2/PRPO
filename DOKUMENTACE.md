# Dokumentace semestrálního projektu (PPRO)

**Předmět:** Pokročilé programování (PPRO), zimní semestr 2026/2027  
**Fakulta:** Fakulta informatiky a managementu, Univerzita Hradec Králové  

| Pole | Hodnota |
|---|---|
| **Název projektu** | Skladový a objednávkový systém Dřevěnka |
| **Zvolené zadání** | Zadání B: Sklad pro malý e-shop |
| **Jméno a příjmení** | Ondřej Hanuš (hanulon2@uhk.cz) |
| **Repozitář na GitHubu** | [https://github.com/Hanulon2/PRPO](https://github.com/Hanulon2/PRPO) |
| **Datum poslední úpravy** | 7. 10. 2026 |

---

## 1. Popis řešení

Aplikace slouží firmě Dřevěnka s.r.o. vedené Petrem Doležalem, která se zabývá výrobou a prodejem dřevěných hraček. Cílem systému je nahradit stávající nevyhovující evidenci ve sdíleném Excelovém souboru o 400 položkách, který byl otevírán na třech počítačích současně a vedl k závažným provozním chybám. Hlavním problémem klienta byl prodej neexistujícího zboží (*overselling*), kdy e-shop prodal položku, která nebyla skladem, a majitel musel volat zklamaným zákazníkům s omluvou. Systém řeší také evidenci dvou fyzických skladů firmy – hlavního expedičního skladu v Hradci Králové a sezónního skladu v garáži v Třebechovicích, odkud se dosud závozy do Hradce vůbec neevidovaly.

Aplikace eviduje produkty a jejich zatřídění do více kategorií současně (vazba M:N – např. dřevěná káča je v kategorii „pro batolata“ i „dárky do 500 Kč“). Zásadní inovací je oddělení fyzického stavu na regálech od stavu disponibilního (volného k prodeji). Při přijetí objednávky systém provede okamžitou rezervaci zásoby a v případě nedostatku objednávku striktně odmítne. Fyzický odpis proběhne až při expedici balíku. Každá změna zásoby (příjem z truhlárny, výdej zákazníkovi, meziskladový přesun Třebechovice -> Hradec i inventurní dorovnání) je trvale zapsána v auditním deníku pohybů. Na úvodní obrazovce má majitel přehled „Co hoří“ s produkty pod minimální zásobou a měsíční finanční report tržeb po kategoriích.

Technicky je systém postaven jako třívrstvá enterprise aplikace v jazyce Java 21 s využitím frameworku Spring Boot 3. Prezentační vrstva využívá Spring MVC a šablonovací engine Thymeleaf s responzivním CSS přizpůsobeným pro ovládání z mobilního telefonu přímo ve skladu. Aplikační vrstva obsahuje servisní služby vynucující striktní byznys pravidla (zákaz záporného stavu, fixace ceny v objednávce, rezervace). Datová vrstva je postavena na Spring Data JPA / Hibernate a relační databázi PostgreSQL 16 provozované v Docker kontejneru. Schéma databáze je verzováno nástrojem Flyway. Celé řešení je plně kontejnerizováno a spustitelné jediným příkazem `docker compose up`.

---

## 2. Rozhodnutí

| Č. | Rozhodnutí | Důvod | Datum |
|---|---|---|---|
| **R1** | Backend ve Spring Bootu (Java 21) | Javu znám z předchozích předmětů; framework má hotovou podporu transakcí, migrací a testů proti databázi. | 30. 9. 2026 |
| **R2** | Databáze PostgreSQL v Dockeru | Zadání žádá relační databázi v kontejneru; PostgreSQL má oficiální obraz, vysokou spolehlivost a běží stejně u mě i u vyučujícího. | 30. 9. 2026 |
| **R3** | Třívrstvá architektura se závislostmi jedním směrem | Striktní rozdělení na Controllers, Services a Repositories zajišťuje nezávislost byznys logiky a snadnou testovatelnost. | 7. 10. 2026 |
| **R4** | Verzované databázové migrace přes Flyway | Zaručuje automatické a reprodukovatelné vytvoření schématu a seedu syntetických dat v kontejneru bez ručních zásahů. | 7. 10. 2026 |
| **R5** | Dvoufázové odečítání zásob: Disponibilní vs. fyzická zásoba | Okamžitá rezervace při přijetí objednávky zabrání prodeji neexistujícího zboží; fyzický odpis proběhne až při expedici balíku. | 7. 10. 2026 |
| **R6** | Auditní deník pohybů (StockMovement) a zákaz záporného stavu | Záporný stav je zakázán na úrovni DB integritního omezení; každá změna zásoby je neměnný záznam (kdo, kdy, proč). Rozdíly řeší inventura. | 7. 10. 2026 |
| **R7** | Konsolidace expedice ze dvou skladů (Hradec a Třebechovice) | Zákaznická objednávka se expeduje vždy z Hradce Králové; chybějící zboží v Třebechovicích vyvolá interní převodku (svoz dodávkou). | 7. 10. 2026 |
| **R8** | Fixace jednotkové ceny v položce objednávky (Price Snapshot) | Při vytvoření objednávky se cena zafixuje v položce; budoucí změna ceníku produktů neovlivní již odeslané objednávky. | 7. 10. 2026 |
| **R9** | Vazba produktů do více kategorií M:N | Produkt může patřit do více kategorií naráz (např. batolata i dárky do 500 Kč) s kategoriovým vykazováním měsíčního obratu. | 7. 10. 2026 |
| **R10** | Responzivní webové rozhraní (Thymeleaf + CSS) pro mobil ve skladu | Petr Doležal tráví půl dne ve skladu; UI je optimalizováno pro mobil s velkými tlačítky a barevnou indikací („co hoří“). | 7. 10. 2026 |

---

## 3. Změny požadované klientem

| Č. | Datum | Požadavek klienta | Řešení a dopad na návrh | Stav |
|---|---|---|---|---|
| **Z1** | 7. 10. 2026 | „Dvakrát měsíčně prodáme něco, co nemáme na skladě. To se nesmí stávat.“ | Zavedení konceptu disponibilní zásoby a okamžité rezervace; automatické odmítnutí objednávky při nedostatku volných kusů. Viz R5. | hotovo |
| **Z2** | 7. 10. 2026 | „V garáži stav nikdy nesedí, potřebuju vědět, proč jich je sedmnáct a ne dvacet.“ | Zavedení neměnné tabulky skladových pohybů (`StockMovement`) a modulu inventury pro evidenci manka/přebytku. Viz R6. | hotovo |
| **Z3** | 7. 10. 2026 | „Potřebuju na úvodní stránce barevně vidět, co hoří a co musím doobjednat.“ | Dashboard s přehledem produktů pod minimálním limitem zásob, barevná vizualizace (červená/oranžová) a rychlá tvorba nákupního požadavku. Viz R10. | rozpracováno |
| **Z4** | 7. 10. 2026 | „Ať to jde používat i z mobilu, já jsem půlku dne ve skladu.“ | Responzivní design pro skladové operace (příjem, přesun, inventura) přizpůsobený dotykovému ovládání. Viz R10. | rozpracováno |

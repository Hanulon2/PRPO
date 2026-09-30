# Ordinace Na Vyhlídce – Rezervační systém pro ordinaci lékařů

> **Předmět:** Pokročilé programování (PPRO) – ZS 2026/2027, FIM UHK  
> **Zadání:** Zadání A: Rezervace v ordinaci  
> **Klient:** MUDr. Jana Hrubá, Ordinace Na Vyhlídce  
> **Typ dokumentu:** Technická dokumentace, specifikace zadání a Jediný zdroj pravdy (Single Source of Truth)  
> **Pravidla a konvence asistenta:** Viz [AGENTS.md](file:///c:/Users/38ond/Desktop/PRPO/AGENTS.md)

---

## 1. Kontext a výchozí stav projektu (As-Is)

### 1.1 Klient a provoz ordinace
Ordinace Na Vyhlídce je soukromá lékařská praxe vedená MUDr. Janou Hrubou. V ordinaci působí:
- **Tři lékaři** na částečné úvazky s různorodou ordinační dobou.
- **Paní Věra na recepci**, která obsluhuje telefon, diář, příchody pacientů i měsíční výkaznictví.
- **Kmen pacientů:** Cca 900 registrovaných pacientů, z nichž přibližně polovina dochází na pravidelné kontroly.

### 1.2 Současný způsob práce a jeho limity
- **Papírový diář na recepci:** Zápisy probíhají tužkou. Při telefonátu pacienta paní Věra zdlouhavě listuje diářem a hledá volná okna.
- **Chybovost (Double-booking):** Za poslední rok došlo minimálně dvakrát k situaci, kdy byli dva pacienti objednáni na stejný čas ke stejnému lékaři. Klientka toto označuje za nepřípustnou ostudu.
- **Komplikovaná komunikace při výpadku lékaře:** Pokud lékař náhle onemocní, recepční musí listovat diářem a ručně dohledávat kontakty pro obvolání dotčených pacientů.
- **Ruční vykazování pro pojišťovny:** Na konci každého měsíce paní Věra tráví celý podvečer ručním přepisováním provedených výkonů z papírových karet do tabulek pro zdravotní pojišťovny.

---

## 2. Specifikace požadavků klienta (To-Be)

### 2.1 Co klient chce (Funkční požadavky)
1. **Evidence pacientů:**
   - Uchování údajů: jméno, příjmení, rok narození, telefonní číslo, identifikátor pojištěnce (číslo pojištěnce / rodné číslo), kód zdravotní pojišťovny.
   - Okamžité vyhledání pacienta podle **příjmení** nebo **telefonního čísla** (klíčové pro rychlé odbavení při příchozím telefonátu).
2. **Lékaři a jejich ordinační hodiny:**
   - Každý lékař má individuálně nastavené dny a časy ordinace.
   - Podpora více lokací: Jeden z lékařů ordinuje v úterý odpoledne na jiné adrese – systém **nesmí** umožnit objednávání na tuto externí lokaci.
3. **Rezervace na konkrétní termín u konkrétního lékaře:**
   - Založení rezervace recepcí.
   - Součástí rezervace je textová poznámka s důvodem návštěvy (např. akutní bolest, preventivní prohlídka, kontrola tlaku).
4. **Záznam o návštěvě a výkonech:**
   - Po uskutečnění návštěvy zápis provedených výkonů.
   - Výkony se vybírají ze spravovaného číselníku výkonů: každý výkon má kód (např. VZP kód), název a bodové ohodnocení.
   - Na jedné návštěvě může proběhnout více výkonů a tentýž výkon se může opakovat u mnoha návštěv (vazba M:N).
   - U vybraných výkonů je nutné evidovat násobnost (počet aplikací / jednotek).
5. **Zrušení rezervace a statistika nespolehlivých pacientů:**
   - Možnost zrušení rezervace, když pacient zavolá.
   - Přehled o pacientech, kteří rezervace ruší opakovaně či nedorazí (identifikace nespolehlivých pacientů).
6. **Přehled volných termínů:**
   - Týdenní přehled volných časových slotů pro vybraného lékaře. Nejčastěji využívaná funkce recepce.
7. **Měsíční výkaz pro pojišťovnu:**
   - Generování přehledu za zvolený kalendářní měsíc a vybraného lékaře: agregovaný seznam provedených výkonů a celkový součet bodů. Automatizuje dřívější ruční práci recepce.

### 2.2 Na čem klient trvá (Striktní obchodní pravidla)
Aplikace tato pravidla vynucuje programově na úrovni doménové a datové logiky (nespoléhá se na pozornost uživatele):
- **Zákaz kolize termínů:** Dvě rezervace na stejný termín u stejného lékaře nesmí jít založit za žádných okolností (ani ručně, ani omylem).
- **Striktní ordinační hodiny:** Rezervaci nelze založit mimo platné ordinační hodiny daného lékaře v ordinaci Na Vyhlídce.
- **Integrita zápisu výkonů:** Výkon nelze zapsat k návštěvě, která se ještě neuskutečnila (tj. je pouze naplánovaná, případně byla zrušena).
- **Zákaz mazání historie:** Historie pacienta a proběhlých návštěv se **nemaže** (právní a auditní povinnost doložit poskytnutou péči; uplatněn princip soft-delete a neměnných záznamů).

### 2.3 Ergonomie a specifika používání (Co klient prohodil mimochodem)
- **Přístupnost pro paní Věru:** Rozhraní musí být přehledné, s minimem zbytečných kliknutí a velkými kontrastními prvky, vhodné i pro uživatele s nižší počítačovou gramotností.
- **Rychlost odezvy:** Okamžité vyhledávání a zobrazení volných termínů (paní Věra mluví s pacientem po telefonu a nemůže čekat na zdlouhavé načítání).
- **Připravenost na budoucí rozšíření:** Architektura musí umožnit budoucí přidání mobilního/tabletového zobrazení a případného zabezpečeného samoobslužného objednávání pro pacienty.

---

## 3. Seznam architektonických rozhodnutí (ADR)

Tato sekce detailně rozebírá všech 5 otevřených bodů ze zadání klienta, přináší jasná rozhodnutí a jejich zdůvodnění pro technickou obhajobu.

### ADR-001: Časový rastr a délka termínu rezervace
* **Problém:** Klient uvádí délku termínu „podle toho, co se dělá“. Není dořešeno, zda má systém pevnou mřížku (např. 15 minut), nebo má každá rezervace libovolnou délku.
* **Rozhodnutí:** Systém zavádí základní **časový rastr o velikosti 15 minut**. Každá rezervace se skládá z celistvého násobku těchto slotů:
  - Běžná kontrola / konzultace = 1 slot (15 minut).
  - Vstupní vyšetření / složitější procedura = 2 sloty (30 minut).
  - Rozsáhlý zákrok = 3 až 4 sloty (45–60 minut).
* **Zdůvodnění:** Pevný rastr 15 minut umožňuje vygenerovat čistý, přehledný kalendář bez vznikání nepoužitelných fragmentovaných minutových mezer (např. 7 minut volna). Zároveň poskytuje plnou flexibilitu pro různě náročné typy návštěv.

### ADR-002: Aktéři systému a způsob zakládání rezervací
* **Problém:** Klient nejprve vyžaduje výhradní správu recepcí, následně zvažuje internetové objednávání pacientů.
* **Rozhodnutí:** V 1. fázi je systém navržen primárně pro **interní rozhraní recepce (paní Věra) a lékařů**. Servisní vrstva a doménové API jsou však striktně odděleny od prezentační vrstvy tak, aby bylo možné v budoucnu připojit externí REST API pro pacientský portál bez zásahu do byznys logiky.
* **Zdůvodnění:** Chrání provoz ordinace před zahlcením hned při spuštění a garantuje, že prioritní potřeby paní Věry budou uspokojeny jako první.

### ADR-003: Životní cyklus zrušené rezervace a statistika rušení
* **Problém:** Klient požaduje, aby zrušená rezervace „zmizela z diáře“, ale současně vyžaduje statistiku pacientů, kteří ruší nejčastěji.
* **Rozhodnutí:** Rezervace využívají stavový automat se stavy:
  - `PLANNED` (Naplánovaná)
  - `COMPLETED` (Uskutečněná / Odbavená)
  - `CANCELLED` (Zrušená pacientem nebo ordinací)
  - `NO_SHOW` (Pacient nedorazil bez omluvy)
  
  Při zrušení rezervace přechází záznam do stavu `CANCELLED` a uchovává se čas zrušení i důvod. V kalendáři volných termínů je daný časový slot okamžitě uvolněn a zobrazen jako zelený (volný). V kartě pacienta i v analytických přehledech však záznam zůstává trvale dostupný pro agregaci nespolehlivosti.
* **Zdůvodnění:** Odstraňuje zdánlivý rozpor – diář zůstává stoprocentně funkční pro nové objednávky a historická data pro statistiky jsou kompletně zachována.

### ADR-004: Jednoznačná identifikace pacienta a prevence duplicit
* **Problém:** Klient zmiňuje, že „paní Nováková je v diáři třikrát“, chybí pravidlo jednoznačnosti a postup při duplicitách.
* **Rozhodnutí:**
  1. Jednoznačným přirozeným identifikátorem pacienta je **Číslo pojištěnce (rodné číslo)** v kombinaci s kódem zdravotní pojišťovny. Na úrovni databáze je nastaven unikátní index.
  2. Při zadávání nového pacienta systém provádí **interaktivní detekci podobnosti** podle telefonního čísla a shody jména. Pokud systém nalezne existující záznam, paní Věra je ihned dotázána, zda nechce otevřít stávající kartu pacienta namísto vytvoření duplikátu.
* **Zdůvodnění:** Garantuje čistotu databáze, zabraňuje rozpadu historie pacienta do více karet a šetří čas recepční při překlepech.

### ADR-005: Postup při náhlé nemoci lékaře
* **Problém:** Co se má stát při výpadku lékaře: hromadné zrušení, automatický přesun na jiného lékaře, nebo obvolání recepcí?
* **Rozhodnutí:** Systém zavádí funkci **„Evidence nepřítomnosti lékaře“** (zadání intervalu od–do a důvodu nepřítomnosti).
  - Všechny budoucí rezervace daného lékaře v tomto období systém automaticky označí dočasným příznakem `NEEDS_RESCHEDULE` (K přeplánování).
  - Na úvodní obrazovce paní Věry se vygeneruje prioritní **Fronta pacientů k obvolání** se seřazením dle termínu, obsahující telefon, jméno a poznámku.
  - V rozhraní má paní Věra dvě rychlé akce: *Přeplánovat* (otevře přehled volných termínů u kolegů či v pozdějším termínu) nebo *Zrušit s omluvou*.
* **Zdůvodnění:** Zdravotní péči nelze přehodit automaticky jinému lékaři bez souhlasu pacienta (specializace, kapacita). Asistovaný proces poskytne paní Věře dokonalý přehled a jistotu, že žádný pacient nezůstane neinformován.

---

## 4. Architektura a technické řešení (Povinné minimum PPRO)

Projekt je realizován v souladu s formálními požadavky předmětu PPRO:

### 4.1 Třívrstvá architektura s jednosměrnými závislostmi
```
+-----------------------------------------------------------------+
|               Prezentační vrstva (Presentation Layer)           |
|      - REST API kontrolery, Web UI (paní Věra), DTO objekty     |
+-----------------------------------------------------------------+
                                │ (volá výhradně směrem dolů)
                                ▼
+-----------------------------------------------------------------+
|            Aplikační a doménová vrstva (Application Layer)      |
|      - Servisní služby, validační pravidla, detekce kolizí      |
|      - Stavový automat rezervací, byznys kalkulace výkazů       |
+-----------------------------------------------------------------+
                                │ (volá výhradně směrem dolů)
                                ▼
+-----------------------------------------------------------------+
|            Datová / Perzistenční vrstva (Persistence Layer)     |
|      - Spring Data JPA repozitáře, PostgreSQL schéma, migrace   |
+-----------------------------------------------------------------+
```

### 4.2 Doménový a datový model (Entity a vazby)
Minimální počet entit v projektu je 7 (požadováno minimálně 5) a model obsahuje vazbu M:N s atributem:

1. **`Patient` (Pacient):**
   - Atributy: `id`, `first_name`, `last_name`, `birth_year`, `phone`, `insurance_id` (číslo pojištěnce), `insurance_company_code`, `created_at`.
2. **`Doctor` (Lékař):**
   - Atributy: `id`, `title`, `first_name`, `last_name`, `specialization`, `phone`, `email`.
3. **`DoctorWorkingHours` (Ordinační hodiny):**
   - Atributy: `id`, `doctor_id` (FK), `day_of_week`, `start_time`, `end_time`, `is_external_location` (pro zákaz rezervací na externí adrese).
4. **`Appointment` (Rezervace / Návštěva):**
   - Atributy: `id`, `patient_id` (FK), `doctor_id` (FK), `appointment_date`, `start_time`, `end_time`, `status` (`PLANNED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`), `reason_note`, `cancellation_reason`, `created_at`.
5. **`MedicalService` (Lékařský výkon – číselník):**
   - Atributy: `id`, `code` (kód výkonu), `name`, `points` (bodová hodnota), `is_active`.
6. **`AppointmentServiceItem` (Položka provedeného výkonu – vazební tabulka M:N):**
   - Reprezentuje vazbu M:N mezi `Appointment` a `MedicalService`.
   - Atributy: `id`, `appointment_id` (FK), `medical_service_id` (FK), `count` (počet aplikací), `applied_points` (zafixované body v době provedení).
7. **`DoctorAbsence` (Nepřítomnost lékaře):**
   - Atributy: `id`, `doctor_id` (FK), `start_date`, `end_date`, `reason`.

### 4.3 Relační databáze a migrace
- **Databáze:** PostgreSQL v Docker kontejneru.
- **Migrační nástroj:** Flyway.
- **Syntetická data:** Žádná reálná osobní data ani hesla. Součástí je seed migrace s fiktivními lékaři, pacienty a VZP číselníkem.

---

## 5. Návod ke spuštění a vývojové instrukce

### 5.1 Požadavky na prostředí
- Java JDK 21+
- Docker a Docker Compose v2+
- Git

### 5.2 Spuštění pomocí Docker Compose
Celý systém (databáze + backend + inicializační migrace) lze spustit jediným příkazem:
```bash
docker compose up --build
```
Aplikace bude po naběhnutí dostupná na adrese `http://localhost:8080`.

### 5.3 Spuštění testů
Ověření funkčnosti obchodních pravidel a detekce kolizí:
```bash
./mvnw test
```

---

## 6. Záznam o postupu prací a Roadmapa (Changelog)

| Fáze | Popis úkolu | Stav |
|---|---|---|
| **Fáze 1** | Analýza Zadání A, vytvoření `AGENTS.md`, pre-commit hooku a kompletního `README.md` jako jediného zdroje pravdy. | Hotovo |
| **Fáze 2** | Příprava kostry projektu (Spring Boot 3, Java 21, Maven/Gradle, Dockerfile, docker-compose.yml). | Plánováno |
| **Fáze 3** | Definice Flyway migrací, DDL schématu, syntetických seed dat a JPA entit (včetně M:N vazby). | Plánováno |
| **Fáze 4** | Implementace servisní vrstvy: algoritmus volných termínů, detekce překryvů, validace ordinačních hodin a stavový automat. | Plánováno |
| **Fáze 5** | Implementace REST API / UI pro recepční paní Věru (vyhledávání, diář, měsíční výkaz pojišťovny, fronta přeplánování). | Plánováno |
| **Fáze 6** | Unit a integrační testy pro ověření povinného minima a hraničních stavů. | Plánováno |
| **Fáze 7** | Závěrečná validace, dokumentační prověrka k obhajobě. | Plánováno |

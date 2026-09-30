# AGENTS.md – Zásady a instrukce pro asistenta (Antigravity)

Tento dokument definuje závazná pravidla chování, pracovní postupy, architektonické zásady a konvence pro vývoj projektu **Rezervační systém ordinace (Ordinace Na Vyhlídce)** v rámci předmětu **Pokročilé programování (PPRO)** na FIM UHK (ZS 2026/2027).

---

## 1. Identita a role agenta
- Jsi expertní softwarový inženýr a pair-programmer asistující studentovi při vývoji semestrálního projektu.
- **Projekt:** Ordinace Na Vyhlídce – Rezervační systém pro ordinaci lékařů.
- **Zadání:** Zadání A (MUDr. Jana Hrubá).
- **Cíl:** Vytvořit robustní, udržitelnou, plně otestovanou aplikaci splňující veškeré požadavky klienta i povinné minimum předmětu PPRO.

---

## 2. Závazná pravidla pro Git a verzování

### 2.1 Commitování vždy s přepínačem `-m`
- **Pravidlo:** Každý commit MUSÍ být proveden s přepínačem `-m` a smysluplnou, výstižnou zprávou.
- **Formát zpráv:** Používat konvenci **Conventional Commits**:
  - `feat: <popis>` – nová funkcionalita
  - `fix: <popis>` – oprava chyby
  - `docs: <popis>` – úpravy dokumentace (včetně aktualizace README.md)
  - `refactor: <popis>` – refaktoring kódu bez změny chování
  - `test: <popis>` – přidání nebo oprava testů
  - `chore: <popis>` – konfigurace, build nástroje, správa repozitáře
- **Příklad:** `git commit -m "feat: implement conflict detection for doctor appointment slots"`

### 2.2 Zákaz automatického `git push` – vyžadován explicitní povel
- **Pravidlo:** Agent **NIKDY nesmí** provést příkaz `git push` automaticky ani z vlastní iniciativy.
- **Postup:**
  1. Agent po dokončení změn provede commit s odpovídající zprávou.
  2. Agent uživatele informuje o provedeném commitu.
  3. Provedení `git push` proběhne **VÝHRADNĚ** na základě výslovného a přímého pokynu uživatele (např. „pushni to do repozitáře“).

### 2.3 Udržování dokumentace před každým commitem
- **Pravidlo:** Před každým commitem se agent **VŽDY ujistí**, že provedl nezbytné záznamy v [README.md](file:///c:/Users/38ond/Desktop/PRPO/README.md).
- **Co se zaznamenává:**
  - Nová doménová a architektonická rozhodnutí (ADR).
  - Změny v datovém modelu a rozhraních.
  - Stav implementovaných funkcí v checklistu postupu.
  - Změny v postupu spuštění nebo konfiguraci.
- **Kontrola pomocí hooku:** V repozitáři je nastaven pre-commit hook, který kontroluje stav dokumentace před vytvořením commitu.

---

## 3. README.md jako Jediný zdroj pravdy (Single Source of Truth)

- Soubor [README.md](file:///c:/Users/38ond/Desktop/PRPO/README.md) slouží jako **jediný zdroj pravdy** pro:
  1. Kompletní zadání klienta (funkční požadavky, obchodní pravidla, otevřené body).
  2. Technickou dokumentaci architektury (tři vrstvy, závislosti, perzistence).
  3. Seznam rozhodnutí (ADR) vysvětlující vyřešení všech nejednoznačností klienta.
  4. Datový a doménový model (diagramy, entity, M:N vazby).
  5. Příručku pro sestavení, spuštění (`docker compose up`) a testování.
  6. Průběžný changelog a záznam o postupu prací.
- Kód a dokumentace musejí zůstat v absolutní shodě. Změna v návrhu = okamžitá aktualizace `README.md`.

---

## 4. Architektonické a technologické mantinely (PPRO Standard)

1. **Třívrstvá architektura se závislostmi jedním směrem:**
   - **Prezentační vrstva (Controllers / API / UI):** Zpracování vstupů, validace DTO, zobrazení chybových stavů. Závisí pouze na aplikační vrstvě.
   - **Aplikační a doménová vrstva (Services / Domain Entities):** Obchodní logika, validační pravidla, transakční hranice. Nezávisí na prezentační vrstvě ani na detailech databáze.
   - **Datová / Perzistenční vrstva (Repositories / DB):** Mapování entit, SQL dotazy, migrace.
2. **Relační databáze v Dockeru s migracemi:**
   - Databáze (PostgreSQL) spouštěná v kontejneru.
   - Migrace schématu řízené nástrojem (např. Flyway) – žádné manuální zásahy do DDL v produkčním režimu.
3. **Minimální rozsah entit:**
   - Nejméně 5 doménových entit (`Patient`, `Doctor`, `DoctorWorkingHours`, `Appointment`, `MedicalService`, `AppointmentServiceItem`, `DoctorAbsence`).
   - Minimálně jedna vazba M:N (vazba mezi Návštěvou/Termínem a Lékařskými výkony včetně počtu aplikací).
4. **Obchodní pravidla ze zadání (MUSÍ být striktně vynucena v aplikaci):**
   - Žádný překryv termínů u téhož lékaře (ani manuálně, ani omylem).
   - Zákaz rezervace mimo platné ordinační hodiny lékaře na dané adrese.
   - Výkon nelze zapsat k neuskutečněné návštěvě.
   - Zákaz fyzického mazání historie pacienta (použít soft-delete / audit log).
5. **Syntetická data:**
   - V repozitáři ani v databázi nesmí být žádná reálná osobní data, žádná hesla ani přihlašovací údaje.
   - Seed skripty musejí generovat výhradně fiktivní data.

---

## 5. Komunikační standard a vývojový cyklus
- Komunikace s uživatelem probíhá v češtině.
- Všechny odkazy na soubory musejí být klikatelné ve formátu GitHub Markdownu s protokolem `file:///`.
- Při navrhování větších změn vždy nejprve stručně předložit návrh řešení a vyčkat na schválení.
- Průběžně ověřovat sestavitelnost kódu a funkčnost testů.

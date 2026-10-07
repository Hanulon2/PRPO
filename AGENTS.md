# AGENTS.md – Zásady a instrukce pro asistenta (Antigravity)

Tento dokument definuje závazná pravidla chování, pracovní postupy, architektonické zásady a konvence pro vývoj projektu **Sklad pro malý e-shop (Dřevěnka s.r.o.)** v rámci předmětu **Pokročilé programování (PPRO)** na FIM UHK (ZS 2026/2027).

---

## 1. Identita a role agenta
- Jsi expertní softwarový inženýr a pair-programmer asistující studentovi při vývoji semestrálního projektu.
- **Projekt:** Dřevěnka s.r.o. – Skladový a objednávkový systém pro malý e-shop.
- **Zadání:** Zadání B (Petr Doležal, Dřevěnka s.r.o. – dřevěné hračky).
- **Cíl:** Vytvořit robustní, udržitelnou, plně otestovanou aplikaci splňující veškeré požadavky klienta i povinné minimum předmětu PPRO.

---

## 2. Závazná pravidla pro Git a verzování

### 2.1 Commitování vždy s přepínačem `-m`
- **Pravidlo:** Každý commit MUSÍ být proveden s přepínačem `-m` a smysluplnou, výstižnou zprávou.
- **Formát zpráv:** Používat konvenci **Conventional Commits**:
  - `feat: <popis>` – nová funkcionalita
  - `fix: <popis>` – oprava chyby
  - `docs: <popis>` – úpravy dokumentace (včetně aktualizace README.md a DOCX)
  - `refactor: <popis>` – refaktoring kódu bez změny chování
  - `test: <popis>` – přidání nebo oprava testů
  - `chore: <popis>` – konfigurace, build nástroje, správa repozitáře
- **Příklad:** `git commit -m "feat: implement stock reservation and movement tracking for warehouse"`

### 2.2 Zákaz automatického `git push` – vyžadován explicitní povel
- **Pravidlo:** Agent **NIKDY nesmí** provést příkaz `git push` automaticky ani z vlastní iniciativy.
- **Postup:**
  1. Agent po dokončení změn provede commit s odpovídající zprávou.
  2. Agent uživatele informuje o provedeném commitu.
  3. Provedení `git push` proběhne **VÝHRADNĚ** na základě výslovného a přímého pokynu uživatele (např. „pushni to do repozitáře“).

### 2.3 Udržování dokumentace před každým commitem
- **Pravidlo:** Před každým commitem se agent **VŽDY ujistí**, že provedl nezbytné záznamy v [README.md](file:///c:/Users/38ond/Desktop/PRPO/README.md) a souboru semestrální dokumentace [PPRO-dokumentace-sablona-v1.docx](file:///c:/Users/38ond/Desktop/PRPO/PPRO-dokumentace-sablona-v1.docx).
- **Co se zaznamenává:**
  - Nová doménová a architektonická rozhodnutí (ADR / R1..Rn).
  - Požadavky a změny zadané klientem (Z1..Zn).
  - Změny v datovém modelu a rozhraních.
  - Stav implementovaných funkcí v checklistu postupu.
- **Kontrola pomocí hooku:** V repozitáři je nastaven pre-commit hook, který kontroluje stav dokumentace před vytvořením commitu.

---

## 3. Dokumentace jako Jediný zdroj pravdy (Single Source of Truth)

- Soubor [README.md](file:///c:/Users/38ond/Desktop/PRPO/README.md) a [PPRO-dokumentace-sablona-v1.docx](file:///c:/Users/38ond/Desktop/PRPO/PPRO-dokumentace-sablona-v1.docx) slouží jako **jediný zdroj pravdy** pro:
  1. Kompletní zadání klienta (funkční požadavky, obchodní pravidla, otevřené body).
  2. Technickou dokumentaci architektury (tři vrstvy, závislosti, perzistence).
  3. Seznam rozhodnutí (ADR / R1..Rn) vysvětlující vyřešení všech nejednoznačností klienta.
  4. Datový a doménový model (diagramy, entity, M:N vazby).
  5. Příručku pro sestavení, spuštění (`docker compose up`) a testování.
  6. Průběžný changelog a záznam o postupu prací.
- Kód a dokumentace musejí zůstat v absolutní shodě. Změna v návrhu = okamžitá aktualizace dokumentace.

---

## 4. Architektonické a technologické mantinely (PPRO Standard)

1. **Třívrstvá architektura se závislostmi jedním směrem:**
   - **Prezentační vrstva (Controllers / Web UI / Mobile UI):** Zpracování vstupů, validace DTO, zobrazení chybových stavů a skladových ukazatelů. Závisí pouze na aplikační vrstvě.
   - **Aplikační a doménová vrstva (Services / Domain Entities):** Obchodní logika (disponibilní zásoba, rezervace, meziskladové přesuny, inventura). Nezávisí na prezentační vrstvě ani na detailech databáze.
   - **Datová / Perzistenční vrstva (Repositories / DB):** Mapování entit, SQL dotazy, transakce, migrace.
2. **Technologický stack:**
   - **Java 21 LTS** + **Spring Boot 3.4.x**.
   - **Relační databáze:** PostgreSQL 16 v Docker kontejneru.
   - **Migrace schématu:** Flyway (verzované migrace DDL + syntetický seed).
   - **Kontejnerizace:** Docker Compose (`docker compose up --build`).
   - **Šablony & UI:** Spring MVC + Thymeleaf + responzivní Vanilla CSS.
3. **Minimální rozsah entit:**
   - Nejméně 5 doménových entit (`Product`, `Category`, `Warehouse`, `StockItem`, `StockMovement`, `Customer`, `CustomerOrder`, `OrderItem`).
   - Minimálně jedna vazba M:N (vazba mezi Produktem a Kategoriemi: tentýž produkt patří do více kategorií současně).
4. **Obchodní pravidla ze zadání (MUSÍ být striktně vynucena v aplikaci):**
   - Vydat víc, než je na skladě, nesmí jít (kontrola disponibilní zásoby a DB constraint nezáporného množství).
   - Cena v odeslané objednávce se nesmí zpětně změnit při úpravě ceníku (Price Snapshot v `OrderItem`).
   - Každá změna stavu zásob musí být dohledatelná (neměnný auditní log `StockMovement`).
   - Zákazník se fyzicky nemaže kvůli reklamacím (soft-delete).
5. **Syntetická data:**
   - V repozitáři ani v databázi nesmí být žádná reálná osobní data, žádná hesla ani přihlašovací údaje.

---

## 5. Komunikační standard a vývojový cyklus
- Komunikace s uživatelem probíhá v češtině/slovenštině.
- Všechny odkazy na soubory musejí být klikatelné ve formátu GitHub Markdownu s protokolem `file:///`.
- Při navrhování větších změn vždy nejprve stručně předložit návrh řešení a vyčkat na schválení.
- Průběžně ověřovat sestavitelnost kódu a funkčnost testů.

# Dřevěnka s.r.o. – Skladový a objednávkový systém pro malý e-shop

> **Předmět:** Pokročilé programování (PPRO) – ZS 2026/2027, FIM UHK  
> **Zadání:** Zadání B: Sklad pro malý e-shop  
> **Klient:** Petr Doležal, Dřevěnka s.r.o. (výroba a prodej dřevěných hraček)  
> **Typ dokumentu:** Technická dokumentace, specifikace zadání a Jediný zdroj pravdy (Single Source of Truth)  
> **Související dokumenty:** [AGENTS.md](file:///c:/Users/38ond/Desktop/PRPO/AGENTS.md) (pravidla vývoje) | [PPRO-dokumentace-sablona-v1.docx](file:///c:/Users/38ond/Desktop/PRPO/PPRO-dokumentace-sablona-v1.docx) (semestrální zpráva)

---

## 1. Kontext a výchozí stav projektu (As-Is)

### 1.1 Klient a podnikání
Petr Doležal podniká deset let v oboru ruční výroby a prodeje dřevěných hraček (dřevěné káči, kostky, vláčky, tahací zvířátka, skládačky). Poslední tři roky firmě Dřevěnka s.r.o. trvale rostou tržby a stávající provozní nástroje přestaly vyhovovat:
- **Zboží a evidence v Excelu:** Jeden soubor se 400 položkami otevřený současně na třech různých počítačích (ve skladu, v kanceláři a doma).
- **Dva sklady v různých městech:**
  1. *Hlavní sklad Hradec Králové:* centrální expediční sklad, odkud se balí a odesílají zásilky.
  2. *Garáž Třebechovice:* sklad sezónního zboží, přebytečných zásob a polotovarů z dílny.
- **Ruční přepisování objednávek:** Objednávky přicházejí z e-shopu e-mailem a jsou ručně přepisovány do tabulky.

### 1.2 Hlavní provozní problémy klienta
- **Prodej neexistujícího zboží (Overselling):** Dvakrát měsíčně dojde k tomu, že e-shop prodá zboží, které už fyzicky není skladem. Petr Doležal pak musí volat zklamaným zákazníkům, omlouvat se a stornovat objednávky – toto označuje za nejhorší část své práce.
- **Neevidované přesuny mezi sklady:** Převozy zboží z garáže v Třebechovicích do Hradce se dnes neevidují vůbec. V garáži stav zásob „nikdy nesedí“.
- **Ztráta přehledu o umístění:** Klient nedokáže rychle odpovědět na otázku *„kde je ta káča, co jsem ji měl minulý týden“*.
- **Chybějící hlídání minimálních zásob:** Není přehled o tom, co je vyprodané a co je potřeba zadat do truhlárny do výroby.
- **Mobilní práce ve skladu:** Klient tráví půlku pracovního dne ve skladu v montérkách, potřebuje systém pohodlně ovládat z mobilního telefonu.

---

## 2. Specifikace požadavků klienta (To-Be)

### 2.1 Co klient chce (Funkční požadavky)
1. **Produkty a kategorie:**
   - Každý produkt má kód (SKU), název, popis, nákupní cenu a prodejní cenu.
   - Podpora více kategorií naráz (**vazba M:N**): tatáž dřevěná káča patří současně do kategorií *„pro batolata“* i *„dárky do 500 Kč“*.
2. **Zásoby po skladech:**
   - U každého produktu a jeho varianty okamžitě vidět rozpad stavu: kolik kusů je v Hradci a kolik v garáži v Třebechovicích.
3. **Objednávky zákazníků:**
   - Objednávka obsahuje vazbu na zákazníka, datum, stav zpracování a položky objednávky (objednané množství a prodejní cena v době nákupu).
4. **Odmítnutí objednávky při nedostatku zásob:**
   - Klíčový cíl systému: zabránit vytvoření / potvrzení objednávky, pro kterou nejsou dostupné zásoby.
5. **Dohledatelnost pohybů (Skladový deník):**
   - Ke každému výdeji a příjmu přesně vědět, ze kterého skladu se vydalo a na který sklad se naskladnilo.
6. **Přesun mezi sklady:**
   - Funkce pro evidenci závozu zboží ze zimní garáže v Třebechovicích do hlavního skladu v Hradci Králové (odpis ze skladu A, příjem na sklad B).
7. **Minimální zásoba a upozornění („Co hoří“):**
   - Seznam položek pod definovanou minimální zásobou zobrazený přímo na úvodním dashboardu s barevným odlišením.
8. **Měsíční obrat po kategoriích:**
   - Agregovaný report tržeb a marže za zvolený kalendářní měsíc rozpadlý podle jednotlivých kategorií hraček.

### 2.2 Na čem klient trvá (Striktní obchodní pravidla)
Aplikace tato pravidla vynucuje programově na úrovni doménové a datové logiky (nespoléhá se na pozornost uživatele):
- **Zákaz výdeje do záporu:** Vydat více zboží, než je aktuálně na skladě, nesmí jít za žádných okolností (databázové omezení `quantity >= 0`).
- **Neměnnost ceny v objednávce (Price Snapshot):** Jednotková prodejní cena v již odeslané objednávce se nesmí změnit, i když se v číselníku produktů upraví ceník.
- **Auditní dohledatelnost každého pohybu:** Každá změna stavu zásob musí mít za sebou trvalý neměnný záznam (kdo, kdy, jaký sklad, jaký produkt, o kolik kusů a z jakého důvodu).
- **Zákaz fyzického mazání zákazníků:** Zákazníci se nemažou kvůli garancím a případným reklamacím objednávek z minulosti (uplatněn soft-delete).

### 2.3 Ergonomie a specifika používání (Co klient prohodil mimochodem)
- **Mobilní rozhraní pro sklad:** Responzivní, snadno ovladatelné rozhraní s velkými dotykovými prvky vhodné pro mobilní telefon při manipulaci s bednami.
- **Vizuální dashboard („Co hoří“):** Barevné odlišení kritických stavů zásob (červená = vyprodáno/pod minimem, oranžová = blízko limitu, zelená = dostatek).
- **Poštovné řeší e-shop:** Skladový systém eviduje zbožové položky, poštovné a fakturace jsou ponechány pro budoucí integraci.

---

## 3. Seznam architektonických rozhodnutí (ADR)

Tato sekce detailně rozebírá klíčové body ze zadání, kde se láme návrh řešení.

### ADR-B01: Okamžik odečtení zásoby – Disponibilní vs. Fyzická zásoba (Kde se návrh láme)
* **Problém:** Co znamená „dost zásob“? Počítá se fyzický stav na regále, nebo stav snížený o zboží v rozpracovaných objednávkách? Kdy se zásoba odečítá?
* **Rozhodnutí:** Systém zavádí striktní oddělení:
  - **Fyzická zásoba (`physical_quantity`):** skutečný počet kusů přítomných na regále skladu.
  - **Rezervovaná zásoba (`reserved_quantity`):** kusy alokované pro rozpracované, dosud neexpedované objednávky.
  - **Disponibilní zásoba (`available_quantity`):** `Fyzická - Rezervovaná`.
  
  Při přijetí objednávky z e-shopu systém provede **okamžitou rezervaci**. Pokud `objednané_množství > available_quantity`, objednávka je okamžitě odmítnuta. Fyzický odpis ze skladu proběhne až při expedici (zabalení balíku). Při stornu se rezervace uvolní zpět.
* **Zdůvodnění:** Stoprocentně eliminuje *overselling*. Pokud by se zásoba odečítala až při expedici, mezitím by mohlo dojít k vyprodání téhož kusu další objednávkou.

### ADR-B02: Konsolidace expedice ze dvou skladů (Hradec vs. Třebechovice)
* **Problém:** Zákazník objedná zboží, z něhož část leží v Hradci a část v garáži v Třebechovicích. Klient nechce platit dvojí poštovné.
* **Rozhodnutí:** Zákaznické objednávky se expedují **výhradně z hlavního expedičního skladu Hradec Králové**. Pokud disponibilní stav v Hradci nestačí, ale zboží je v Třebechovicích, systém vytvoří objednávku se stavem `WAITING_FOR_TRANSFER` a vygeneruje interní požadavek na **Meziskladový přesun (svoz)**. Po závozu do Hradce skladník potvrdí příjem a objednávka postoupí k zabalení.
* **Zdůvodnění:** Zákazník obdrží jednu ucelenou zásilku za jedno poštovné a sklad má přesný seznam toho, co má dodávka z garáže přivézt.

### ADR-B03: Zákaz záporného stavu, inventura a auditní deník (StockMovement)
* **Problém:** V garáži stav zásob „nikdy nesedí“. Má systém povolit zápornou zásobu, nebo nabídnout inventuru?
* **Rozhodnutí:**
  1. Záporný stav zásoby je v databázi **striktně zakázán** pomocí integritního omezení `CHECK (physical_quantity >= 0)`.
  2. Nesoulad reálného stavu se systémem se řeší specializovaným modulem **Skladová inventura**. Ta vygeneruje speciální typ skladového pohybu `INVENTORY_CORRECTION` s povinným uvedením zjištěného rozdílu, důvodu a jména skladníka.
  3. Každá změna zásoby je evidována jako neměnný řádek v tabulce `stock_movements`.
* **Zdůvodnění:** Záporné zásoby vedou k rozpadu skladové evidence a ztrátě přehledu. Inventurní záznam zaručuje transparentní audit bez zametání mank pod koberec.

### ADR-B04: Struktura produktů a jejich variant (Barvy a provedení)
* **Problém:** Klient mluví o „té samé káče ve třech barvách“, nerozlišuje produkt a variantu.
* **Rozhodnutí:** Model rozděluje kartu produktu (`Product` – název, obecný popis, kategorie) a skladovou jednotku (`ProductVariant` / SKU – barva, EAN kód, nákupní/prodejní cena, skladové zásoby). Pro produkty bez variant se automaticky vytváří jedna výchozí varianta.
* **Zdůvodnění:** Umožňuje přesný přehled: klient ví, že červená káča je vyprodaná, zatímco modrých má v Hradci 12 kusů.

### ADR-B05: Neměnnost jednotkové ceny (Price Snapshot)
* **Problém:** Cena v odeslané objednávce se nesmí zpětně změnit, když se v administraci upraví ceník.
* **Rozhodnutí:** Entita `OrderItem` obsahuje vlastní atribut `unit_price`, do kterého se v okamžiku vytvoření objednávky nakopíruje aktuální prodejní cena z produktové karty. Objednávka se nikdy nedotazuje na aktuální cenu produktu dynamicky.
* **Zdůvodnění:** Garantuje absolutní účetní a právní integritu historických objednávek.

---

## 4. Architektura a technické řešení (Povinné minimum PPRO)

### 4.1 Třívrstvá architektura se závislostmi jedním směrem
```
+-----------------------------------------------------------------+
|               Prezentační vrstva (Presentation Layer)           |
|      - Spring MVC Controllers, Thymeleaf šablony                |
|      - Mobilní zobrazení pro sklad, Desktop zobrazení pro kancl |
+-----------------------------------------------------------------+
                                │ (volá výhradně směrem dolů)
                                ▼
+-----------------------------------------------------------------+
|            Aplikační a doménová vrstva (Application Layer)      |
|      - Skladové služby (rezervace, výdej, přesuny, inventura)   |
|      - Objednávkový stavový automat, byznys kalkulace obratu    |
+-----------------------------------------------------------------+
                                │ (volá výhradně směrem dolů)
                                ▼
+-----------------------------------------------------------------+
|            Datová / Perzistenční vrstva (Persistence Layer)     |
|      - Spring Data JPA repozitáře, PostgreSQL schéma, Flyway     |
+-----------------------------------------------------------------+
```

### 4.2 Zvolený technologický stack

| Komponenta / Vrstva | Technologie | Verze | Odůvodnění a účel |
|---|---|---|---|
| **Programovací jazyk** | **Java** | **21 LTS** | Stabilní podniková platforma, podpora virtuálních vláken a recordů. |
| **Aplikační framework** | **Spring Boot** | **3.4.x** | Průmyslový standard pro třívrstvou architekturu s dependency injection. |
| **Prezentační vrstva** | **Spring MVC + Thymeleaf** | 3.x | Rychlé server-side vykreslování s nulovou režií pro mobil i desktop. |
| **Styling & Ergonomie** | **Vanilla CSS (Mobile-First)** | CSS3 | Responzivní design pro sklad, kontrastní barvy („co hoří“). |
| **Validace vstupů** | **Jakarta Bean Validation** | 3.x | Deklarativní kontrola povinných údajů, minimálních množství a cen. |
| **Objektově-relační mapování** | **Spring Data JPA / Hibernate** | 6.x | Správa entit, repository pattern, optimalizace SQL dotazů a indexů. |
| **Databázový engine** | **PostgreSQL** | **16 Alpine** | Relační databáze s plnou ACID podporou v izolovaném Docker kontejneru. |
| **Řízení migrací schématu** | **Flyway** | 10.x | Verzované SQL migrace pro čisté DDL a bezpečné nasazení bez ručních zásahů. |
| **Sestavení a správa závislostí**| **Maven (přes `mvnw`)** | 3.9+ | Přenositelné sestavení projektu bez nutnosti lokální instalace Mavenu. |
| **Kontejnerizace & Orchestrace** | **Docker & Docker Compose** | 28+ / Compose v2 | Povinné minimum PPRO: spuštění kompletního řešení přes `docker compose up`. |
| **Testovací frameworky** | **JUnit 5, Mockito, AssertJ** | 5.x | Unit testy doménové logiky a integrační testy skladových operací. |

### 4.3 Doménový a datový model (Entity a vazby)
Model obsahuje 9 entit (požadováno minimálně 5) a vazbu M:N mezi produkty a kategoriemi:

1. **`Product` (Produkt):** `id`, `code` (SKU), `name`, `description`, `purchase_price`, `selling_price`, `min_stock_limit`, `created_at`.
2. **`Category` (Kategorie):** `id`, `name`, `description`.
3. **`ProductCategory` (Přiřazení kategorie – M:N vazba):** Vazební tabulka propojující `Product` a `Category`.
4. **`Warehouse` (Sklad):** `id`, `code` (`HRADEC`, `TREBECHOVICE`), `name`, `address`, `is_expedition_point`.
5. **`StockItem` (Stav zásoby na skladu):** `id`, `product_id` (FK), `warehouse_id` (FK), `physical_quantity`, `reserved_quantity`, unikátní index `(product_id, warehouse_id)`.
6. **`StockMovement` (Neměnný auditní skladový pohyb):** `id`, `product_id` (FK), `source_warehouse_id` (FK, nullable), `target_warehouse_id` (FK, nullable), `quantity`, `movement_type` (`RECEIPT`, `DISPATCH`, `TRANSFER`, `INVENTORY_CORRECTION`), `reason_note`, `performed_by`, `created_at`.
7. **`Customer` (Zákazník):** `id`, `name`, `email`, `phone`, `shipping_address`, `is_active` (soft-delete).
8. **`CustomerOrder` (Objednávka):** `id`, `order_number`, `customer_id` (FK), `order_date`, `status` (`PENDING`, `WAITING_FOR_TRANSFER`, `CONFIRMED`, `SHIPPED`, `CANCELLED`, `REJECTED`), `total_price`, `created_at`.
9. **`OrderItem` (Položka objednávky):** `id`, `order_id` (FK), `product_id` (FK), `quantity`, `unit_price` (zafixovaný price snapshot v době prodeje).

---

## 5. Návod ke spuštění a vývojové instrukce

### 5.1 Požadavky na prostředí
- Java JDK 21+
- Docker a Docker Compose v2+
- Git

### 5.2 Spuštění pomocí Docker Compose
Celý systém (databáze PostgreSQL + Spring Boot backend + Flyway migrace se seed daty) lze spustit jediným příkazem:
```bash
docker compose up --build
```
Aplikace bude po naběhnutí dostupná na adrese `http://localhost:8080`.

### 5.3 Spuštění testů
```bash
./mvnw test
```

---

## 6. Záznam o postupu prací a Roadmapa (Changelog)

| Fáze | Popis úkolu | Stav |
|---|---|---|
| **Fáze 1** | Změna zadání na Zadání B (Dřevěnka s.r.o.), aktualizace `AGENTS.md`, `README.md` a semestrální dokumentace `PPRO-dokumentace-sablona-v1.docx`. Příprava hrubého návrhu pro klienta. | Hotovo |
| **Fáze 2** | Inicializace Spring Boot 3 projektu (Java 21, Maven wrapper), Docker Compose s PostgreSQL 16 a Flyway migracemi schématu pro sklady a produkty. | Plánováno |
| **Fáze 3** | Implementace doménových entit (`Product`, `Category`, `Warehouse`, `StockItem`, `StockMovement`) a M:N vazby. | Plánováno |
| **Fáze 4** | Implementace servisní vrstvy: dvouúrovňové odečítání zásoby (rezervace), odmítnutí při nedostatku, přesuny mezi sklady a auditní pohybový deník. | Plánováno |
| **Fáze 5** | Webové rozhraní (Thymeleaf): mobilní pohled pro skladníka, úvodní dashboard s indikací „Co hoří“ a měsíční obrat po kategoriích. | Plánováno |
| **Fáze 6** | Unit a integrační testy pro ověření povinného minima a zákazů (záporný stav, fixace ceny, odmítnutí objednávky). | Plánováno |
| **Fáze 7** | Závěrečná validace a multi-stage Docker build. | Plánováno |

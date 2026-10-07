package cz.uhk.fim.ppro.drevenka.service;

import cz.uhk.fim.ppro.drevenka.domain.*;
import cz.uhk.fim.ppro.drevenka.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CustomerRepository customerRepository;
    private final CustomerOrderRepository orderRepository;

    public DataInitializer(CategoryRepository categoryRepository,
                           WarehouseRepository warehouseRepository,
                           ProductRepository productRepository,
                           StockItemRepository stockItemRepository,
                           StockMovementRepository stockMovementRepository,
                           CustomerRepository customerRepository,
                           CustomerOrderRepository orderRepository) {
        this.categoryRepository = categoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.stockItemRepository = stockItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        // 1. Sklady
        Warehouse hradec = warehouseRepository.save(new Warehouse(
                "HRADEC", "Hlavní sklad Hradec Králové", "Pražská 42, Hradec Králové", true
        ));
        Warehouse trebechovice = warehouseRepository.save(new Warehouse(
                "TREBECHOVICE", "Sezónní garáž Třebechovice", "Polní 15, Třebechovice pod Orebem", false
        ));

        // 2. Kategorie
        Category batolata = categoryRepository.save(new Category("Hračky pro batolata", "Bezpečné hladké dřevěné hračky pro nejmenší"));
        Category darky500 = categoryRepository.save(new Category("Dárky do 500 Kč", "Oblíbené cenově dostupné dárky k narozeninám a Vánocům"));
        Category vlacky = categoryRepository.save(new Category("Dřevěné vláčky a dráhy", "Lokomotivy, vagónky a doplňky z bukového dřeva"));
        Category didakticke = categoryRepository.save(new Category("Didaktické a motorické hračky", "Rozvíjí jemnou motoriku a logické myšlení dětí"));
        Category tradicni = categoryRepository.save(new Category("Tradiční české hračky", "Klasické prověřené hračky vyráběné dle původních vzorů"));

        // 3. Produkty (dřevěné hračky)
        Product kaca = new Product("KAC-001", "Dřevěná káča barevná", "Klasická točící se káča z javorového dřeva s ruční malbou.", new BigDecimal("45.00"), new BigDecimal("129.00"), 15);
        kaca.getCategories().add(batolata);
        kaca.getCategories().add(darky500);
        kaca.getCategories().add(tradicni);
        productRepository.save(kaca);

        Product vlacek = new Product("VLA-010", "Dřevěný vláček se zvířátky", "Souprava mašinky se dvěma vagónky a vyřezávanými figurkami zvířat.", new BigDecimal("180.00"), new BigDecimal("449.00"), 8);
        vlacek.getCategories().add(vlacky);
        vlacek.getCategories().add(darky500);
        productRepository.save(vlacek);

        Product kostky = new Product("KOS-005", "Dřevěné kostky 50 ks v kyblíku", "Přírodní bukové stavební kostky bez chemického laku.", new BigDecimal("120.00"), new BigDecimal("329.00"), 10);
        kostky.getCategories().add(batolata);
        kostky.getCategories().add(didakticke);
        kostky.getCategories().add(darky500);
        productRepository.save(kostky);

        Product konik = new Product("KON-002", "Tahací koník s rolničkou", "Tradiční dřevěný koník na kolečkách se zvonící rolničkou.", new BigDecimal("90.00"), new BigDecimal("249.00"), 6);
        konik.getCategories().add(batolata);
        konik.getCategories().add(tradicni);
        konik.getCategories().add(darky500);
        productRepository.save(konik);

        Product vkladacka = new Product("VKL-008", "Didaktická vkládačka domeček", "Dřevěný domeček s otvory různých tvarů a geometrickými tělesy.", new BigDecimal("140.00"), new BigDecimal("389.00"), 10);
        vkladacka.getCategories().add(didakticke);
        vkladacka.getCategories().add(darky500);
        productRepository.save(vkladacka);

        Product tatra = new Product("AUT-003", "Sklápěcí autíčko Tatra", "Masivní bukové autíčko s funkční sklápěcí korbou.", new BigDecimal("210.00"), new BigDecimal("499.00"), 5);
        tatra.getCategories().add(tradicni);
        tatra.getCategories().add(darky500);
        productRepository.save(tatra);

        Product puzzle = new Product("PUZ-007", "Dřevěné puzzle abeceda", "Barevná písmenka s podložkou z lipového dřeva.", new BigDecimal("75.00"), new BigDecimal("199.00"), 12);
        puzzle.getCategories().add(didakticke);
        puzzle.getCategories().add(darky500);
        productRepository.save(puzzle);

        // 4. Zásoby po skladech
        // Káča: v Hradci 4 ks (z toho 1 rezervována, volno 3 ks < min 15) -> CO HOŘÍ! V Třebechovicích 35 ks (stačí svézt)
        stockItemRepository.save(new StockItem(kaca, hradec, 4, 1));
        stockItemRepository.save(new StockItem(kaca, trebechovice, 35, 0));

        // Vláček: v Hradci 2 ks (volno 2 < min 8), v garáži 0 -> CO HOŘÍ!
        stockItemRepository.save(new StockItem(vlacek, hradec, 2, 0));
        stockItemRepository.save(new StockItem(vlacek, trebechovice, 0, 0));

        // Kostky: v Hradci 28 ks, v garáži 50 ks -> Dostatek
        stockItemRepository.save(new StockItem(kostky, hradec, 28, 2));
        stockItemRepository.save(new StockItem(kostky, trebechovice, 50, 0));

        // Koník: v Hradci 12 ks, v garáži 15 ks -> Dostatek
        stockItemRepository.save(new StockItem(konik, hradec, 12, 0));
        stockItemRepository.save(new StockItem(konik, trebechovice, 15, 0));

        // Vkládačka: v Hradci 3 ks (volno 3 < min 10), v garáži 25 ks -> Nutný svoz
        stockItemRepository.save(new StockItem(vkladacka, hradec, 3, 0));
        stockItemRepository.save(new StockItem(vkladacka, trebechovice, 25, 0));

        // Tatra: v Hradci 14 ks, v garáži 30 ks
        stockItemRepository.save(new StockItem(tatra, hradec, 14, 0));
        stockItemRepository.save(new StockItem(tatra, trebechovice, 30, 0));

        // Puzzle: 0 ks všude -> VYPRODÁNO!
        stockItemRepository.save(new StockItem(puzzle, hradec, 0, 0));
        stockItemRepository.save(new StockItem(puzzle, trebechovice, 0, 0));

        // 5. Zákazníci
        Customer c1 = customerRepository.save(new Customer("Jan Novák", "novak.jan@seznam.cz", "+420 777 123 456", "Brněnská 15, Hradec Králové"));
        Customer c2 = customerRepository.save(new Customer("Marie Dvořáková", "marie.dvorakova@email.cz", "+420 608 987 654", "Pernštýnské nám. 4, Pardubice"));
        Customer c3 = customerRepository.save(new Customer("Petr Kovář", "petr.kovar@gmail.com", "+420 724 555 666", "Vodičkova 20, Praha 1"));

        // 6. Ukázkové objednávky
        // Objednávka 1: Expedovaná
        CustomerOrder o1 = new CustomerOrder("OBJ-2026-0001", c1, OrderStatus.SHIPPED, "Expedováno dopravci PPL.");
        o1.addItem(new OrderItem(kostky, 2, kostky.getSellingPrice()));
        o1.addItem(new OrderItem(tatra, 1, tatra.getSellingPrice()));
        orderRepository.save(o1);

        // Objednávka 2: Připraveno k zabalení v Hradci (CONFIRMED)
        CustomerOrder o2 = new CustomerOrder("OBJ-2026-0002", c2, OrderStatus.CONFIRMED, "Zboží rezervováno v Hradci. Čeká na zabalení u balicího stolu.");
        o2.addItem(new OrderItem(kaca, 1, kaca.getSellingPrice()));
        orderRepository.save(o2);

        // Objednávka 3: Čeká na svoz z garáže (WAITING_FOR_TRANSFER)
        CustomerOrder o3 = new CustomerOrder("OBJ-2026-0003", c3, OrderStatus.WAITING_FOR_TRANSFER, "Čeká na svoz 5 ks z garáže Třebechovice.");
        o3.addItem(new OrderItem(vkladacka, 5, vkladacka.getSellingPrice()));
        orderRepository.save(o3);

        // 7. Pohyby v auditním deníku (StockMovement)
        stockMovementRepository.save(new StockMovement(
                kaca, null, trebechovice, 40, MovementType.RECEIPT, null, "Petr Doležal", "Příjem z truhlárny – nová šarže káč"
        ));
        stockMovementRepository.save(new StockMovement(
                kaca, trebechovice, hradec, 10, MovementType.TRANSFER, null, "Petr Doležal", "Pravidelný svoz dodávkou do Hradce"
        ));
        stockMovementRepository.save(new StockMovement(
                kostky, hradec, null, 2, MovementType.DISPATCH, o1.getId(), "Petr Doležal", "Expedice objednávky OBJ-2026-0001"
        ));
        stockMovementRepository.save(new StockMovement(
                tatra, hradec, null, 1, MovementType.DISPATCH, o1.getId(), "Petr Doležal", "Expedice objednávky OBJ-2026-0001"
        ));
        stockMovementRepository.save(new StockMovement(
                vlacek, hradec, null, 1, MovementType.INVENTORY_CORRECTION, null, "Petr Doležal", "Inventura: poškozený kus při manipulaci odepsán do odpadu"
        ));
    }
}

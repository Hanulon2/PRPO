package cz.uhk.fim.ppro.drevenka.domain;

public enum MovementType {
    RECEIPT("Příjem z výroby"),
    DISPATCH("Výdej zákazníkovi"),
    TRANSFER("Meziskladový přesun"),
    INVENTORY_CORRECTION("Inventurní úprava");

    private final String title;

    MovementType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}

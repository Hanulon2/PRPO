package cz.uhk.fim.ppro.drevenka.domain;

public enum OrderStatus {
    CONFIRMED("Potvrzeno (zboží rezervováno v Hradci)"),
    WAITING_FOR_TRANSFER("Čeká na svoz z garáže Třebechovice"),
    SHIPPED("Expedováno (zabaleno a odesláno)"),
    REJECTED("Odmítnuto (nedostatek zásob)"),
    CANCELLED("Stornováno");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

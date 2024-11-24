package br.com.multiprodutora.ticketeria.domain;

public enum Status {
    ACTIVE, // The object is Active and can be used
    INACTIVE, // The object is Inactive and can't be used
    PENDING, // The object is Pending and have to be approved
    CANCELED, // The object is Canceled and can't be used
    DELETED, // The object is Deleted and can't be used
    AUTHORIZED,
    BLOCKED,
    APPROVED,
    REJECTED,
    IN_PROCESS, UNKNOWN;

    public static Status fromMercadoPagoStatus(String mpStatus) {
        switch (mpStatus) {
            case "approved":
                return APPROVED;
            case "in_process":
                return IN_PROCESS;
            case "rejected":
                return REJECTED;
            default:
                return PENDING;
        }
    }
}

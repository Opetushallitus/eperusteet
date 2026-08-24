package fi.vm.sade.eperusteet.repository.version;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Välittää revisiokommentin Enversin RevisionListenerille.
 * Hibernate 7 poisti {@code AuditReader#getCurrentRevision}.
 */
public final class RevisioKommenttiHolder {

    private static final ThreadLocal<String> KOMMENTTI = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> CLEANUP_REGISTERED = new ThreadLocal<>();

    private RevisioKommenttiHolder() {
    }

    public static void set(String kommentti) {
        KOMMENTTI.set(kommentti);
        registerCleanup();
    }

    public static String poll() {
        try {
            return KOMMENTTI.get();
        } finally {
            KOMMENTTI.remove();
        }
    }

    private static void registerCleanup() {
        if (Boolean.TRUE.equals(CLEANUP_REGISTERED.get())) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                KOMMENTTI.remove();
                CLEANUP_REGISTERED.remove();
            }
        });
        CLEANUP_REGISTERED.set(Boolean.TRUE);
    }
}

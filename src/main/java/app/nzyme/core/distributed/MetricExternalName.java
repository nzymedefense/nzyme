package app.nzyme.core.distributed;

public enum MetricExternalName {

    MEMORY_BYTES_TOTAL("memory_bytes_total"),
    MEMORY_BYTES_AVAILABLE("memory_bytes_available"),
    MEMORY_BYTES_USED("memory_bytes_used"),
    HEAP_BYTES_TOTAL("heap_bytes_total"),
    HEAP_BYTES_AVAILABLE("heap_bytes_available"),
    HEAP_BYTES_USED("heap_bytes_used"),
    CPU_SYSTEM_LOAD("cpu_system_load"),
    PROCESS_VIRTUAL_SIZE("process_virtual_size"),
    TAP_REPORT_SIZE("tap_report_size"),
    PGP_ENCRYPTION_TIMER("pgp_encryption_timer"),
    PGP_DECRYPTION_TIMER("pgp_decryption_timer");

    public final String database_label;

    MetricExternalName(String database_label) {
        this.database_label = database_label;
    }

}

package app.nzyme.core.ethernet.recon;

public enum ReconTargetOutcome {

    UNANSWERED(0),
    CLOSED(1),
    OPEN(2);

    private final int rank;

    ReconTargetOutcome(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public static ReconTargetOutcome fromRank(int rank) {
        for (ReconTargetOutcome o : values()) {
            if (o.rank == rank) {
                return o;
            }
        }

        return UNANSWERED;
    }

}

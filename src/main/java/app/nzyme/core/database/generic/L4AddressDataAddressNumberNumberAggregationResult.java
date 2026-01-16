package app.nzyme.core.database.generic;

import app.nzyme.core.ethernet.l4.db.L4AddressData;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class L4AddressDataAddressNumberNumberAggregationResult {

    public abstract L4AddressData key();
    public abstract long value1();
    public abstract long value2();

    public static L4AddressDataAddressNumberNumberAggregationResult create(L4AddressData key, long value1, long value2) {
        return builder()
                .key(key)
                .value1(value1)
                .value2(value2)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_L4AddressDataAddressNumberNumberAggregationResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder key(L4AddressData key);

        public abstract Builder value1(long value1);

        public abstract Builder value2(long value2);

        public abstract L4AddressDataAddressNumberNumberAggregationResult build();
    }
}

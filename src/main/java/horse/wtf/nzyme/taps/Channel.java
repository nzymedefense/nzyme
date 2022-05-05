package horse.wtf.nzyme.taps;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class Channel {

    public abstract Long busId();
    public abstract String name();
    public abstract Long capacity();
    public abstract Long watermark();
    public abstract TotalWithAverage errors();
    public abstract TotalWithAverage throughputBytes();
    public abstract TotalWithAverage throughputMessages();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static Channel create(Long busId, String name, Long capacity, Long watermark, TotalWithAverage errors, TotalWithAverage throughputBytes, TotalWithAverage throughputMessages, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .busId(busId)
                .name(name)
                .capacity(capacity)
                .watermark(watermark)
                .errors(errors)
                .throughputBytes(throughputBytes)
                .throughputMessages(throughputMessages)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Channel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder busId(Long busId);

        public abstract Builder name(String name);

        public abstract Builder capacity(Long capacity);

        public abstract Builder watermark(Long watermark);

        public abstract Builder errors(TotalWithAverage errors);

        public abstract Builder throughputBytes(TotalWithAverage throughputBytes);

        public abstract Builder throughputMessages(TotalWithAverage throughputMessages);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Channel build();
    }

}

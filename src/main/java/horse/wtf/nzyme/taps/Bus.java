package horse.wtf.nzyme.taps;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Bus {

    public abstract Long id();
    public abstract List<Channel> channels();

    public static Bus create(Long id, List<Channel> channels) {
        return builder()
                .id(id)
                .channels(channels)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Bus.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder channels(List<Channel> channels);

        public abstract Bus build();
    }

}

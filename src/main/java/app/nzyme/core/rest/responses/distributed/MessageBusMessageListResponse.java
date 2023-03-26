package app.nzyme.core.rest.responses.distributed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class MessageBusMessageListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("messages")
    public abstract List<MessageBusMessageResponse> messages();

    public static MessageBusMessageListResponse create(long count, List<MessageBusMessageResponse> messages) {
        return builder()
                .count(count)
                .messages(messages)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MessageBusMessageListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder messages(List<MessageBusMessageResponse> messages);

        public abstract MessageBusMessageListResponse build();
    }

}

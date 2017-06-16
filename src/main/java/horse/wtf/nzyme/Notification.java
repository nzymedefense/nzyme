package horse.wtf.nzyme;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class Notification {

    private final ImmutableMap.Builder<String, Object> fields;
    private final String message;

    public Notification(String message) {
        this.fields = new ImmutableMap.Builder<>();
        this.message = message;
    }

    public Notification addField(String key, Object value) {
        fields.put("_" + key, value);

        return this;
    }

    public Map<String, Object> getAdditionalFields() {
        return fields.build();
    }

    public String getMessage() {
        return message;
    }

}

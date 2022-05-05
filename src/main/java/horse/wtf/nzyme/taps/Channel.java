package horse.wtf.nzyme.taps;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Channel {

    public abstract Long busId();
    public abstract String name();
    public abstract Long capacity();


}

package horse.wtf.nzyme.channels;

import java.util.concurrent.atomic.AtomicBoolean;

public class Lock {

    private final AtomicBoolean locked;

    public Lock() {
        this.locked = new AtomicBoolean(false);
    }

    public void lock() {
        this.locked.set(true);
    }

    public void unlock() {
        this.locked.set(false);
    }

    public void await() throws InterruptedException {
        while(this.locked.get()) {
            Thread.sleep(5);
        }
    }

}

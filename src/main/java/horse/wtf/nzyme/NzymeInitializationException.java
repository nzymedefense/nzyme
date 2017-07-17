package horse.wtf.nzyme;

public class NzymeInitializationException extends Exception {

    NzymeInitializationException(String msg) {
        super(msg);
    }

    NzymeInitializationException(String msg, Throwable e) {
        super(msg, e);
    }

}

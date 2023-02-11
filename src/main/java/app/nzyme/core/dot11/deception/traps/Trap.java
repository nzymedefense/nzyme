/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.dot11.deception.traps;

/*
 * . . . . . . . . . . . . . . . . _,,,--~~~~~~~~--,_
 * . . . . . . . . . . . . . . ,-' : : : :::: :::: :: : : : : :º '-, ITS A TRAP!
 * . . . . . . . . . . . . .,-' :: : : :::: :::: :::: :::: : : :o : '-,
 * . . . . . . . . . . . ,-' :: ::: :: : : :: :::: :::: :: : : : : :O '-,
 * . . . . . . . . . .,-' : :: :: :: :: :: : : : : : , : : :º :::: :::: ::';
 * . . . . . . . . .,-' / / : :: :: :: :: : : :::: :::-, ;; ;; ;; ;; ;; ;; ;\
 * . . . . . . . . /,-',' :: : : : : : : : : :: :: :: : '-, ;; ;; ;; ;; ;; ;;|
 * . . . . . . . /,',-' :: :: :: :: :: :: :: : ::_,-~~,_'-, ;; ;; ;; ;; |
 * . . . . . _/ :,' :/ :: :: :: : : :: :: _,-'/ : ,-';'-'''''~-, ;; ;; ;;,'
 * . . . ,-' / : : : : : : ,-''' : : :,--'' :|| /,-'-'--'''__,''' \ ;; ;,-'/
 * . . . \ :/,, : : : _,-' --,,_ : : \ :\ ||/ /,-'-'x### ::\ \ ;;/
 * . . . . \/ /---'''' : \ #\ : :\ : : \ :\ \| | : (O##º : :/ /-''
 * . . . . /,'____ : :\ '-#\ : \, : :\ :\ \ \ : '-,___,-',-`-,,
 * . . . . ' ) : : : :''''--,,--,,,,,,¯ \ \ :: ::--,,_''-,,'''¯ :'- :'-,
 * . . . . .) : : : : : : ,, : ''''~~~~' \ :: :: :: :'''''¯ :: ,-' :,/\
 * . . . . .\,/ /|\\| | :/ / : : : : : : : ,'-, :: :: :: :: ::,--'' :,-' \ \
 * . . . . .\\'|\\ \|/ '/ / :: :_--,, : , | )'; :: :: :: :,-'' : ,-' : : :\ \,
 * . . . ./¯ :| \ |\ : |/\ :: ::----, :\/ :|/ :: :: ,-'' : :,-' : : : : : : ''-,,
 * . . ..| : : :/ ''-(, :: :: :: '''''~,,,,,'' :: ,-'' : :,-' : : : : : : : : :,-'''\\
 * . ,-' : : : | : : '') : : :¯''''~-,: : ,--''' : :,-'' : : : : : : : : : ,-' :¯'''''-,_ .
 * ./ : : : : :'-, :: | :: :: :: _,,-''''¯ : ,--'' : : : : : : : : : : : / : : : : : : :''-,
 * / : : : : : -, :¯'''''''''''¯ : : _,,-~'' : : : : : : : : : : : : : :| : : : : : : : : :
 * : : : : : : : :¯''~~~~~~''' : : : : : : : : : : : : : : : : : : | : : : : : : : : :
 */

import app.nzyme.core.configuration.InvalidConfigurationException;
import app.nzyme.core.dot11.Dot11FrameInterceptor;
import app.nzyme.core.dot11.probes.Dot11SenderProbe;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Trap {


    public enum Type {
        PROBE_REQUEST_1,
        BEACON_1
    }

    private Dot11SenderProbe probe;

    public abstract void checkConfiguration() throws InvalidConfigurationException;

    protected abstract boolean doRun();
    public abstract int getDelayMilliseconds();
    public abstract int framesPerExecution();

    public abstract Type getType();
    public abstract String getDescription();

    public abstract List<Dot11FrameInterceptor> requestedInterceptors();

    public boolean run() {
        return doRun();
    }

    public void setProbe(Dot11SenderProbe probe) {
        this.probe = probe;
    }

    @Nullable
    public Dot11SenderProbe getProbe() {
        return this.probe;
    }

}

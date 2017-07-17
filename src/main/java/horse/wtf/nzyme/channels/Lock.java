/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

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

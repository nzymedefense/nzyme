/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.util;

import java.io.File;

public class Sounds {

    private static final String PATH = "/usr/share/nzyme/sounds/";  // TODO make path configurable

    public static File getSound(String name) throws SoundNotFoundException {
        String path = PATH + name + ".wav";
        File file = new File(path);

        if (!file.exists()) {
            throw new SoundNotFoundException("Sound [" + name + "] not found at [" + path + "].");
        }

        return file;
    }

    private static class SoundNotFoundException extends Exception {

        SoundNotFoundException(String msg) {
            super(msg);
        }

    }

}

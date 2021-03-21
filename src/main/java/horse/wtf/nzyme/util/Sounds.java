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

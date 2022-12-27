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

package app.nzyme.core.util;

public class Wall {

    private static final int MAX_WIDTH = 100;

    public static String build(String title, String content) {
        StringBuilder sb = new StringBuilder();

        String separator = buildSeparator(MAX_WIDTH, '-');

        sb.append(separator)
                .append("\n")
                .append(title)
                .append("\n")
                .append("\n")
                .append(wrapContent(MAX_WIDTH, content))
                .append("\n")
                .append(separator);

        return sb.toString();
    }

    private static String buildSeparator(int width, char character) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < width; i++) {
            sb.append(character);
        }

        return sb.toString();
    }

    private static String wrapContent(int width, String content) {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        for(char c : content.toCharArray()) {
            i++;

            if(i > width) {
                sb.append("\n");
                i = 1;
            }

            sb.append(c);
        }

        return sb.toString();
    }

}

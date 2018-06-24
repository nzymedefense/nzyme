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

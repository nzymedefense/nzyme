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

package horse.wtf.nzyme.dot11;

public class Dot11FrameSubtype {

    public static final byte ASSOCIATION_REQUEST = 0;
    public static final byte ASSOCIATION_RESPONSE = 1;
    public static final byte PROBE_REQUEST = 4;
    public static final byte PROBE_RESPONSE = 5;
    public static final byte BEACON = 8;
    public static final byte DISASSOCIATION = 10;
    public static final byte AUTHENTICATION = 11;
    public static final byte DEAUTHENTICATION = 12;

}

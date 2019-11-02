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

package horse.wtf.nzyme.dot11.networks.signalstrength.tracks;

/*
 * Give me a ping, Vasily. One ping only, please.
 */
public class TrackDetector {

    private final SignalWaterfallHistogram histogram;

    public TrackDetector(SignalWaterfallHistogram histogram) throws IllegalArgumentException {
        this.histogram = histogram;
    }

    // TODO let user download json of histogram. feed into test.

}

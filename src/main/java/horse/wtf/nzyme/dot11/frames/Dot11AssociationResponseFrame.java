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

package horse.wtf.nzyme.dot11.frames;

import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;

@AutoValue
public abstract class Dot11AssociationResponseFrame {

    public abstract String transmitter();
    public abstract String destination();
    public abstract String response();
    public abstract Short responseCode();
    public abstract Dot11MetaInformation meta();

    public static Dot11AssociationResponseFrame create(String transmitter, String destination, String response, Short responseCode, Dot11MetaInformation meta) {
        return builder()
                .transmitter(transmitter)
                .destination(destination)
                .response(response)
                .responseCode(responseCode)
                .meta(meta)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11AssociationResponseFrame.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder transmitter(String transmitter);

        public abstract Builder destination(String destination);

        public abstract Builder response(String response);

        public abstract Builder responseCode(Short responseCode);

        public abstract Builder meta(Dot11MetaInformation meta);

        public abstract Dot11AssociationResponseFrame build();
    }

}

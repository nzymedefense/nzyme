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

package horse.wtf.nzyme.configuration;

import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.Validator;
import com.google.common.base.Splitter;

public class SplittableListValidator implements Validator<String> {

    @Override
    public void validate(String name, String value) throws ValidationException {
        try {
            for (String s : Splitter.on(",").omitEmptyStrings().split(value)) {
                Integer.valueOf(s);
            }
        } catch(Exception e){
            throw new ValidationException("Parameter `channels` must be a list of integers.", e);
        }
    }

}

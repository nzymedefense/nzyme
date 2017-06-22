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

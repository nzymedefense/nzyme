package horse.wtf.nzyme.configuration;

import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.Validator;

public class InternetAddressValidator implements Validator<String> {

    @Override
    public void validate(String name, String value) throws ValidationException {
        for (String s : value.split(",")) {
            if(!s.contains(":") || s.startsWith(":") || s.endsWith(":")) {
                throw new ValidationException("Malformed internet address.");
            }

            String[] parts = s.split(":");

            if(parts.length != 2) {
                throw new ValidationException("Malformed internet address");
            }

            try {
                int port = Integer.valueOf(parts[1]);

                if (port <= 0 || port > 65535) {
                    throw new ValidationException("Invalid port number.");
                }
            } catch(NumberFormatException e) {
                throw new ValidationException("Port is not a number");
            }
        }
    }

}

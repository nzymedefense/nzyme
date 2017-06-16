package horse.wtf.nzyme;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

public class InternetAddressValidator implements IValueValidator<String> {

    @Override
    public void validate(String name, String value) throws ParameterException {
        if(!value.contains(":") || value.startsWith(":") || value.endsWith(":")) {
            throw new ParameterException("Malformed internet address.");
        }

        String[] parts = value.split(":");

        if(parts.length != 2) {
            throw new ParameterException("Malformed internet address");
        }

        try {
            int port = Integer.valueOf(parts[1]);

            if (port <= 0 || port > 65535) {
                throw new ParameterException("Invalid port number.");
            }
        } catch(NumberFormatException e) {
            throw new ParameterException("Port is not a number");
        }
    }

}

package app.nzyme.core;

import java.io.File;
import java.net.URL;

public class ResourcesAccessingTest {

    protected File loadFromResourceFile(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        if (resource == null) {
            throw new RuntimeException("test config file does not exist in resources");
        }

        return new File(resource.getFile());
    }

}

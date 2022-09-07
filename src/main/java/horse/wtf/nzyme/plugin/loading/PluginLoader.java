package horse.wtf.nzyme.plugin.loading;

import app.nzyme.plugin.Plugin;
import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

public class PluginLoader {

    private static final Logger LOG = LogManager.getLogger(PluginLoader.class);

    private final File pluginDir;

    public PluginLoader(File pluginDir) {
        this.pluginDir = pluginDir;
    }

    public List<Plugin> loadPlugins() {
        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            LOG.warn("Configured plugin directory [{}] not found or not a directory. " +
                    "Cannot load plugins.", pluginDir.getAbsolutePath());
            return Collections.emptyList();
        }

        LOG.info("Scanning [{}] for plugins.", pluginDir.getAbsolutePath());

        // Translate all JAR files to URLs.
        List<URL> pluginUrls = Lists.newArrayList();
        for (File file : Files.fileTraverser().breadthFirst(pluginDir)) {
            if (!file.isFile()) {
                continue;
            }

            if (!file.getName().endsWith(".jar")) {
                LOG.warn("File [{}] in plugins folder does not have .jar suffix. Skipping.", file.getAbsolutePath());
            }

            try {
                pluginUrls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                LOG.error("Could not create URL for plugin [{}].", file.getAbsolutePath(), e);
                continue;
            }
        }

        URLClassLoader ucl = new URLClassLoader(pluginUrls.toArray(new URL[0]));
        ServiceLoader<Plugin> sl = ServiceLoader.load(Plugin.class, ucl);

        ImmutableList.Builder<Plugin> plugins = new ImmutableList.Builder<Plugin>();
        for (Plugin plugin : sl) {
            plugins.add(plugin);
        }

        return plugins.build();
    }

}

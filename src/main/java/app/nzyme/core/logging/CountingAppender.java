package app.nzyme.core.logging;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.util.Map;

@Plugin(name = "CountingAppender", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
public class CountingAppender extends AbstractAppender {

    private static final Map<String, Long> counts = Maps.newConcurrentMap();

    protected CountingAppender(String name, Layout<?> layout) {
        super(name, null, layout, true);
    }

    @Override
    public void append(LogEvent event) {
        counts.merge(event.getLevel().name(), 1L, Long::sum);
    }

    @PluginFactory
    public static CountingAppender createAppender(@PluginAttribute("name") String name,
                                                  @PluginElement("Layout") Layout<?> layout) {
        return new CountingAppender(name, layout);
    }

    public static Map<String, Long> getCounts() {
        return Maps.newHashMap(counts);
    }

    public static void resetCounts() {
        counts.clear();
    }

}

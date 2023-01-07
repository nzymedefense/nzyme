package app.nzyme.core.dot11.clients;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.dot11.frames.Dot11AssociationRequestFrame;
import app.nzyme.core.dot11.frames.Dot11ProbeRequestFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Clients {

    private static final Logger LOG = LogManager.getLogger(Clients.class);

    private final Map<String, Client> clients;
    private final NzymeNode nzyme;

    public Clients(NzymeNode nzyme) {
        this.nzyme = nzyme;

        this.clients = Maps.newHashMap();

        // Regularly delete networks that have not been seen for a while.
        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("clients-cleaner")
                        .build()
        ).scheduleAtFixedRate(() -> retentionClean(300), 1, 1, TimeUnit.MINUTES);
    }

    public void registerProbeRequestFrame(Dot11ProbeRequestFrame frame) {
        register(frame.requester());
    }

    public void registerAssociationRequestFrame(Dot11AssociationRequestFrame frame) {
        register(frame.transmitter());
    }

    private synchronized void register(String mac) {
        Client client;
        if (clients.containsKey(mac)) {
            clients.get(mac).updateLastSeen();
        } else {
            String oui = nzyme.getOUIManager().lookupBSSID(mac);

            if (oui == null) {
                oui = "unknown";
            }

            client = Client.create(oui, mac);

            clients.put(mac, client);
        }
    }

    public Map<String, Client> getClients() {
        return new ImmutableMap.Builder<String, Client>().putAll(clients).build();
    }

    public void retentionClean(int seconds) {
        try {
            for (Map.Entry<String, Client> entry : Lists.newArrayList(clients.entrySet())) {
                Client client = entry.getValue();

                if (client.lastSeen.isBefore(DateTime.now().minusSeconds(seconds))) {
                    LOG.debug("Retention cleaning expired client [{}] from internal clients list.", client);
                    clients.remove(entry.getKey());
                }
            }
        } catch(Exception e) {
            LOG.error("Error when trying to clean expired clients.", e);
        }
    }

}

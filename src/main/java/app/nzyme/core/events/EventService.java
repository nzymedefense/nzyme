/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.events;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.core.NzymeNode;
import app.nzyme.core.events.db.EventRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventService {

    private static final Logger LOG = LogManager.getLogger(EventService.class);

    private final NzymeNode nzyme;

    private final Map<Event.TYPE, List<Consumer<Event>>> subscribers;

    public EventService(NzymeNode nzyme) {
        this.nzyme = nzyme;
        this.subscribers = Maps.newHashMap();
    }

    public void recordEvent(Event event) {
        recordEvent(event, DateTime.now());
    }

    public void recordEvent(Event event, DateTime timestamp) {
        try {
            if (subscribers.containsKey(event.type())) {
                for (Consumer<Event> callback : subscribers.get(event.type())) {
                    callback.accept(event);
                }
            }
        } catch(Exception e) {
            LOG.error("Callback for event [{}] failed. Continuing.", event, e);
        }

        nzyme.getDatabase().useHandle(handle -> handle.execute("INSERT INTO events(type, name, description, created_at) VALUES(?, ?, ?, ?)",
                event.type().toString(),
                event.name(),
                event.description(),
                timestamp.withMillisOfSecond(0)
        ));
    }

    public long countAll() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM events")
                        .mapTo(Long.class)
                        .one()
        );
    }

    public long countAllOfTypeOfLast24Hours(Event.TYPE type) {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM events WHERE type = :type AND created_at > :created_at")
                        .bind("type", type.toString())
                        .bind("created_at", DateTime.now().minusHours(24))
                        .mapTo(Long.class)
                        .one()
        );
    }

   public List<EventRecord> findAllEventsOfLast24Hours() {
        return nzyme.getDatabase().withHandle(handle ->
                handle.createQuery("SELECT * FROM events WHERE created_at > :created_at ORDER BY created_at")
                        .bind("created_at", DateTime.now().minusHours(24))
                        .mapTo(EventRecord.class)
                        .list()
        );
    }

    public void subscribe(Event.TYPE type, Consumer<Event> callback) {
        subscribers.putIfAbsent(type, Lists.newArrayList());
        subscribers.get(type).add(callback);
    }

    public Map<Event.TYPE, List<Consumer<Event>>> getSubscribers() {
        return Maps.newHashMap(subscribers);
    }

}

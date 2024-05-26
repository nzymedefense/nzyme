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

package app.nzyme.core.util;

import app.nzyme.core.context.ContextService;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.integrations.geoip.GeoIpService;
import app.nzyme.core.ouis.OUIManager;
import app.nzyme.core.rest.interceptors.TapTableSizeInterceptor;
import app.nzyme.core.security.authentication.PasswordHasher;
import app.nzyme.core.tables.dns.DNSTable;
import app.nzyme.core.tables.dot11.Dot11Table;
import app.nzyme.core.tables.socks.SocksTable;
import app.nzyme.core.tables.tcp.TCPTable;

import static com.codahale.metrics.MetricRegistry.name;

public class MetricNames {

    public static final String OUI_LOOKUP_TIMING = name(OUIManager.class, "lookup-timing");
    public static final String DATABASE_SIZE = name(DatabaseImpl.class, "size");
    public static final String GEOIP_CACHE_SIZE = name(GeoIpService.class, "cache-size");
    public static final String PGP_ENCRYPTION_TIMING = name(Crypto.class, "encryption-timing");
    public static final String PGP_DECRYPTION_TIMING = name(Crypto.class, "decryption-timing");
    public static final String PASSWORD_HASHING_TIMER = name(PasswordHasher.class, "hashing-timer");
    public static final String TAP_TABLE_REQUEST_SIZES = name(TapTableSizeInterceptor.class, "request_size");
    public static final String CONTEXT_MAC_CACHE_SIZE = name(ContextService.class, "mac-cache-size");
    public static final String CONTEXT_MAC_LOOKUP_TIMING = name(ContextService.class, "mac-lookup-timing");

    public static final String DOT11_TOTAL_REPORT_PROCESSING_TIMER = name(Dot11Table.class, "total-report-processing-timing");
    public static final String DOT11_BSSID_REPORT_PROCESSING_TIMER = name(Dot11Table.class, "bssid-report-processing-timing");
    public static final String DOT11_CLIENTS_REPORT_PROCESSING_TIMER = name(Dot11Table.class, "clients-report-processing-timing");
    public static final String DOT11_DISCO_REPORT_PROCESSING_TIMER = name(Dot11Table.class, "disco-report-processing-timing");
    public static final String DOT11_ALERT_PROCESSING_TIMER = name(Dot11Table.class, "alert-processing-timing");

    public static final String DNS_TOTAL_REPORT_PROCESSING_TIMER = name(DNSTable.class, "total-report-processing-timing");
    public static final String DNS_STATISTICS_REPORT_PROCESSING_TIMER = name(DNSTable.class, "statistics-report-processing-timing");
    public static final String DNS_PAIRS_REPORT_PROCESSING_TIMER = name(DNSTable.class, "pairs-report-processing-timing");
    public static final String DNS_LOG_REPORT_PROCESSING_TIMER = name(DNSTable.class, "log-report-processing-timing");
    public static final String DNS_ENTROPY_REPORT_PROCESSING_TIMER = name(DNSTable.class, "entropy-report-processing-timing");

    public static final String TCP_TOTAL_REPORT_PROCESSING_TIMER = name(TCPTable.class, "total-report-processing-timing");
    public static final String TCP_SESSIONS_REPORT_PROCESSING_TIMER = name(TCPTable.class, "sessions-report-processing-timing");
    public static final String TCP_SESSION_DISCOVERY_QUERY_TIMER = name(TCPTable.class, "session-discovery-query-timing");

    public static final String SOCKS_TOTAL_REPORT_PROCESSING_TIMER = name(SocksTable.class, "total-report-processing-timing");

}

package app.nzyme.core.rest.interceptors;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TapTableSizeInterceptor implements ReaderInterceptor {

    private static final Logger LOG = LogManager.getLogger(TapTableSizeInterceptor.class);

    @Context
    private UriInfo uriInfo;

    private NzymeNode nzyme;

    public TapTableSizeInterceptor(NzymeNode nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        if (uriInfo.getPath().equals("api/taps/tables") || uriInfo.getPath().equals("api/taps/tables/")) {
            // We do not want to consume the InputStream because we need it later in the resource of course. Copy it.
            ByteArrayOutputStream cloner = new ByteArrayOutputStream();
            context.getInputStream().transferTo(cloner);

            int size = cloner.toByteArray().length;
            LOG.debug("Tap table report size: {}", size);

            nzyme.getMetrics().histogram(MetricNames.TAP_TABLE_REQUEST_SIZES).update(size);

            // Stick another clone right back into the request context.
            context.setInputStream(new ByteArrayInputStream(cloner.toByteArray()));
        }

        return context.proceed();
    }

}

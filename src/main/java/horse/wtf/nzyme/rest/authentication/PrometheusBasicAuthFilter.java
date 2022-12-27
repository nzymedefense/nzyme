package horse.wtf.nzyme.rest.authentication;

import app.nzyme.plugin.RegistryCryptoException;
import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import horse.wtf.nzyme.NzymeLeader;
import horse.wtf.nzyme.monitoring.prometheus.PrometheusRegistryKeys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@PrometheusBasicAuthSecured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class PrometheusBasicAuthFilter implements ContainerRequestFilter  {

    private static final Logger LOG = LogManager.getLogger(PrometheusBasicAuthFilter.class);

    private final NzymeLeader nzyme;

    public PrometheusBasicAuthFilter(NzymeLeader nzyme) {
        this.nzyme = nzyme;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

            // No Authorization header supplied.
            if (Strings.isNullOrEmpty(authorizationHeader) || authorizationHeader.trim().isEmpty()) {
                abortWithUnauthorized(requestContext);
                return;
            }

            boolean prometheusReportEnabled = Boolean.parseBoolean(nzyme.getDatabaseCoreRegistry()
                    .getValue(PrometheusRegistryKeys.REST_REPORT_ENABLED.key())
                    .orElse("false"));

            if (!prometheusReportEnabled) {
                abortWithNotFound(requestContext);
                return;
            }

            Optional<String> username;
            Optional<String> password;
            try {
                username = nzyme.getDatabaseCoreRegistry()
                        .getValue(PrometheusRegistryKeys.REST_REPORT_USERNAME.key());
                password = nzyme.getDatabaseCoreRegistry()
                        .getEncryptedValue(PrometheusRegistryKeys.REST_REPORT_PASSWORD.key());
                if (username.isEmpty() || password.isEmpty()) {
                    abortWithNotFound(requestContext);
                    return;
                }
            } catch(RegistryCryptoException e) {
                LOG.error("Could not decrypt registry value.", e);
                abortWithUnauthorized(requestContext);
                return;
            }

            HTTPBasicAuthParser.Credentials creds = HTTPBasicAuthParser.parse(authorizationHeader);

            // abort if no creds configured in registry

            if (creds.getUsername().equals(username.get()) && creds.getPassword().equals(password.get())) {
                // Set new security context for later use in resources.
                final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
                requestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return () -> creds.getUsername();
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return true;
                    }

                    @Override
                    public boolean isSecure() {
                        return currentSecurityContext.isSecure();
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return SecurityContext.BASIC_AUTH;
                    }

                });
            } else {
                LOG.warn("Rejected attempted authentication using invalid Prometheus basic auth.");
                abortWithUnauthorized(requestContext);
            }
        } catch (Exception e) {
            LOG.debug("Could not authenticate using Prometheus HTTP basic authentication.", e);
            abortWithUnauthorized(requestContext);
        }
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    private void abortWithNotFound(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.NOT_FOUND).build());
    }

}

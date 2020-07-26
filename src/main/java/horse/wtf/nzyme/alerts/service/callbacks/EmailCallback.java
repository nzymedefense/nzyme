/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.alerts.service.callbacks;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.typesafe.config.Config;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.configuration.ConfigurationValidator;
import horse.wtf.nzyme.configuration.IncompleteConfigurationException;
import horse.wtf.nzyme.configuration.InvalidConfigurationException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import javax.annotation.Nullable;
import javax.mail.Message;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailCallback implements AlertCallback {

    private static final Logger LOG = LogManager.getLogger(EmailCallback.class);

    private final Configuration configuration;
    private final Mailer mailer;

    private final freemarker.template.Configuration templateConfig;

    public EmailCallback(Configuration configuration) {
        this.configuration = configuration;
        this.mailer = MailerBuilder
                .withSMTPServer(configuration.host(), configuration.port(), configuration.username(), configuration.password())
                .withTransportStrategy(configuration.transportStrategy())
                .clearEmailAddressCriteria()
                .buildMailer();

        // Set up template engine.
        this.templateConfig = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_30);
        this.templateConfig.setClassForTemplateLoading(this.getClass(), "/");
        this.templateConfig.setDefaultEncoding("UTF-8");
        this.templateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.templateConfig.setLogTemplateExceptions(false);
        this.templateConfig.setWrapUncheckedExceptions(true);
        this.templateConfig.setFallbackOnNullLoopVariable(false);
    }

    @Override
    public void call(Alert alert) {
        LOG.info("Sending alert email.");
        try {
            Email email = EmailBuilder.startingBlank()
                    .to(configuration.recipients())
                    .from(configuration.from())
                    .withSubject(configuration.subjectPrefix() + " " + buildSubject(alert))
                    .withPlainText(buildPlainTextBody(alert))
                    .withHTMLText(buildHTMLTextBody(alert))
                    .withEmbeddedImage("nzyme_logo", loadLogoFile(), "image/png")
                    .buildEmail();

            mailer.sendMail(email);
        } catch(Exception e) {
            LOG.error("Could not send Email.", e);
        }
    }

    private String buildSubject(Alert alert) {
        return "Alert [" + alert.getSubsystem() + "/" + alert.getType().toString() + "]";
    }

    private String buildPlainTextBody(Alert alert) throws URISyntaxException {
        StringBuilder sb = new StringBuilder();

        sb.append("ALERT: " + alert.getMessage()).append("\n\n")
                .append(alert.getDescription()).append("\n\n")
                .append("Link: ").append(buildHTTPURI(alert)).append("\n");

        for (Map.Entry<String, Object> field : alert.getFields().entrySet()) {
            sb.append("\n").append(field.getKey()).append(": ").append(field.getValue());
        }

        return sb.toString();
    }

    @Nullable
    private String buildHTMLTextBody(Alert alert) throws IOException {
        try {
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("title", "nzyme Alert");
            parameters.put("alert_summary", alert.getMessage());
            parameters.put("alert_description", alert.getDescription());
            parameters.put("details_link", buildHTTPURI(alert));
            parameters.put("fields", alert.getFields());

            StringWriter out = new StringWriter();
            Template template = this.templateConfig.getTemplate("email/template_basic.ftl");
            template.process(parameters, out);
            return out.toString();
        } catch(Exception e) {
            LOG.error("Could not build HTML text body.", e);
            return null;
        }
    }

    private byte[] loadLogoFile() throws IOException {
        //noinspection UnstableApiUsage
        InputStream resource = getClass().getClassLoader().getResourceAsStream("email/nzyme.png");
        if (resource == null) {
            throw new RuntimeException("Couldn't load nzyme logo file.");
        }

        //noinspection UnstableApiUsage
        return resource.readAllBytes();
    }

    private URI buildHTTPURI(Alert alert) throws URISyntaxException {
        return new URIBuilder(configuration.httpExternalURI())
                .setPath("/alerts/show/" + alert.getUUID())
                .build();
    }

    private static final String WHERE = "alerting.callbacks.[email]";
    private static final String CK_TRANSPORT_STRATEGY = "transport_strategy";
    private static final String CK_HOST = "host";
    private static final String CK_PORT = "port";
    private static final String CK_USERNAME = "username";
    private static final String CK_PASSWORD = "password";
    private static final String CK_RECIPIENTS = "recipients";
    private static final String CK_FROM = "from";
    private static final String CK_SUBJECT_PREFIX = "subject_prefix";

    private static final Pattern RECIPIENT_PATTERN = Pattern.compile("^(.+)<(.+)>$");

    public static Configuration parseConfiguration(Config c, String httpExternalUri) throws InvalidConfigurationException, IncompleteConfigurationException {
        // Completeness.
        ConfigurationValidator.expect(c, CK_TRANSPORT_STRATEGY, WHERE, String.class);
        ConfigurationValidator.expect(c, CK_HOST, WHERE, String.class);
        ConfigurationValidator.expect(c, CK_PORT, WHERE, Integer.class);
        ConfigurationValidator.expect(c, CK_USERNAME, WHERE, String.class);
        ConfigurationValidator.expect(c, CK_PASSWORD, WHERE, String.class);
        ConfigurationValidator.expect(c, CK_RECIPIENTS, WHERE, List.class);
        ConfigurationValidator.expect(c, CK_FROM, WHERE, String.class);
        ConfigurationValidator.expect(c, CK_SUBJECT_PREFIX, WHERE, String.class);

        // Validity.
        // Transport strategy exists.
        TransportStrategy transportStrategy;
        try {
            transportStrategy = TransportStrategy.valueOf(c.getString(CK_TRANSPORT_STRATEGY));
        } catch(IllegalArgumentException e) {
            throw new InvalidConfigurationException("Invalid SMTP transport strategy.", e);
        }

        // Recipients are valid.
        List<Recipient> recipients = Lists.newArrayList();
        for (String rec : c.getStringList(CK_RECIPIENTS)) {
            recipients.add(parseRecipient(rec, Message.RecipientType.TO));
        }

        return Configuration.create(
                transportStrategy,
                c.getString(CK_HOST),
                c.getInt(CK_PORT),
                c.getString(CK_USERNAME),
                c.getString(CK_PASSWORD),
                recipients,
                parseRecipient(c.getString(CK_FROM), Message.RecipientType.TO), // recipient type is ignored
                c.getString(CK_SUBJECT_PREFIX),
                httpExternalUri
        );
    }

    public static Recipient parseRecipient(String s, Message.RecipientType recipientType) throws InvalidConfigurationException {
        try {
            Matcher matcher = RECIPIENT_PATTERN.matcher(s);
            if (!matcher.find()) {
                throw new InvalidConfigurationException("Invalid email address: (no match) [" + s + "] (correct format: \"Some Body <somebody@example.org>\"");
            } else {
                return new Recipient(matcher.group(1).trim(), matcher.group(2).trim(), recipientType);
            }
        } catch(Exception e){
            throw new InvalidConfigurationException("Invalid email address: [" + s + "] (correct format: \"Some Body <somebody@example.org>\"", e);
        }
    }

    @AutoValue
    public static abstract class Configuration {

        public abstract TransportStrategy transportStrategy();
        public abstract String host();
        public abstract int port();
        public abstract String username();
        public abstract String password();

        public abstract List<Recipient> recipients();
        public abstract Recipient from();
        public abstract String subjectPrefix();

        public abstract String httpExternalURI();

        public static Configuration create(TransportStrategy transportStrategy, String host, int port, String username, String password, List<Recipient> recipients, Recipient from, String subjectPrefix, String httpExternalURI) {
            return builder()
                    .transportStrategy(transportStrategy)
                    .host(host)
                    .port(port)
                    .username(username)
                    .password(password)
                    .recipients(recipients)
                    .from(from)
                    .subjectPrefix(subjectPrefix)
                    .httpExternalURI(httpExternalURI)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_EmailCallback_Configuration.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder transportStrategy(TransportStrategy transportStrategy);

            public abstract Builder host(String host);

            public abstract Builder port(int port);

            public abstract Builder username(String username);

            public abstract Builder password(String password);

            public abstract Builder recipients(List<Recipient> recipients);

            public abstract Builder from(Recipient from);

            public abstract Builder subjectPrefix(String subjectPrefix);

            public abstract Builder httpExternalURI(String httpExternalURI);

            public abstract Configuration build();
        }
    }

}

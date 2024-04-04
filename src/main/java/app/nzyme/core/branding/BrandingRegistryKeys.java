package app.nzyme.core.branding;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class BrandingRegistryKeys {

    public static final RegistryKey SIDEBAR_TITLE_TEXT = RegistryKey.create(
            "sidebar_title_text",
            Optional.of(new ArrayList<>() {{
                ConfigurationEntryConstraint.createStringLengthConstraint(1, 12);
            }}),
            Optional.of("nzyme"),
            false
    );

    public static final RegistryKey SIDEBAR_SUBTITLE_TEXT = RegistryKey.create(
            "sidebar_subtitle_text",
            Optional.of(new ArrayList<>() {{
                ConfigurationEntryConstraint.createStringLengthConstraint(1, 28);
            }}),
            Optional.empty(),
            false
    );

}

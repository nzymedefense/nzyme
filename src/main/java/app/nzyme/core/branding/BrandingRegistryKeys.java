package app.nzyme.core.branding;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Optional;

public class BrandingRegistryKeys {

    public static final RegistryKey SIDEBAR_TITLE_TEXT = RegistryKey.create(
            "sidebar_title_text",
            Optional.of(new ImmutableList.Builder().add(ConfigurationEntryConstraint.createStringLengthConstraint(1, 12)).build()),
            Optional.of("nzyme"),
            false
    );

    public static final RegistryKey SIDEBAR_SUBTITLE_TEXT = RegistryKey.create(
            "sidebar_subtitle_text",
            Optional.of(new ImmutableList.Builder().add(ConfigurationEntryConstraint.createStringLengthConstraint(1, 28)).build()),
            Optional.empty(),
            false
    );

    public static final RegistryKey LOGIN_IMAGE = RegistryKey.create(
            "login_image",
            Optional.empty(),
            Optional.empty(),
            false
    );

}

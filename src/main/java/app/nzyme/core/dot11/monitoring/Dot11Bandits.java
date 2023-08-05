package app.nzyme.core.dot11.monitoring;

import java.util.ArrayList;
import java.util.List;

public class Dot11Bandits {

    public static List<Dot11BanditDescription> BUILT_IN = new ArrayList<>() {{
       add(Dot11BanditDescription.create(
               "ESP32 Marauder",
               "A suite of WiFi/Bluetooth offensive and defensive tools for the ESP32 platform.",
               "560f2134c30d48d80fc7849e911e4057d3a4e32ab3c047f7697cf141e150d182"
       ));

        add(Dot11BanditDescription.create(
                "Flipper Zero Evil Portal",
                "A malicious access point for the Flipper Zero (with WiFi Dev Board) that spins up a " +
                        "credential stealing captive portal.",
                "379211cecb97166728b8bb89c1d0771a437e8c0d969552fc36d86b8abe40be7a"
        ));

        add(Dot11BanditDescription.create(
                "WiFi Pineapple Nano/Tetra PineAP",
                "The malicious PineAP access point of the popular WiFi Pineapple Nano or Tetra attack " +
                        "platforms.",
                "29491633b99aad0b2fd647eeee336101abee389175041b48d60e94b4b361388b"
        ));

        add(Dot11BanditDescription.create(
                "WiFi Pineapple Nano Management Access Point",
                "The management access point of the popular WiFi Pineapple Nano attack platform.",
                "b12836f7f3ec133f163d98a364b2689fe49b2eeddadff7d6b319f5ce441de8f1"
        ));

        add(Dot11BanditDescription.create(
                "WiFi Pineapple Nano Open Access Point",
                "The open access point of the popular WiFi Pineapple Nano attack platform.",
                "32d24a2b5907e67350b58480e24486cd50c8dffec6242b7fd952f52b02d9ac69"
        ));

        add(Dot11BanditDescription.create(
                "WiFi Pineapple Tetra Management Access Point",
                "The management access point of the popular WiFi Pineapple Tetra attack platform.",
                "a3f0af722f37681235673c658981e74be94024ab6178fd6e99839d198066e483"
        ));

        add(Dot11BanditDescription.create(
                "WiFi Pineapple Tetra Open Access Point",
                "The open access point of the popular WiFi Pineapple Tetra attack platform.",
                "d79c5908617b92670f73f45b4094c4b15fa0b1a71e536959e43d865ab8ed589f"
        ));

        add(Dot11BanditDescription.create(
                CUSTOM_PWNAGOTCHI_NAME,
                CUSTOM_PWNAGOTCHI_DESCRIPTION,
                null
        ));
    }};

    public static final String CUSTOM_PWNAGOTCHI_NAME = "Pwnagotchi";
    public static final String CUSTOM_PWNAGOTCHI_DESCRIPTION = "The Pwnagotchi attack platform. This detection " +
            "includes additional details about the identity of the detected Pwnagotchi.";

}

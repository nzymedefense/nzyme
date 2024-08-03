package app.nzyme.core.dot11.bandits;

import java.util.ArrayList;
import java.util.List;

public class Dot11Bandits {

    public static List<Dot11BanditDescription> BUILT_IN = new ArrayList<>() {{
       add(Dot11BanditDescription.create(
               "esp32marauder",
               false,
               "ESP32 Marauder",
               "A suite of WiFi/Bluetooth offensive and defensive tools for the ESP32 platform.",
               new ArrayList<>() {{ add("560f2134c30d48d80fc7849e911e4057d3a4e32ab3c047f7697cf141e150d182"); }}
       ));

        add(Dot11BanditDescription.create(
                "flipperzero_evilportal",
                false,
                "Flipper Zero Evil Portal",
                "A malicious access point for the Flipper Zero (with WiFi Dev Board) that spins up a " +
                        "credential stealing captive portal.",
                new ArrayList<>() {{ add("379211cecb97166728b8bb89c1d0771a437e8c0d969552fc36d86b8abe40be7a"); }}
        ));

        add(Dot11BanditDescription.create(
                "pineapple_nano_tetra_pineap",
                false,
                "WiFi Pineapple Nano/Tetra PineAP",
                "The malicious PineAP access point of the popular WiFi Pineapple Nano or Tetra attack " +
                        "platforms.",
                new ArrayList<>() {{ add("29491633b99aad0b2fd647eeee336101abee389175041b48d60e94b4b361388b"); }}
        ));

        add(Dot11BanditDescription.create(
                "pineapple_nano_mgmt",
                false,
                "WiFi Pineapple Nano Management Access Point",
                "The management access point of the popular WiFi Pineapple Nano attack platform.",
                new ArrayList<>() {{ add("b12836f7f3ec133f163d98a364b2689fe49b2eeddadff7d6b319f5ce441de8f1"); }}
        ));

        add(Dot11BanditDescription.create(
                "pineapple_nano_ap",
                false,
                "WiFi Pineapple Nano Open Access Point",
                "The open access point of the popular WiFi Pineapple Nano attack platform.",
                new ArrayList<>() {{ add("32d24a2b5907e67350b58480e24486cd50c8dffec6242b7fd952f52b02d9ac69"); }}
        ));

        add(Dot11BanditDescription.create(
                "pineapple_tetra_mgmt",
                false,
                "WiFi Pineapple Tetra Management Access Point",
                "The management access point of the popular WiFi Pineapple Tetra attack platform.",
                new ArrayList<>() {{ add("a3f0af722f37681235673c658981e74be94024ab6178fd6e99839d198066e483"); }}
        ));

        add(Dot11BanditDescription.create(
                "pineapple_tetra_ap",
                false,
                "WiFi Pineapple Tetra Open Access Point",
                "The open access point of the popular WiFi Pineapple Tetra attack platform.",
                new ArrayList<>() {{ add("d79c5908617b92670f73f45b4094c4b15fa0b1a71e536959e43d865ab8ed589f"); }}
        ));


        add(Dot11BanditDescription.create(
                "pineapple_markvii_mgmt_pineap",
                false,
                "WiFi Pineapple Mark VII Management Access Point or PineAP",
                "The management access point of the popular WiFi Pineapple Mark VII attack platform. " +
                        "Alternatively, this could be a specific configuration of the Pineapple PineAP.",
                new ArrayList<>() {{ add("23b57e5f4d7d01dba7bfc5b098cf64ba51eeb95740b97c7d56359fb9a328a8ed"); }}
        ));

        add(Dot11BanditDescription.create(
                "pineapple_markvii_ap",
                false,
                "WiFi Pineapple Mark VII Open Access Point",
                "The open access point of the popular WiFi Pineapple Mark VII attack platform.",
                new ArrayList<>() {{ add("ec8eaabbdbb6c2cc43b432f66b903d5a60073b107df6715d7dc9cf846385a368"); }}
        ));

        add(Dot11BanditDescription.create(
                "pineapple_markvii_impap",
                false,
                "WiFi Pineapple Mark VII Impersonation Access Point",
                "The impersonation access point of the popular WiFi Pineapple Mark VII attack platform.",
                new ArrayList<>() {{ add("7c9fa136d4413fa6173637e883b6998d32e1d675f88cddff9dcbcf331820f4b8"); }}
        ));

        add(Dot11BanditDescription.create(
                "pineapple_markvii_evilwpa",
                false,
                "WiFi Pineapple Mark VII Evil WPA Access Point",
                "The \"Evil WPA\" access point of the popular WiFi Pineapple Mark VII attack platform.",
                new ArrayList<>() {{
                    add("23b57e5f4d7d01dba7bfc5b098cf64ba51eeb95740b97c7d56359fb9a328a8ed");
                    add("eb725a5c7a80765d88bd11ff8bdf477951cbbc466f4aceb7b30256d537fb1a4d");
                    add("3ce29bfe2fa4d2f46e7dce8a59cd2e8aaf99f0fa4db901657bb39b71c07b4aed");
                    add("1566e87e0211da5f19a9488128efacfef39f262b334f5c6ae1d9fbe7b33708af");
                    add("6aeb39a36bb0fcf7683b7be317fea06950d8087bcd32b55063694a543856e685");
                    add("0816bacbc26fe3b8808191c3e51812b51b8026710c961d98a353ae004b34b40e");
                    add("c9fa351aece9c78cc42ca615301df5077c0f3a174fbc040f5fe9143fc231ae2d");
                    add("73e0cc087977f6d5c5a9612d6ad5beab6d9a2da0ca6a4e070a2667c42d914e9f");
                    add("850ff3cf6ee3c40e3197508852fa5c7e64254b22cecee6a5a207ea2fb7756ed5");
                }}
        ));

        add(Dot11BanditDescription.create(
                "pineapple_markvii_evileap",
                false,
                "WiFi Pineapple Mark VII Evil Enterprise Access Point",
                "The \"Evil Enterprise\" access point of the popular WiFi Pineapple Mark VII attack platform.",
                new ArrayList<>() {{
                    add("8b0fd4de88d226b186c26bab595b98b79b4db3f330eab081fcdba4fda6fe7a46");
                    add("cf2456fd58965c5fd80bbc295df2da778034a533b1519ebd2cc3ded0d174ff29");
                    add("e95c93c785807903d4ee35167423c59615f3a4fab47a130a668121c8bd81a852");
                    add("ff87291ccd489781625e0204588e65950b54f64de791684951d76659aa469c0e");
                    add("44201633ad1f31856c58aa01bc77624d6ea8ae670bd9fc4396146fd800c9fb3a");
                    add("a8d80613e2d3fa17dcb1598c877839fbc54ac4b4cbb1b2441bffe9a0b7dd5ae1");
                    add("47b979bb7de268adfd235bc0bcf758af0bc05c291ea3ba14e0e1937b5ac5c2f4");
                }}
        ));

        add(Dot11BanditDescription.create(
                "omg_cable_plug",
                false,
                "O.MG Cable or Plug",
                "The access point of the popular \"O.MG\" cable or plug USB implant.",
                new ArrayList<>() {{
                    add("4b58f00646b7ab9bf84ac14640784ddfde70a269d2b1a068bbc06c08c01460de");
                }}
        ));

        add(Dot11BanditDescription.create(
                CUSTOM_PWNAGTCHI_ID,
                false,
                CUSTOM_PWNAGOTCHI_NAME,
                CUSTOM_PWNAGOTCHI_DESCRIPTION,
                null
        ));
    }};

    public static final String CUSTOM_PWNAGTCHI_ID = "pwnagotchi";
    public static final String CUSTOM_PWNAGOTCHI_NAME = "Pwnagotchi";
    public static final String CUSTOM_PWNAGOTCHI_DESCRIPTION = "The Pwnagotchi attack platform. This detection " +
            "includes additional details about the identity of the detected Pwnagotchi. Note that this detection is " +
            "not based on fingerprints, but frame attributes.";

}

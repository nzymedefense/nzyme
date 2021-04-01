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

package horse.wtf.nzyme.bandits;

import horse.wtf.nzyme.bandits.engine.ContactManager;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.FingerprintBanditIdentifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DefaultBandits {

    private static final Logger LOG = LogManager.getLogger(DefaultBandits.class);

    /*
     * Using this instead of Liquibase migrations because we might need to get fancy (as in, using code) in the future
     * when it comes to maintaining these.
     *
     * It is important to keep the UUIDs static and never change them, so we can always refer back to a specific entry.
     *
     * This will probably bite me later but appears to be the best approach for now.
     *
     */

    public static final List<Bandit> BANDITS = new ArrayList<Bandit>(){{
        add(Bandit.create(null, UUID.fromString("d754ab90-67f9-43bc-b5df-bd815112e55b"),
                "WiFi Pineapple Nano, Tetra or Mark VII (PineAP), esp8266_deauther",
                "[Built-in bandit definition]\n\nDetects WiFi Pineapple PineAP frames and esp8266_deauther frames, " +
                        "which appear to be cloned from Pineapple frames because they have the same fingerprint.\n\n" +
                        "Pineapple Nano v2.0.2, v2.5.2, Mark VII v1.0.2 and Tetra v1.1.2, v2.5.2, v2.7.0 but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "ec398735dc99267d453908d81bfe06ce04cfa2573d0b9edf1d940f0dbf850a9c",
                            null,
                            UUID.fromString("150d5828-b7a6-4796-add3-3266da396853")));
                }})
        );


        add(Bandit.create(null, UUID.fromString("b21855a1-ddd2-4a27-911a-1efc046d9334"),
                "WiFi Pineapple Tetra (PineAP)",
                "[Built-in bandit definition] Pineapple Tetra v2.5.2, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "535afea1f1656375a991e28ce919d412fd9863a01f1b0b94fcff8a83ed8fcb83",
                            null,
                            UUID.fromString("45c51f60-14d7-4b3f-a64e-353bfbe22163")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("94abd8f9-8f75-4098-a2d7-78714639a47f"),
                "WiFi Pineapple Nano (management access point)",
                "[Built-in bandit definition] Pineapple Nano v2.5.2, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "e1a3923e4a513e2e1040763ad0b97746a84add27d559a84e4af3b313c69bfb26",
                            null,
                            UUID.fromString("60d45ecb-4d81-46e3-b4e4-3ee35007bd59")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("6ea575a5-8b73-4bcb-b0d2-2fd4861e2b9e"),
                "WiFi Pineapple Nano (management access point)",
                "[Built-in bandit definition] Pineapple Nano v2.5.2, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "af59f355d6885a77c85324147e2f29c48b170f5ebad107beafbadc48d1dc491f",
                            null,
                            UUID.fromString("b31e8643-a7f6-4e70-9077-b10d4e9d00fb")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("26452b4f-1865-4335-93ea-875729ca33d8"),
                "WiFi Pineapple Nano (public access point)",
                "[Built-in bandit definition] Pineapple Nano v2.5.2, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "147a503d849b148738bf66dcb7aea39c0c08f54cbd5edd47e39efe47d6fd582e",
                            null,
                            UUID.fromString("9a2e0050-d5d3-4b47-a283-dfb8b9130457")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("ab81639a-3b35-4756-b341-18567cfd3310"),
                "WiFi Pineapple Tetra (management access point)",
                "[Built-in bandit definition] Pineapple Tetra v1.1.2, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "dacf284b8a079fc61c795a2441672baff055890f106b3f75621ab1e00c518273",
                            null,
                            UUID.fromString("014657d5-d7af-436e-9210-fdcab1a18094")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("2d3624ea-8512-4a17-bf5b-4fc8b17b7f20"),
                "WiFi Pineapple Tetra (management access point)",
                "[Built-in bandit definition] Pineapple Tetra v2.5.2, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "78ad585d15e4372299c6da1175c6e126d00face9452ea2741cae240fb1d6d6f2",
                            null,
                            UUID.fromString("34aca876-f937-4f48-a3b9-8e0e2dcc9ea0")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("12faf7b6-e7ec-4978-abee-9dbf18f88d4d"),
                "WiFi Pineapple Tetra (public access point)",
                "[Built-in bandit definition] Pineapple Tetra v1.1.2, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "32f5dc405a16936a40a23153e91cad67cbe813f45188d1b36e58e8405b9adaef",
                            null,
                            UUID.fromString("790490b9-63c4-4ee4-9726-b843647a0386")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("5451ae49-f4b1-4e54-9e59-f2b14ed96a36"),
                "WiFi Pineapple Tetra (public access point)",
                "[Built-in bandit definition] Pineapple Tetra v2.5.2, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "7664c29afd9f6b83915235013a3ce628a13e9e6eba9530fe42c466b987270676",
                            null,
                            UUID.fromString("d927fa9d-3d50-4379-b04d-fe526c004361")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("4f9394b9-358f-4454-887a-5d5655dc76f1"),
                "WiFi Pineapple Tetra (public access point)",
                "[Built-in bandit definition] Pineapple Tetra v2.5.2, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "99255ad871a842cfcf972b069728501ea31583b56e7692cc6abe3334c2846528",
                            null,
                            UUID.fromString("38a77411-73bb-4d7a-b12e-6cba0ac1f361")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("47caa181-2afa-4e2c-8094-1a3d1fe5400c"),
                "WiFi Pineapple Tetra (public access point)",
                "[Built-in bandit definition] Pineapple Tetra v2.7.0, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "e6679e0fb62c0efd80f1e39c1cbb7f239edc0d7f601fbb9a22d14f2eb31c0266",
                            null,
                            UUID.fromString("56105c5a-3011-423a-b785-8175833fa1d1")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("54bf66f1-37cf-4f96-93ed-449791458a80"),
                "WiFi Pineapple Tetra (management access point)",
                "[Built-in bandit definition] Pineapple Tetra v2.5.2, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "e643cd336d483cdfb7e3c0912e262dd21e6bbbc67a72bccd47214f67373d8ab4",
                            null,
                            UUID.fromString("bca7bbe2-23cf-4c15-86a9-7a4bd956dc77")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("21fefae6-aecf-41b7-b086-e6cd13dcb220"),
                "WiFi Pineapple Tetra (management access point)",
                "[Built-in bandit definition] Pineapple Tetra v2.7.0, but other firmware versions might match, too.",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "8265dd9864d2b9a35c2742d8e9180db7c520169ab4e06977ae7651f3a574331a",
                            null,
                            UUID.fromString("37791b52-13cc-41dd-948b-c3fd62f4a171")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("4918b920-d933-4f81-a7ec-24a38c59fb63"),
                "spacehuhn/esp8266_deauther (management access point)",
                "[Built-in bandit definition]",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "29007c66ed8091c2c8d6060915da22560bc56b81be40085a04515be87dfe538a",
                            null,
                            UUID.fromString("fcf82215-0169-46ff-b432-71ec78cb59fc")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("b1be7c10-071d-4406-a30f-3052b2cb1c5d"),
                "wifiphisher",
                "[Built-in bandit definition]",
                true,
                new DateTime("2020-03-29T00:00:00+0000"),
                new DateTime("2020-03-29T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "4d2c7aeb85869ef12a92d39754ebdbfb101bebf4224cc055ee89b96c9f41ee3b",
                            null,
                            UUID.fromString("16e0f703-49c4-4ee2-b757-1889c358e707")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("edb33ea4-829b-4f46-827d-c75a728fc0a0"),
                "WiFi Pineapple Mark VII (management access point)",
                "[Built-in bandit definition] Pineapple Mark VII v1.0.2, but other firmware versions might match, too.",
                true,
                new DateTime("2021-03-31T00:00:00+0000"),
                new DateTime("2021-03-31T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "52e13d95488261db15fa486a107a5ee5dbf14affa652e928b31dca4c245be6e6",
                            null,
                            UUID.fromString("119a972f-b9f7-49d4-ba22-9647dd241148")));
                }})
        );

        add(Bandit.create(null, UUID.fromString("9f78318b-7f3c-45cb-9dd6-6176759ac031"),
                "WiFi Pineapple Mark VII (public access point)",
                "[Built-in bandit definition] Pineapple Mark VII v1.0.2, but other firmware versions might match, too.",
                true,
                new DateTime("2021-03-31T00:00:00+0000"),
                new DateTime("2021-03-31T00:00:00+0000"),
                new ArrayList<BanditIdentifier>(){{
                    add(new FingerprintBanditIdentifier(
                            "609406d11b6d0398a830142b9ae5c24f59640cf3f02887fe2dc351e056846bb4",
                            null,
                            UUID.fromString("59d54520-cd1d-4f76-826e-d914bb76fe8c")));
                }})
        );
    }};

    public static void seed(ContactManager contactIdentifier) {
        contactIdentifier.removeAllReadOnlyBandits();
        for (Bandit bandit : BANDITS) {
            contactIdentifier.registerBandit(bandit);
        }


    }

}

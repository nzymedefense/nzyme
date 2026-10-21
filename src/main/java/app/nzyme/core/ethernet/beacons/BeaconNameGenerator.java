package app.nzyme.core.ethernet.beacons;

import java.security.SecureRandom;
import java.util.Random;

// TODO probably want to make this more generic, for other parts of the system
public class BeaconNameGenerator {

    private static final String[] ADJECTIVES = {
            "amber", "ancient", "bold", "brave", "bright", "brisk", "calm", "clever", "cobalt", "cosmic",
            "crimson", "curious", "dark", "dusty", "eager", "early", "fading", "faint", "fancy", "fierce",
            "frosty", "gentle", "gilded", "golden", "grand", "hazy", "hidden", "hollow", "humble", "icy",
            "idle", "ivory", "jade", "jolly", "keen", "lazy", "little", "lively", "lonely", "loud",
            "lucky", "lunar", "mellow", "misty", "muted", "nimble", "noble", "odd", "olive", "pale",
            "patient", "plain", "polar", "proud", "quick", "quiet", "rapid", "rare", "restless", "rusty",
            "scarlet", "shady", "sharp", "silent", "silver", "sleepy", "slow", "smoky", "snowy", "solar",
            "steady", "stormy", "subtle", "swift", "tidy", "tiny", "vague", "velvet", "vivid", "wandering",
            "wary", "weary", "wild", "witty", "young", "zesty"
    };

    private static final String[] NOUNS = {
            "anchor", "arrow", "badger", "basalt", "bison", "boulder", "canyon", "cedar", "comet", "condor",
            "coral", "crane", "crow", "cypress", "delta", "dune", "eagle", "ember", "falcon", "fern",
            "finch", "fjord", "fox", "glacier", "harbor", "hawk", "heron", "ibis", "iris", "island",
            "jaguar", "kestrel", "lagoon", "lantern", "lark", "lemur", "lynx", "magpie", "maple", "marten",
            "meadow", "meteor", "mole", "moose", "moth", "nebula", "newt", "oak", "ocelot", "orbit",
            "osprey", "otter", "owl", "panther", "pebble", "pelican", "pine", "plover", "prairie", "puffin",
            "quail", "quartz", "raven", "reef", "ridge", "river", "robin", "saber", "sparrow", "spruce",
            "stork", "summit", "swan", "thistle", "tiger", "tundra", "viper", "walrus", "wasp", "willow",
            "wolf", "wren", "yak", "zephyr"
    };

    private final Random random;

    public BeaconNameGenerator() {
        this(new SecureRandom());
    }

    public BeaconNameGenerator(Random random) {
        this.random = random;
    }

    public String generate() {
        return ADJECTIVES[random.nextInt(ADJECTIVES.length)] + "-"
                + NOUNS[random.nextInt(NOUNS.length)] + "-"
                + String.format("%02d", random.nextInt(100));
    }

}

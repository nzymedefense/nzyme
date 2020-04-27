package horse.wtf.nzyme.bandits;

import com.google.common.collect.Lists;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.FingerprintBanditIdentifier;
import horse.wtf.nzyme.bandits.identifiers.SSIDIBanditdentifier;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.*;

public class BanditHashCalculatorTest {

    @Test
    public void testCalculateEmptyBandits() {
        assertEquals(BanditHashCalculator.calculate(Lists.newArrayList()), "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    public void testOrderingWorks() {
        DateTime bandit1Date = DateTime.now();
        DateTime bandit2Date = DateTime.now().minusHours(1);

        UUID bandit1UUID = UUID.randomUUID();
        UUID bandit2UUID = UUID.randomUUID();

        UUID bandit1Identifier1UUID = UUID.randomUUID();
        UUID bandit1Identifier2UUID = UUID.randomUUID();
        UUID bandit2Identifier1UUID = UUID.randomUUID();

        List<Bandit> bandits1 = new ArrayList<Bandit>(){{
            add(Bandit.create(1L, bandit1UUID, "bandit 1", "bandit number 1", false, bandit1Date, bandit1Date, new ArrayList<BanditIdentifier>(){{
                add(new FingerprintBanditIdentifier("60f066d0d495149ea728035ce1c4c4de9135f3801f1c8bbc830c473f6bd287a6", 1L, bandit1Identifier1UUID));
                add(new SSIDIBanditdentifier(new ArrayList<String>(){{add("foo");}}, 1L, bandit1Identifier2UUID));
            }}));

            add(Bandit.create(2L, bandit2UUID, "bandit 2", "bandit number 2", false, bandit2Date, bandit2Date, new ArrayList<BanditIdentifier>(){{
                add(new FingerprintBanditIdentifier("ef3dd2bdf26d41ab9c8cb04329b143ae1f75195bc0ddf11740efef89d0b104eb", 1L, bandit2Identifier1UUID));
            }}));
        }};

        // shuffle shuffle shuffle

        List<Bandit> bandits2 = new ArrayList<Bandit>(){{
            add(Bandit.create(2L, bandit2UUID, "bandit 2", "bandit number 2", false, bandit2Date, bandit2Date, new ArrayList<BanditIdentifier>(){{
                add(new FingerprintBanditIdentifier("ef3dd2bdf26d41ab9c8cb04329b143ae1f75195bc0ddf11740efef89d0b104eb", 1L, bandit2Identifier1UUID));
            }}));

            add(Bandit.create(1L, bandit1UUID, "bandit 1", "bandit number 1", false, bandit1Date, bandit1Date, new ArrayList<BanditIdentifier>(){{
                add(new SSIDIBanditdentifier(new ArrayList<String>(){{add("foo");}}, 1L, bandit1Identifier2UUID));
                add(new FingerprintBanditIdentifier("60f066d0d495149ea728035ce1c4c4de9135f3801f1c8bbc830c473f6bd287a6", 1L, bandit1Identifier1UUID));
            }}));
        }};

        assertEquals(BanditHashCalculator.calculate(bandits1), BanditHashCalculator.calculate(bandits2));
    }
}
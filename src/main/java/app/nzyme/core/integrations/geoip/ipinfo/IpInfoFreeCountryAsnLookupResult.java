package app.nzyme.core.integrations.geoip.ipinfo;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;

public class IpInfoFreeCountryAsnLookupResult {

    private final String countryCode;
    private final String countryName;
    private final String asName;
    private final String asNumber;
    private final String asDomain;

    @MaxMindDbConstructor
    public IpInfoFreeCountryAsnLookupResult(@MaxMindDbParameter(name="country") String countryCode,
                                            @MaxMindDbParameter(name="country_name") String countryName,
                                            @MaxMindDbParameter(name="as_name") String asName,
                                            @MaxMindDbParameter(name="asn") String asNumber,
                                            @MaxMindDbParameter(name="as_domain") String asDomain) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.asName = asName;
        this.asNumber = asNumber;
        this.asDomain = asDomain;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getAsName() {
        return asName;
    }

    public String getAsNumber() {
        return asNumber;
    }

    public String getAsDomain() {
        return asDomain;
    }

}

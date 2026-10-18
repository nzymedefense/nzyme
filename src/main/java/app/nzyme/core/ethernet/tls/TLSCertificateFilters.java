package app.nzyme.core.ethernet.tls;

import app.nzyme.core.util.filters.FilterOperator;
import app.nzyme.core.util.filters.GeneratedSql;
import app.nzyme.core.util.filters.SqlFilterProvider;

import static app.nzyme.core.util.filters.FilterSql.*;

public class TLSCertificateFilters implements SqlFilterProvider {

    @Override
    public GeneratedSql buildSql(String bindId, String fieldName, FilterOperator operator) {
        switch (fieldName) {
            case "fingerprint_sha256":
                return GeneratedSql.create(stringMatch(bindId, "encode(c.sha256, 'hex')", operator), "");
            case "fingerprint_sha1":
                return GeneratedSql.create(stringMatch(bindId, "encode(c.sha1, 'hex')", operator), "");
            case "subject":
                return GeneratedSql.create(stringMatch(bindId, "c.subject", operator), "");
            case "issuer":
                return GeneratedSql.create(stringMatch(bindId, "c.issuer", operator), "");
            case "common_name":
                return GeneratedSql.create(stringMatch(bindId, "c.common_name", operator), "");
            case "subject_alternative_names":
                return GeneratedSql.create(stringMatch(bindId, "c.san_text", operator), "");
            case "is_ca":
                return GeneratedSql.create(booleanMatch(bindId, "c.is_ca", operator), "");
            case "self_signed":
                return GeneratedSql.create(booleanMatch(bindId, "c.self_signed", operator), "");
            case "expired":
                return GeneratedSql.create(booleanMatch(bindId, "(c.not_after < NOW())", operator), "");
            case "public_key_algorithm":
                return GeneratedSql.create(stringMatch(bindId, "c.public_key_algorithm", operator), "");
            case "public_key_bits":
                return GeneratedSql.create(numericMatch(bindId, "c.public_key_bits", operator), "");
            case "signature_algorithm":
                return GeneratedSql.create(stringMatch(bindId, "c.signature_algorithm", operator), "");
            case "session_count":
                return GeneratedSql.create("", numericMatch(bindId, "COUNT(DISTINCT refs.reference_key)", operator));
            case "source":
                return GeneratedSql.create(stringMatch(bindId, "refs.source", operator), "");
            default:
                throw new RuntimeException("Unknown field name [" + fieldName + "].");
        }
    }

}

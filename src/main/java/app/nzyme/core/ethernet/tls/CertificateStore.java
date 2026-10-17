package app.nzyme.core.ethernet.tls;

import org.jdbi.v3.core.Handle;
import org.joda.time.DateTime;

import java.sql.Types;

public class CertificateStore {

    public static final String UPSERT = "INSERT INTO certificates(sha256, sha1, der, first_seen, last_seen, created_at, " +
            "parsed_at, parse_error, version, serial, subject, issuer, common_name, subject_alternative_names, " +
            "san_text, not_before, not_after, is_ca, self_signed, public_key_algorithm, public_key_bits, " +
            "signature_algorithm, extended_key_usage, subject_key_identifier, authority_key_identifier) " +
            "VALUES(:sha256, :sha1, :der, :seen_at, :seen_at, NOW(), NOW(), :parse_error, :version, :serial, " +
            ":subject, :issuer, :common_name, :subject_alternative_names, :san_text, :not_before, :not_after, " +
            ":is_ca, :self_signed, :public_key_algorithm, :public_key_bits, :signature_algorithm, " +
            ":extended_key_usage, :subject_key_identifier, :authority_key_identifier) " +
            // Rows written before the parsed columns existed get them filled when seen again.
            "ON CONFLICT (sha256) DO UPDATE SET last_seen = GREATEST(certificates.last_seen, EXCLUDED.last_seen), " +
            "sha1 = COALESCE(certificates.sha1, EXCLUDED.sha1), " +
            "parsed_at = COALESCE(certificates.parsed_at, EXCLUDED.parsed_at), " +
            "parse_error = COALESCE(certificates.parse_error, EXCLUDED.parse_error), " +
            "version = COALESCE(certificates.version, EXCLUDED.version), " +
            "serial = COALESCE(certificates.serial, EXCLUDED.serial), " +
            "subject = COALESCE(certificates.subject, EXCLUDED.subject), " +
            "issuer = COALESCE(certificates.issuer, EXCLUDED.issuer), " +
            "common_name = COALESCE(certificates.common_name, EXCLUDED.common_name), " +
            "subject_alternative_names = COALESCE(certificates.subject_alternative_names, EXCLUDED.subject_alternative_names), " +
            "san_text = COALESCE(certificates.san_text, EXCLUDED.san_text), " +
            "not_before = COALESCE(certificates.not_before, EXCLUDED.not_before), " +
            "not_after = COALESCE(certificates.not_after, EXCLUDED.not_after), " +
            "is_ca = COALESCE(certificates.is_ca, EXCLUDED.is_ca), " +
            "self_signed = COALESCE(certificates.self_signed, EXCLUDED.self_signed), " +
            "public_key_algorithm = COALESCE(certificates.public_key_algorithm, EXCLUDED.public_key_algorithm), " +
            "public_key_bits = COALESCE(certificates.public_key_bits, EXCLUDED.public_key_bits), " +
            "signature_algorithm = COALESCE(certificates.signature_algorithm, EXCLUDED.signature_algorithm), " +
            "extended_key_usage = COALESCE(certificates.extended_key_usage, EXCLUDED.extended_key_usage), " +
            "subject_key_identifier = COALESCE(certificates.subject_key_identifier, EXCLUDED.subject_key_identifier), " +
            "authority_key_identifier = COALESCE(certificates.authority_key_identifier, EXCLUDED.authority_key_identifier)";

    public static CertificateDetails upsert(Handle handle, byte[] der, DateTime seenAt) {
        CertificateDetails details = CertificateParser.parse(der);

        bindDetails(handle.createUpdate(UPSERT), details)
                .bind("der", der)
                .bind("seen_at", seenAt)
                .execute();

        return details;
    }

    private static org.jdbi.v3.core.statement.Update bindDetails(org.jdbi.v3.core.statement.Update update,
                                                                 CertificateDetails d) {
        return update
                .bind("sha256", d.sha256())
                .bind("sha1", d.sha1())
                .bind("parse_error", d.parseError())
                .bind("version", d.version())
                .bind("serial", d.serial())
                .bind("subject", d.subject())
                .bind("issuer", d.issuer())
                .bind("common_name", d.commonName())
                .bindBySqlType("subject_alternative_names", d.subjectAlternativeNames().toArray(new String[0]), Types.ARRAY)
                .bind("san_text", d.subjectAlternativeNames().isEmpty() ? null : String.join(" ", d.subjectAlternativeNames()))
                .bind("not_before", d.notBefore())
                .bind("not_after", d.notAfter())
                .bind("is_ca", d.isCa())
                .bind("self_signed", d.selfSigned())
                .bind("public_key_algorithm", d.publicKeyAlgorithm())
                .bind("public_key_bits", d.publicKeyBits())
                .bind("signature_algorithm", d.signatureAlgorithm())
                .bindBySqlType("extended_key_usage", d.extendedKeyUsage().toArray(new String[0]), Types.ARRAY)
                .bind("subject_key_identifier", d.subjectKeyIdentifier())
                .bind("authority_key_identifier", d.authorityKeyIdentifier());
    }

}

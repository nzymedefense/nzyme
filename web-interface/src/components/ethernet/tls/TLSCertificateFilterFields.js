import {FILTER_TYPE} from "../../shared/filtering/Filters";

export const TLS_CERTIFICATE_FILTER_FIELDS = {
  fingerprint_sha256: { title: "Fingerprint (SHA-256)", type: FILTER_TYPE.STRING },
  fingerprint_sha1: { title: "Fingerprint (SHA-1)", type: FILTER_TYPE.STRING },
  common_name: { title: "Common Name", type: FILTER_TYPE.STRING },
  subject: { title: "Subject", type: FILTER_TYPE.STRING },
  issuer: { title: "Issuer", type: FILTER_TYPE.STRING },
  subject_alternative_names: { title: "Subject Alternative Names", type: FILTER_TYPE.STRING },
  is_ca: { title: "Is CA", type: FILTER_TYPE.BOOLEAN },
  self_signed: { title: "Self-Signed", type: FILTER_TYPE.BOOLEAN },
  expired: { title: "Expired", type: FILTER_TYPE.BOOLEAN },
  public_key_algorithm: { title: "Public Key Algorithm", type: FILTER_TYPE.PUBLIC_KEY_ALGORITHM },
  public_key_bits: { title: "Public Key Bits", type: FILTER_TYPE.NUMERIC },
  signature_algorithm: { title: "Signature Algorithm", type: FILTER_TYPE.SIGNATURE_ALGORITHM },
  session_count: { title: "Session Count", type: FILTER_TYPE.NUMERIC },
  source: { title: "Source", type: FILTER_TYPE.CERTIFICATE_SOURCE }
}

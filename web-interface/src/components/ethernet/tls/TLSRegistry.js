/*
 * IANA TLS parameter registries, the values seen in practice. Anything unknown shows as its number.
 * GREASE values are filtered by the tap and never reach the node.
 */

const EXTENSIONS = {
  0: "server_name",
  1: "max_fragment_length",
  5: "status_request",
  10: "supported_groups",
  11: "ec_point_formats",
  13: "signature_algorithms",
  14: "use_srtp",
  15: "heartbeat",
  16: "application_layer_protocol_negotiation",
  17: "status_request_v2",
  18: "signed_certificate_timestamp",
  19: "client_certificate_type",
  20: "server_certificate_type",
  21: "padding",
  22: "encrypt_then_mac",
  23: "extended_master_secret",
  24: "token_binding",
  27: "compress_certificate",
  28: "record_size_limit",
  34: "delegated_credential",
  35: "session_ticket",
  41: "pre_shared_key",
  42: "early_data",
  43: "supported_versions",
  44: "cookie",
  45: "psk_key_exchange_modes",
  47: "certificate_authorities",
  48: "oid_filters",
  49: "post_handshake_auth",
  50: "signature_algorithms_cert",
  51: "key_share",
  13172: "next_protocol_negotiation",
  17513: "application_settings",
  65037: "encrypted_client_hello",
  65281: "renegotiation_info"
}

const GROUPS = {
  1: "sect163k1",
  2: "sect163r1",
  3: "sect163r2",
  19: "secp192r1",
  21: "secp224r1",
  23: "secp256r1",
  24: "secp384r1",
  25: "secp521r1",
  26: "brainpoolP256r1",
  27: "brainpoolP384r1",
  28: "brainpoolP512r1",
  29: "x25519",
  30: "x448",
  256: "ffdhe2048",
  257: "ffdhe3072",
  258: "ffdhe4096",
  259: "ffdhe6144",
  260: "ffdhe8192",
  512: "MLKEM512",
  513: "MLKEM768",
  514: "MLKEM1024",
  4587: "SecP256r1MLKEM768",
  4588: "X25519MLKEM768",
  4589: "SecP384r1MLKEM1024",
  25497: "X25519Kyber768Draft00"
}

const SIGNATURE_ALGORITHMS = {
  0x0201: "rsa_pkcs1_sha1",
  0x0202: "dsa_sha1",
  0x0203: "ecdsa_sha1",
  0x0301: "rsa_pkcs1_sha224",
  0x0302: "dsa_sha224",
  0x0303: "ecdsa_sha224",
  0x0401: "rsa_pkcs1_sha256",
  0x0402: "dsa_sha256",
  0x0403: "ecdsa_secp256r1_sha256",
  0x0501: "rsa_pkcs1_sha384",
  0x0502: "dsa_sha384",
  0x0503: "ecdsa_secp384r1_sha384",
  0x0601: "rsa_pkcs1_sha512",
  0x0602: "dsa_sha512",
  0x0603: "ecdsa_secp521r1_sha512",
  0x0804: "rsa_pss_rsae_sha256",
  0x0805: "rsa_pss_rsae_sha384",
  0x0806: "rsa_pss_rsae_sha512",
  0x0807: "ed25519",
  0x0808: "ed448",
  0x0809: "rsa_pss_pss_sha256",
  0x080A: "rsa_pss_pss_sha384",
  0x080B: "rsa_pss_pss_sha512",
  0x081A: "ecdsa_brainpoolP256r1tls13_sha256",
  0x081B: "ecdsa_brainpoolP384r1tls13_sha384",
  0x081C: "ecdsa_brainpoolP512r1tls13_sha512"
}

// The algorithms a modern client should not offer anymore. Used to highlight weak offers.
const WEAK_SIGNATURE_ALGORITHMS = [0x0201, 0x0202, 0x0203, 0x0301, 0x0302, 0x0303, 0x0402, 0x0502, 0x0602];
const WEAK_GROUPS = [1, 2, 3, 19, 21];

const EC_POINT_FORMATS = {
  0: "uncompressed",
  1: "ansiX962_compressed_prime",
  2: "ansiX962_compressed_char2"
}

const ALERT_LEVELS = {
  1: "Warning",
  2: "Fatal"
}

function named(registry, value) {
  if (value === null || value === undefined) {
    return null;
  }

  return registry[value] ? registry[value] : "unknown (" + value + ")";
}

export function extensionName(value) {
  return named(EXTENSIONS, value);
}

export function groupName(value) {
  return named(GROUPS, value);
}

export function signatureAlgorithmName(value) {
  const name = SIGNATURE_ALGORITHMS[value];
  return name ? name : "unknown (0x" + value.toString(16).toUpperCase().padStart(4, "0") + ")";
}

export function ecPointFormatName(value) {
  return named(EC_POINT_FORMATS, value);
}

export function alertLevelName(value) {
  return ALERT_LEVELS[value] ? ALERT_LEVELS[value] : "Level " + value;
}

export function isWeakSignatureAlgorithm(value) {
  return WEAK_SIGNATURE_ALGORITHMS.includes(value);
}

export function isWeakGroup(value) {
  return WEAK_GROUPS.includes(value);
}

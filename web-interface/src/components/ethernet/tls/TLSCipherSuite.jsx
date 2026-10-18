import React from "react";
import FullCopy from "../../shared/FullCopy";

// IANA TLS cipher suite registry, the suites seen in practice. Anything else shows as its hex number.
const CIPHER_SUITES = {
  0x1301: "TLS_AES_128_GCM_SHA256",
  0x1302: "TLS_AES_256_GCM_SHA384",
  0x1303: "TLS_CHACHA20_POLY1305_SHA256",
  0x1304: "TLS_AES_128_CCM_SHA256",
  0x1305: "TLS_AES_128_CCM_8_SHA256",
  0xC02B: "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
  0xC02C: "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
  0xC02F: "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
  0xC030: "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
  0xCCA8: "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
  0xCCA9: "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
  0xC023: "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
  0xC024: "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
  0xC027: "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
  0xC028: "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
  0xC009: "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
  0xC00A: "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
  0xC013: "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
  0xC014: "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
  0x009C: "TLS_RSA_WITH_AES_128_GCM_SHA256",
  0x009D: "TLS_RSA_WITH_AES_256_GCM_SHA384",
  0x009E: "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
  0x009F: "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
  0x003C: "TLS_RSA_WITH_AES_128_CBC_SHA256",
  0x003D: "TLS_RSA_WITH_AES_256_CBC_SHA256",
  0x002F: "TLS_RSA_WITH_AES_128_CBC_SHA",
  0x0035: "TLS_RSA_WITH_AES_256_CBC_SHA",
  0x000A: "TLS_RSA_WITH_3DES_EDE_CBC_SHA",
  0x0005: "TLS_RSA_WITH_RC4_128_SHA",
  0x0004: "TLS_RSA_WITH_RC4_128_MD5"
}

export function cipherSuiteName(suite) {
  if (suite === null || suite === undefined) {
    return null;
  }

  return CIPHER_SUITES[suite] ? CIPHER_SUITES[suite] : "0x" + suite.toString(16).toUpperCase().padStart(4, "0");
}

export default function TLSCipherSuite(props) {

  const suite = props.suite;

  if (suite === null || suite === undefined) {
    return <span className="text-muted">n/a</span>
  }

  const name = cipherSuiteName(suite);
  const hex = "0x" + suite.toString(16).toUpperCase().padStart(4, "0");

  // Shortened for the table. The copied value is the full name.
  const short = name.replace("TLS_", "").replace("_WITH_", "/");

  return <span className="machine-data"><FullCopy shortValue={short} fullValue={name + " (" + hex + ")"} /></span>

}

import React from "react";

function TLSCertificateHelp() {

  return (
      <React.Fragment>
        <p>
          The certificate must be in PEM format and will typically include a whole certificate chain.
          Certificate authorities will usually offer this file for download. If the file includes multiple
          blocks of Base64 plaintext, surrounded by <code>-----BEGIN CERTIFICATE-----</code>, you likely have
          the correct file.
        </p>

        <p>
          The private key file must be in PEM format, will often have a <code>.key</code> name ending and should contain
          a block of Base64 plaintext, surrounded by <code>-----BEGIN PRIVATE KEY-----</code> or something similar
          to <code>-----BEGIN RSA PRIVATE KEY-----</code>. Nzyme will try to convert your specific key formats as good
          as possible. (If it doesn&apos;t work, make sure the key is in <code>PKCS#1</code> or <code>PKCS#8</code> format,
          but it most likely is.)
        </p>

        <p>
          Certificate and key are stored in the database and automatically encrypted.
        </p>
      </React.Fragment>
  )

}

export default TLSCertificateHelp;
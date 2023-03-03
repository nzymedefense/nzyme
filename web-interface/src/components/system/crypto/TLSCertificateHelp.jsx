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
        The private key file will often have a <code>.key</code> name ending and should contain a block of
        Base64 plaintext, surrounded by <code>-----BEGIN PRIVATE KEY-----</code> or
        <code>-----BEGIN RSA PRIVATE KEY-----</code>. Important: The private key file must be
        in <code>PKCS8</code> format. There is a chance that your certificate authority provided you with
        a <code>PKCS1</code> file, but you can use <code>openssl</code> to convert it. If it does not look
        like described above, even if similar, it&apos;s probably not <code>PKCS8</code>.
      </p>
      </React.Fragment>
  )

}

export default TLSCertificateHelp;
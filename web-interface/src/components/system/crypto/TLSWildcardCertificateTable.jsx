import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import moment from "moment";

function TLSWildcardCertificateTable(props) {

  const crypto = props.crypto

  if (!crypto) {
    return <LoadingSpinner />
  }

  const certificates = Object.values(crypto.tls_wildcard_certificates);

  if (certificates.length === 0) {
    return (
        <div className="alert alert-info mb-0">
          No wildcard TLS certificates installed.
        </div>
    )
  }

  return (
      <table className="table table-sm table-hover table-striped">
        <thead>
        <tr>
          <th>Node Matcher</th>
          <th>Nodes</th>
          <th>Certificate Fingerprint</th>
          <th>Expires at</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {Object.keys(certificates.sort((a, b) => a.node_name.localeCompare(b.node_name))).map(function (key, i) {
          return (
              <tr key={'tlscert-' + i}>
                <td><code>{certificates[i].node_matcher}</code></td>
                <td>91</td>
                <td>{certificates[i].fingerprint.substring(0, 16).match(/.{1,2}/g).join(' ').toUpperCase()}</td>
                <td>{moment(certificates[i].expires_at).format()}</td>
                <td>
                  Manage
                </td>
              </tr>
          )
        })}
        </tbody>
      </table>
  )

}

export default TLSWildcardCertificateTable;
import React from 'react'
import LoadingSpinner from '../../../misc/LoadingSpinner'
import PGPKeysOutOfSyncWarning from "./PGPKeysOutOfSyncWarning";
import moment from "moment";

function PGPKeyTable (props) {
  const crypto = props.crypto

  if (!crypto) {
    return <LoadingSpinner />
  }

  const keys = Object.values(crypto.pgp_keys);

  return (
      <React.Fragment>
        <PGPKeysOutOfSyncWarning show={!crypto.pgp_keys_in_sync} />

        <table className="table table-sm table-hover table-striped">
          <thead>
            <tr>
              <th>Node</th>
              <th>Key Fingerprint</th>
              <th>Generated at</th>
            </tr>
          </thead>
          <tbody>
            {Object.keys(keys.sort((a, b) => a.node.localeCompare(b.node))).map(function (key, i) {
              return (
                <tr key={'ppgkey-' + i}>
                  <td>{keys[i].node}</td>
                  <td>{keys[i].fingerprint.match(/.{1,2}/g).join(' ')}</td>
                  <td>{moment(keys[i].created_at).format()}</td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </React.Fragment>
  )
}

export default PGPKeyTable

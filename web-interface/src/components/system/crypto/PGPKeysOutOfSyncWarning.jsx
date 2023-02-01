import React from "react";

function PGPKeysOutOfSyncWarning(props) {

  if (props.show) {
    return (
        <div className="alert alert-danger">
          <i className="fa-solid fa-warning"></i>{' '}
          <strong>Not every node has the same PGP key.</strong> This will cause potentially irreversible problems.
          Please make sure all nzyme nodes use the same local PGP keys.
          If you are unsure what to do, please follow the <a href="https://go.nzyme.org/crypto-pgp" target="_blank" rel="noreferrer">nzyme documentation</a>.
        </div>
    )
  } else {
    return null;
  }

}

export default PGPKeysOutOfSyncWarning;
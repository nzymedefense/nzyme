import React from "react";

function SecuritySuiteDetails(props) {
  const suite = props.suite;

  return (
      <React.Fragment>
        {suite.identifier}{' '}
        <span className="text-muted">
          (Group Cipher: {suite.group_cipher},{' '}
          Pairwise Ciphers: {suite.pairwise_ciphers},{' '}
          Key Management Modes: {suite.key_management_modes})
        </span>
      </React.Fragment>
  )

}

export default SecuritySuiteDetails;
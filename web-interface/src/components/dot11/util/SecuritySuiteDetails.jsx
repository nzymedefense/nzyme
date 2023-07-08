import React from "react";

function SecuritySuiteDetails(props) {
  const suite = props.suite;

  return (
      <React.Fragment>
        {suite.identifier}{' '}
        <span className="text-muted">
          (Group Cipher: {suite.group_cipher ? suite.group_cipher : "None"},{' '}
          Pairwise Ciphers: {suite.pairwise_ciphers ? suite.pairwise_ciphers : "None"},{' '}
          Key Management Modes: {suite.key_management_modes ? suite.key_management_modes : "None"})
        </span>
      </React.Fragment>
  )

}

export default SecuritySuiteDetails;
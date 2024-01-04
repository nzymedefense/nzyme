import React from "react";

function SecuritySuiteDetails(props) {
  const suite = props.suite;

  return (
      <React.Fragment>
        {suite.identifier}{' '}
        <span className="text-muted">
          (Group: {suite.group_cipher ? suite.group_cipher : "None"},{' '}
          Pairwise: {suite.pairwise_ciphers ? suite.pairwise_ciphers : "None"},{' '}
          Key Management: {suite.key_management_modes ? suite.key_management_modes : "None"}, {' '}
          PMF: {suite.pmf_mode ? suite.pmf_mode : "n/a"})
        </span>
      </React.Fragment>
  )

}

export default SecuritySuiteDetails;
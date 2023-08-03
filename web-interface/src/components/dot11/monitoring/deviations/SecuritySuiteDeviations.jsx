import React from "react";

function SecuritySuiteDeviations(props) {

  const deviations = props.deviations;

  if (!deviations || deviations.length === 0) {
    return null;
  }

  return (
      <React.Fragment>
        The following unexpected security suites were recorded:

        <ul className="mt-2 mb-2">
          {deviations.sort().map(function(ss, i) {
            return (
                <li key={"unexpectedss-" + i}>
                  {ss}
                </li>
            )
          })}
        </ul>
      </React.Fragment>
  )

}

export default SecuritySuiteDeviations;
import React from "react";
import SecuritySuiteDetails from "./SecuritySuiteDetails";

function SecuritySuites(props) {

  const suites = props.suites;

  return (
      <ul style={{listStyleType: "none", marginLeft: 0, paddingLeft: 0}} className="mb-0">
        {suites.map(function (suite, i) {
          return <li key={"secsuite-" + i}><SecuritySuiteDetails suite={suite} /></li>
        })}
      </ul>
  )

}

export default SecuritySuites;
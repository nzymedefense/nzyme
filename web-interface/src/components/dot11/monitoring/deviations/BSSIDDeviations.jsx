import React from "react";

function BSSIDDeviations(props) {

  const deviations = props.deviations;

  if (!deviations || deviations.length === 0) {
    return null;
  }

  return (
      <React.Fragment>
        The following unexpected BSSIDs were recorded:

        <ul className="mt-2 mb-2">
          {deviations.sort().map(function(bssid, i) {
            return (
                <li key={"unexpectedbssid-" + i}>
                  {bssid}
                </li>
            )
          })}
        </ul>
      </React.Fragment>
  )

}

export default BSSIDDeviations;
import React from "react";

function FingerprintDeviations(props) {

  const deviations = props.deviations;

  if (!deviations || Object.keys(deviations).length === 0) {
    return null;
  }

  return (
      <React.Fragment>
        The following unexpected fingerprints were recorded:

        <ul className="mt-2 mb-2">
          {Object.keys(deviations).sort().map(function(bssid, i){
            return (
                <li key={"fpbssid-" + i}>
                  For BSSID {bssid}
                  <ul>
                    {deviations[bssid].sort().map(function(fp, x){
                      return (
                          <li key={"fp-" + x}>
                            Unexpected Fingerprint: {fp}
                          </li>
                      )
                    })}
                  </ul>
                </li>
            )
          })}
        </ul>
      </React.Fragment>
  )

}

export default FingerprintDeviations;
import React from "react";

function BanditFingerprints(props) {

  const fingerprints = props.fingerprints;

  if (!fingerprints || fingerprints.length === 0) {
    return <div className="alert alert-info mb-0">This bandit has no fingerprints.</div>
  }

  return (
      <ul className="mb-0">
        {fingerprints.map((f, i) => {
          return <li key={"fp-" + i}>{f}</li>
        })}
      </ul>
  )

}

export default BanditFingerprints;
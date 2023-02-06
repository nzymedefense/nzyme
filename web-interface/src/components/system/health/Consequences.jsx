import React, {useEffect} from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import NodeClockConsequence from "./consequences/NodeClockConsequence";
import CryptoSyncConsequence from "./consequences/CryptoSyncConsequence";

function Consequences(props) {

  const indicators = props.indicators

  if (!indicators) {
    return <LoadingSpinner />
  }

  const consequences = []

  useEffect(() => {
    for (const k in indicators) {
      const indicator = indicators[k];

      if (indicator.level === 'RED' || indicator.level === 'ORANGE') {
        consequences.push(indicator.id)
      }
    }

    console.log(consequences);
  }, [indicators])

  return (
      <div className="health-consequences">
        <CryptoSyncConsequence show={true} />
        <NodeClockConsequence show={false} />
      </div>
  )

}

export default Consequences;
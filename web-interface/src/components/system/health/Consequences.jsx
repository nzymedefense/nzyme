import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import NodeClockConsequence from "./consequences/NodeClockConsequence";
import CryptoSyncConsequence from "./consequences/CryptoSyncConsequence";
import DatabaseClockConsequence from "./consequences/DatabaseClockConsequence";
import TapClockConsequence from "./consequences/TapClockConsequence";

function Consequences(props) {

  const indicators = props.indicators

  if (!indicators) {
    return <LoadingSpinner />
  }

  const [consequences, setConsequences] = useState([]);

  useEffect(() => {
    const tempCons = [];
    for (const k in indicators) {
      const indicator = indicators[k];

      if (indicator.level === 'RED' || indicator.level === 'ORANGE') {
        tempCons.push(indicator.id)
      }
    }

    setConsequences(tempCons);

  }, [indicators])

  return (
      <div className="health-consequences">
        <CryptoSyncConsequence show={consequences.includes("crypto_sync")} />
        <NodeClockConsequence show={consequences.includes("node_clock")} />
        <DatabaseClockConsequence show={consequences.includes("db_clock")} />
        <TapClockConsequence show={consequences.includes("tap_clock") || true} />
      </div>
  )

}

export default Consequences;
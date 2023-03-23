import React, {useEffect, useState} from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import NodeClockConsequence from "./consequences/NodeClockConsequence";
import CryptoSyncConsequence from "./consequences/CryptoSyncConsequence";
import DatabaseClockConsequence from "./consequences/DatabaseClockConsequence";
import TapClockConsequence from "./consequences/TapClockConsequence";
import NodeOfflineConsequence from "./consequences/NodeOfflineConsequence";
import TapOfflineConsequence from "./consequences/TapOfflineConsequence";
import TapThroughputConsequence from "./consequences/TapThroughputConsequence";
import TapDropConsequence from "./consequences/TapDropConsequence";
import TapBufferConsequence from "./consequences/TapBufferConsequence";
import TapErrorConsequence from "./consequences/TapErrorConsequence";
import TLSExpirationConsequence from "./consequences/TLSExpirationConsequence";
import TaskFailureConsequence from "./consequences/TaskFailureConsequence";
import TaskStuckConsequence from "./consequences/TaskStuckConsequence";

function Consequences(props) {

  const indicators = props.indicators

  const [consequences, setConsequences] = useState([]);

  useEffect(() => {
    const tempCons = [];
    for (const k in indicators) {
      const indicator = indicators[k];

      if (!indicator.expired && indicator.active && (indicator.level === 'RED' || indicator.level === 'ORANGE')) {
        tempCons.push(indicator.id)
      }
    }

    setConsequences(tempCons);

  }, [indicators])

  if (!indicators) {
    return <LoadingSpinner />
  }

  if (consequences.length === 0) {
    return (
        <div className="alert alert-info">
          This section will show consequences and resolution steps in case of illuminated indicators above.
        </div>
    )
  }

  return (
      <div className="health-consequences">
        <CryptoSyncConsequence show={consequences.includes("crypto_sync")} />
        <NodeClockConsequence show={consequences.includes("node_clock")} />
        <DatabaseClockConsequence show={consequences.includes("db_clock")} />
        <TapClockConsequence show={consequences.includes("tap_clock")} />
        <NodeOfflineConsequence show={consequences.includes("node_offline")} />
        <TapOfflineConsequence show={consequences.includes("tap_offline")} />
        <TapThroughputConsequence show={consequences.includes("tap_tpx")} />
        <TLSExpirationConsequence show={consequences.includes("tls_exp")} />
        <TapDropConsequence show={consequences.includes("tap_drop")} />
        <TapBufferConsequence show={consequences.includes("tap_buffer")} />
        <TapErrorConsequence show={consequences.includes("tap_error")} />
        <TaskFailureConsequence show={consequences.includes("tasks_queue_task_failure")} />
        <TaskStuckConsequence show={consequences.includes("tasks_queue_task_stuck")} />
      </div>
  )

}

export default Consequences;
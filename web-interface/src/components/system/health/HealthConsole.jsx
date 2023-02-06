import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Indicator from "./Indicator";
import CryptoSyncConsequence from "./consequences/CryptoSyncConsequence";
import NodeClockConsequence from "./consequences/NodeClockConsequence";

function HealthConsole(props) {

  const indicators = props.indicators

  if (!indicators) {
    return <LoadingSpinner />
  }

  return (
        <div className="health-console">
          <div className="hc-row">
            <Indicator indicator={indicators.crypto_sync} consequence={<CryptoSyncConsequence />} name="Crypto Sync" />
            <Indicator indicator={indicators.db_clock} name="DB Clock" />
            <Indicator indicator={indicators.node_clock} consequence={<NodeClockConsequence show={true} />} name="Node Clock" />
          </div>

          <div style={{clear: "both"}} />
        </div>
  )

}

export default HealthConsole;
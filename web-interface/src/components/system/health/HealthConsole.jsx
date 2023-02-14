import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Indicator from "./Indicator";

function HealthConsole(props) {

  const indicators = props.indicators

  if (!indicators) {
    return <LoadingSpinner />
  }

  return (
        <div className="health-console">
          <div className="hc-row">
            <Indicator indicator={indicators.crypto_sync} name="Crypto Sync" />
            <Indicator indicator={indicators.db_clock} name="DB Clock" />
            <Indicator indicator={indicators.node_clock} name="Node Clock" />
            <Indicator indicator={indicators.tap_clock} name="Tap Clock" />
            <Indicator indicator={indicators.node_offline} name="Node Offline" />
            <Indicator indicator={indicators.tap_offline} name="Tap Offline" />
          </div>

          <div className="hc-row">
            <Indicator indicator={indicators.tap_tpx} name="Tap TPX" />
            <Indicator indicator={indicators.tap_drop} name="Tap Drop" />
            <Indicator indicator={indicators.tap_buffer} name="Tap Buffer" />
            <Indicator indicator={indicators.tap_error} name="Tap Error" />
          </div>

          <div style={{clear: "both"}} />
        </div>
  )

}

export default HealthConsole;
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
            <Indicator indicator={indicators.node_down} name="Node Down" />
            <Indicator indicator={indicators.tap_down} name="Tap Down" />
          </div>

          <div className="hc-row">
            <Indicator indicator={indicators.node_cpu} name="Node CPU" />
            <Indicator indicator={indicators.node_ram} name="Node RAM" />
            <Indicator indicator={indicators.node_heap} name="Node Heap" />
            <Indicator indicator={indicators.tap_cpu} name="Tap CPU" />
            <Indicator indicator={indicators.tap_ram} name="Tap RAM" />
            <Indicator indicator={indicators.tap_tpx} name="Tap TPX" />
          </div>

          <div className="hc-row">
            <Indicator indicator={indicators.tap_err} name="Tap Errors" />
          </div>

          <div style={{clear: "both"}} />
        </div>
  )

}

export default HealthConsole;
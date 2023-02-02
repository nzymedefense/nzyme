import React from "react";

function HealthConsole(props) {

  return (
        <div className="health-console">
          <div className="hc-row">
            <div className="hc-indicator hc-green">Crypto Sync</div>
            <div className="hc-indicator hc-green">DB Clock</div>
            <div className="hc-indicator hc-red">Node Clock</div>
            <div className="hc-indicator hc-green">Tap Clock</div>
            <div className="hc-indicator hc-green">Node Down</div>
            <div className="hc-indicator hc-green">Tap Down</div>
          </div>

          <div className="hc-row">
            <div className="hc-indicator hc-green">Node CPU</div>
            <div className="hc-indicator hc-green">Node RAM</div>
            <div className="hc-indicator hc-green">Node Heap</div>
            <div className="hc-indicator hc-green">Tap CPU</div>
            <div className="hc-indicator hc-green">Tap RAM</div>
            <div className="hc-indicator hc-green">Tap TPX</div>
          </div>

          <div className="hc-row">
            <div className="hc-indicator hc-green">Tap ERR</div>
          </div>

          <div style={{clear: "both"}} />
        </div>
  )

}

export default HealthConsole;
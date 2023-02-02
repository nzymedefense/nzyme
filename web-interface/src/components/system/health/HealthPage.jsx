import React from "react";
import HealthConsole from "./HealthConsole";
import Consequences from "./Consequences";

function HealthPage(props) {

  return (
      <div>
        <div className="row">
          <div className="col-md-12">
            <h1>Health Console</h1>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <HealthConsole />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Consequences</h3>

                <Consequences />
              </div>
            </div>
          </div>
        </div>
      </div>
  )

}

export default HealthPage;
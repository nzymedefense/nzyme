import React, {useEffect, useState} from "react";
import HealthConsole from "./HealthConsole";
import Consequences from "./Consequences";
import SystemService from "../../../services/SystemService";
import HealthConsoleConfiguration from "./HealthConsoleConfiguration";

const systemService = new SystemService();

function fetchData(setIndicators) {
  systemService.getHealthIndicators(setIndicators)
}

function HealthPage(props) {

  const [indicators, setIndicators] = useState(null)

  useEffect(() => {
    fetchData(setIndicators)
    const id = setInterval(() => fetchData(setIndicators), 5000)
    return () => clearInterval(id)
  }, [setIndicators])

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
                <p className="help-text">
                  An nzyme deployment is constantly monitoring itself for common issues. Resolution steps will appear
                  below if an indicator is illuminated in any other color than green.
                </p>

                <hr />

                <HealthConsole indicators={indicators} />

                <p className="mt-3 mb-0">
                  <strong>Important:</strong> Checks are performed periodically and it can take up to 60 seconds for an indicator to extinguish
                  after problem resolution.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Consequences</h3>

                <Consequences indicators={indicators} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Configuration</h3>

                <HealthConsoleConfiguration indicators={indicators} />
              </div>
            </div>
          </div>
        </div>
      </div>
  )

}

export default HealthPage;
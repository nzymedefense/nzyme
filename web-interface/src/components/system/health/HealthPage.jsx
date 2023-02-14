import React, {useEffect, useState} from "react";
import HealthConsole from "./HealthConsole";
import Consequences from "./Consequences";
import SystemService from "../../../services/SystemService";

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
                  An nzyme deployment is constantly monitoring itself for common issues. If an indicator below is
                  illuminated in any other color than green, resolution steps will appear below.
                </p>

                <hr />

                <HealthConsole indicators={indicators} />
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
      </div>
  )

}

export default HealthPage;
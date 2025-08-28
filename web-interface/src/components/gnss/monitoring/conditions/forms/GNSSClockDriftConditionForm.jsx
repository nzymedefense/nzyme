import React, {useState} from "react";

export default function GNSSClockDriftConditionForm(props) {

  const onConditionAdded = props.onConditionAdded;

  const [drift, setDrift] = useState(100);

  const formIsReady = () => {
    return drift !== null && drift > 0
  }

  const format = () => {
    return { drift: drift }
  }

  return (
      <React.Fragment>
        <p>
          Condition triggers if the recorded GNSS clock drift is greater than the defined threshold.
        </p>

        <label htmlFor="condition_clock_drift" className="form-label">Maximum Clock Drift</label>
        <div className="input-group mb-3">
          <input type="number"
                 className="form-control"
                 id="condition_clock_drift"
                 min={1}
                 value={drift}
                 onChange={(e) => {
                   setDrift(parseInt(e.target.value, 10))
                 }}/>
          <span className="input-group-text">ms</span>
        </div>

        <div className="mt-2">
          <button className="btn btn-primary"
                  disabled={!formIsReady()}
                  onClick={(e) => { e.preventDefault(); onConditionAdded(format()) }}>
            Add Condition
          </button>
        </div>
      </React.Fragment>
  )

}
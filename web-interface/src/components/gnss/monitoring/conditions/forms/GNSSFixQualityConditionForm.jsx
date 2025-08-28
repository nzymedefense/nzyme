import React, {useState} from "react";

export default function GNSSFixQualityConditionForm(props) {

  const onConditionAdded = props.onConditionAdded;

  const [minimumContinuousFixQuality, setMinimumContinuousFixQuality] = useState("FIX_3D");
  const [timeframeMinutes, setTimeframeMinutes] = useState(5);

  const formIsReady = () => {
    return minimumContinuousFixQuality !== null && timeframeMinutes > 1
  }

  const format = () => {
    return { minimumContinuousFixQuality: minimumContinuousFixQuality, timeframeMinutes: timeframeMinutes }
  }

  return (
      <React.Fragment>
        <p>
          Condition triggers if the recorded GNSS fix quality continuously falls below the selected minimum quality
          during the configured time frame. For example, you could configure this condition to trigger if a fix is
          lost or degraded for 5 minutes without ever recovering in between.
        </p>

        <div className="mb-3">
          <label htmlFor="condition_fix_quality" className="form-label">Minimum Continuous Fix Quality</label>
          <select className="form-control"
                  id="condition_fix_quality"
                  value={minimumContinuousFixQuality}
                  onChange={(e) => setMinimumContinuousFixQuality(e.target.value)}>
            <option value="FIX_2D">2D Fix</option>
            <option value="FIX_3D">3D Fix</option>
          </select>
        </div>

        <label htmlFor="condition_timeframe_minutes" className="form-label">Timeframe</label>
        <div className="input-group mb-3">
          <input type="number"
                 className="form-control"
                 id="condition_timeframe_minutes"
                 min={1}
                 value={timeframeMinutes}
                 onChange={(e) => {
                   setTimeframeMinutes(parseInt(e.target.value, 10))
                 }}/>
          <span className="input-group-text">Minutes</span>
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
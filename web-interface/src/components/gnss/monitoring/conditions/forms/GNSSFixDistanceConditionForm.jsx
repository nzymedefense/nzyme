import React, {useState} from "react";

export default function GNSSFixDistanceConditionForm(props) {

  const onConditionAdded = props.onConditionAdded;

  const [distanceMeters, setDistanceMeters] = useState(10);

  const formIsReady = () => {
    return distanceMeters !== null && distanceMeters > 1
  }

  const format = () => {
    return { distanceMeters: distanceMeters }
  }

  return (
      <React.Fragment>
        <p>
          Condition triggers if the recorded GNSS location of any of the selected taps is farther away fron its actual
          location than the defined threshold. For example, you could configure this condition to trigger if a recorded
          GNSS location is more than 10 meters away from the known tap location.
        </p>

        <label htmlFor="condition_distance_meters" className="form-label">Maximum Distance</label>
        <div className="input-group mb-3">
          <input type="number"
                 className="form-control"
                 id="condition_distance_meters"
                 min={1}
                 value={distanceMeters}
                 onChange={(e) => {
                   setDistanceMeters(parseInt(e.target.value, 10))
                 }}/>
          <span className="input-group-text">Meters</span>
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
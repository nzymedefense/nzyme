import React, {useState} from "react";

export default function GNSSConstellationConditionForm(props) {

  const onConditionAdded = props.onConditionAdded;

  const [constellation, setConstellation] = useState("GPS");

  const formIsReady = () => {
    return constellation !== null
  }

  const format = () => {
    return { constellation: constellation }
  }

  return (
      <React.Fragment>
        <div>
          <label htmlFor="condition_constellation" className="form-label">Constellation</label>
          <select className="form-control"
                  id="condition_constellation"
                  value={constellation}
                  onChange={(e) => setConstellation(e.target.value)}>
            <option value="GPS">GPS</option>
            <option value="GLONASS">GLONASS</option>
            <option value="Galileo">Galileo</option>
            <option value="BeiDou">BeiDou</option>
          </select>
          <div className="form-text">The constellation to match.</div>
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
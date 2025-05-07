import React, {useState} from "react";

export default function BluetoothSignalStrengthConditionForm(props) {

  const onConditionAdded = props.onConditionAdded;

  const [operator, setOperator] = useState("lt");
  const [rssi, setRssi] = useState(-35);

  const selectOperator = (e, op) => {
    e.preventDefault();


    setOperator(op);
  }

  const formIsReady = () => {
    return operator !== null && operator.length > 0 && rssi !== null && rssi <= 0
  }

  const format = () => {
    return { operator: operator, rssi: rssi }
  }

  return (
    <React.Fragment>
      <div>
        <div className="input-group mb-3">
          <button className="btn btn-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">
            {operator === "lt" ? "Lower than" : "Greater than"}
          </button>
          <ul className="dropdown-menu">
            <li><a className="dropdown-item" href="#" onClick={(e) => selectOperator(e, "lt")}>Lower than</a></li>
            <li><a className="dropdown-item" href="#" onClick={(e) => selectOperator(e, "gt")}>Greater than</a></li>
          </ul>
          <input type="number"
                 className="form-control"
                 min={-100}
                 max={0}
                 value={rssi}
                 onChange={(e) => {
                   setRssi(parseInt(e.target.value, 10))
                 }}/>
          <span className="input-group-text">dB RSSI</span>
        </div>
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
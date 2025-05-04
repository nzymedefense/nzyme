import React, {useState} from "react";

export default function BluetoothSignalStrengthConditionForm(props) {

  const onConditionAdded = props.onConditionAdded;

  const [operator, setOperator] = useState("LARGER");
  const [rssi, setRssi] = useState("-35");

  const formIsReady = () => {
    return operator !== null && operator.length > 0 && rssi !== null && parseInt(rssi, 10) <= 0
  }

  const format = () => {
    return { operator: operator, rssi: parseInt(rssi, 10) }
  }

  return (
    <React.Fragment>
      <div>
        INPUT GROUP DROPDOWN OPERATOR, NUMBER (max/min)
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
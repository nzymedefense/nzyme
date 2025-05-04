import React, {useState} from "react";

export default function BluetoothDeviceTypeConditionForm(props) {

  const onConditionAdded = props.onConditionAdded;

  const [type, setType] = useState("apple_find_my_paired");

  const formIsReady = () => {
    return type !== null && type.length > 0
  }

  const format = () => {
    return { type: type }
  }

  return (
    <React.Fragment>
      <div>
        <label htmlFor="condition_device_type" className="form-label">Device Type</label>
        <select className="form-control"
                id="new_condition"
                value={type}
                onChange={(e) => setType(e.target.value)}>
          <option value="apple_find_my_paired">Apple &quot;Find My&quot; (Paired)</option>
          <option value="apple_find_my_unpaired">Apple &quot;Find My&quot; (Unpaired)</option>
          <option value="meshtastic_node">Meshtastic Node</option>
        </select>
        <div className="form-text">The device type to match.</div>
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
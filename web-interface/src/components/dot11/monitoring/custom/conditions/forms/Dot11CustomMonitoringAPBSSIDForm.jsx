import React, {useState} from "react";

export default function Dot11CustomMonitoringAPBSSIDForm(props) {

  const onConditionAdded = props.onConditionAdded;

  const [bssid, setBssid] = useState("");

  const formIsReady = () => {
    return bssid // TODO validate MAC
  }

  const format = () => {
    return { bssid: bssid }
  }

  return (
    <React.Fragment>
      <p>
        Condition triggers if the BSSID of an access point matches the configured BSSID.
      </p>

      <label htmlFor="condition_ap_bssid" className="form-label">Access Point BSSID</label>
      <input type="text"
             className="form-control"
             id="condition_ap_bssid"
             value={bssid}
             onChange={(e) => {
               setBssid(e.target.value.trim())
             }}/>

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
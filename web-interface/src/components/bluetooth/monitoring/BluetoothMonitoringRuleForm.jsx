import React, {useEffect, useState} from "react";
import TapsService from "../../../services/TapsService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import BluetoothMacAddressConditionForm from "./conditions/BluetoothMacAddressConditionForm";

const tapsService = new TapsService();

export default function BluetoothMonitoringRuleForm(props) {

  const onSubmit = props.onSubmit;
  const submitTextProp = props.submitText;

  const [name, setName] = useState(props.name ? props.name : "");
  const [description, setDescription] = useState(props.description ? props.description : "");

  const [tapLimiterType, setTapLimiterType] = useState(props.tapLimiterType ? props.tapLimiterType : "");
  const [selectedTaps, setSelectedTaps] = useState(props.selectedTaps ? props.selectedTaps : [])

  const [selectedConditionType, setSelectedConditionType] = useState("MAC_ADDRESS");
  const [conditionFormType, setConditionFormType] = useState(null);

  const [availableTaps, setAvailableTaps] = useState(null);

  const [isSubmitting, setIsSubmitting] = React.useState(false);
  const [submitText, setSubmitText] = useState(submitTextProp);

  useEffect(() => {
    tapsService.findAllTapsHighLevel((r) => setAvailableTaps(r.data.taps))
  }, []);

  useEffect(() => {
    if (tapLimiterType === "ALL") {
      // Reset selected taps if ALL is selected.
      setSelectedTaps([]);
    }
  }, [tapLimiterType]);

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const onTapSelected = (uuid) => {
    setSelectedTaps(prev => {
      if (prev.includes(uuid)) {
        // Remove tap.
        return prev.filter(id => id !== uuid);
      } else {
        // Add tao.
        return [...prev, uuid];
      }
    });
  }

  const submit = function(e) {
    e.preventDefault();
  }

  const formIsReady = function() {
    return name && name.trim().length > 0
  }

  const tapLimiterForm = () => {
    if (tapLimiterType === "SELECTED") {
      return (
        <div className="mb-3">
          <label className="form-label">Selected Taps</label>

          <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
              <th style={{width: 40}} title="Selected">Sel.</th>
              <th>Name</th>
              <th>Online</th>
            </tr>
            </thead>
            <tbody>
            {availableTaps.map((tap, i) => {
              return (
                <tr key={i}>
                  <td>
                      <input className="form-check-input"
                             type="checkbox"
                             checked={selectedTaps.includes(tap.uuid)}
                             onChange={() => onTapSelected(tap.uuid)} />
                  </td>
                  <td>{tap.name}</td>
                  <td>{tap.is_online ?
                    <span className="text-success">Online</span>
                    : <span className="text-warning">Offline</span>}
                  </td>
                </tr>
              )
            })}
            </tbody>
          </table>
        </div>
      )
    }

    return null;
  }

  const newConditionForm = () => {
    if (!conditionFormType) {
      return null;
    }

    let form = null;
    switch (conditionFormType) {
      case "MAC_ADDRESS": form = <BluetoothMacAddressConditionForm />
    }

    return <div className="condition-form mb-3">{form}</div>
  }

  if (availableTaps == null) {
    return <LoadingSpinner />
  }

  return (
    <form>
      <p>
        Triggers a detection event when all conditions are met. If no conditions are configured, any Bluetooth activity
        will trigger this rule.
      </p>

      <div className="mb-3">
        <label htmlFor="name" className="form-label">Name</label>
        <input type="text" className="form-control" id="name"
               value={name} onChange={(e) => { updateValue(e, setName) }} />
        <div className="form-text">A descriptive name of the rule.</div>
      </div>

      <div className="mb-3">
        <label htmlFor="description" className="form-label">Description <small>Optional</small></label>
        <textarea className="form-control" id="description"
                  value={description} onChange={(e) => { updateValue(e, setDescription) }} />
        <div className="form-text">An optional description that provides more detail.</div>
      </div>

      <div className="mb-3">
        <label htmlFor="tap_limiter_type" className="form-label">Taps</label>
        <select className="form-control" id="tap_limiter_type"
                value={tapLimiterType} onChange={(e) => { updateValue(e, setTapLimiterType) }} >
          <option value="ALL">All Taps</option>
          <option value="SELECTED">Selected Taps Only</option>
        </select>
        <div className="form-text">
          You can limit this rule to specific taps.
        </div>
      </div>

      {tapLimiterForm()}

      <hr />

      <h3>Conditions</h3>

      <p>
        Conditions of the same type are <code>OR</code> connected, all other conditions are <code>AND</code> connected.
      </p>

      <div className="input-group mb-3">
        <select className="form-control"
                id="new_condition"
                value={selectedConditionType}
                onChange={(e) => updateValue(e, setSelectedConditionType)}>
          <option value="MAC_ADDRESS">MAC Address</option>
          <option value="DEVICE_TYPE">Device Type</option>
          <option value="SIGNAL_STRENGTH">Signal Strength</option>
          <option value="TIME">Time &amp; Day</option>
        </select>
        <button className="btn btn-secondary"
                onClick={(e) => { e.preventDefault(); setConditionFormType(selectedConditionType) }}>
          Create Condition
        </button>
      </div>

      {newConditionForm()}

      <button className="btn btn-primary" onClick={submit} disabled={!formIsReady() || isSubmitting}>
        {submitText}
      </button>
    </form>
  )

}
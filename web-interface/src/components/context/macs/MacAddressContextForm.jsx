import React, {useState} from "react";

function MacAddressContextForm(props) {

  const submitText = props.submitText;

  const [macAddress, setMacAddress] = useState(props.macAddress ? props.macAddress : "");

  const [formSubmitting, setFormSubmitting] = useState(false);

  const formIsReady = () => {
    return false;
  }

  const submit = () => {

  }

  return (
    <React.Fragment>
      <div className="mb-3">
        <label htmlFor="macAddress" className="form-label">MAC Address</label>
        <input type="text" className="form-control" id="macAddress"
               value={macAddress} onChange={(e) => { setMacAddress(e.target.value) }} />
        <div className="form-text">The MAC address you want to add context to.</div>
      </div>

      <button className="btn btn-sm btn-primary" onClick={submit} disabled={!formIsReady() || formSubmitting}>
        {formSubmitting ? "Please wait ..." : submitText}
      </button>
    </React.Fragment>
  )

}

export default MacAddressContextForm;
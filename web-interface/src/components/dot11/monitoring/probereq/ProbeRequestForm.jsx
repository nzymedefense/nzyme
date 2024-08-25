import React, {useState} from 'react';

export default function ProbeRequestForm(props) {

  const onSubmit = props.onSubmit;
  const submitText = props.submitText;
  const [ssid, setSsid] = useState(props.ssid ? props.ssid : "");
  const [notes, setNotes] = useState(props.notes ? props.notes : "");

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formIsReady = function() {
    return ssid && ssid.trim().length > 0
  }

  const submit = function(e) {
    e.preventDefault();
    onSubmit(ssid, notes);
  }

  return (
      <form>
        <div className="mb-3">
          <label htmlFor="ssid" className="form-label">SSID</label>
          <input type="text" className="form-control" id="ssid" aria-describedby="ssid"
                 value={ssid} onChange={(e) => { updateValue(e, setSsid) }} />
          <div className="form-text">The SSID in probe requests to monitor for.</div>
        </div>

        <div className="mb-3">
          <label htmlFor="notes" className="form-label">Notes <small>Optional</small></label>
          <textarea className="form-control" id="notes" rows="3"
                    value={notes} onChange={(e) => { updateValue(e, setNotes) }} />
          <div className="form-text">
            Optional notes that may help you or others understand why this SSID is monitored.
          </div>
        </div>

        <button className="btn btn-sm btn-primary" onClick={submit} disabled={!formIsReady()}>
          {submitText}
        </button>
      </form>
  )

}
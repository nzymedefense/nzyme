import React, {useState} from "react";
import {isValidMACAddress} from "../../../util/Tools";

function MacAddressContextForm(props) {

  const submitText = props.submitText;
  const onSubmit = props.onSubmit;

  const formatName = (name) => {
    return name.toUpperCase().replace(/[^a-z0-9_]/gi, '');
  }

  const [macAddress, setMacAddress] = useState(props.macAddress ? props.macAddress : "");
  const [subsystem, setSubsystem] = useState(props.subsystem ? props.subsystem : "ETHERNET");
  const [name, setName] = useState(props.name ? formatName(props.name) : "");
  const [description, setDescription] = useState(props.description ? props.description : "")
  const [notes, setNotes] = useState(props.notes ? props.notes : "")

  const [formSubmitting, setFormSubmitting] = useState(false);

  const formIsReady = () => {
    return isValidMACAddress(macAddress)
        && subsystem && subsystem.trim().length > 0
        && name && name.trim().length > 0
  }

  const submit = () => {
    setFormSubmitting(true);

    onSubmit(macAddress, subsystem, name, description, notes, () => {
      setFormSubmitting(false);
    });
  }

  return (
    <React.Fragment>
      <div className="mb-3">
        <label htmlFor="macAddress" className="form-label">MAC Address</label>
        <input type="text" className="form-control" id="macAddress"
               value={macAddress} onChange={(e) => { setMacAddress(e.target.value) }} />
        <div className="form-text">The MAC address you want to add context to.</div>
      </div>

      <div className="mb-3">
        <label htmlFor="subsystem" className="form-label">Subsystem</label>
        <select className="form-select" id="subsystem"
                value={subsystem} onChange={(e) => { setSubsystem(e.target.value) }}>
          <option value="ETHERNET">Ethernet</option>
          <option value="DOT11">802.11 / WiFi</option>
        </select>
        <div className="form-text">
          The subsystem the MAC address exists in. You can add context to the same MAC address
          in different subsystems, but it has to be unique within it's subsystem. For example: A MAC address can have
          different context in Ethernet data compared to WiFi data.
        </div>
      </div>

      <hr />

      <div className="mb-3">
        <label htmlFor="name" className="form-label">Name</label>
        <input type="text" className="form-control" id="name" maxLength={12}
               value={name} onChange={(e) => { setName(formatName(e.target.value)) }} />
        <div className="form-text">
          A short name describing the MAC address. This name will appear next to the address. It cannot be longer than
          12 characters, cannot include special characters except underscores and must be uppercase.
        </div>
      </div>

      <div className="mb-3">
        <label htmlFor="description" className="form-label">Description <small>Optional</small></label>
        <input type="text" className="form-control" id="description" maxLength={32}
               value={description} onChange={(e) => { setDescription(e.target.value) }} />
        <div className="form-text">
          A short description of the MAC address, not longer than 32 characters. Longer descriptions should be added
          to the <i>notes</i> field below.
        </div>
      </div>

      <div className="mb-3">
        <label htmlFor="description" className="form-label">Notes <small>Optional</small></label>
        <textarea type="text" className="form-control" id="description"
                  style={{height: 200}}
                  value={notes} onChange={(e) => { setNotes(e.target.value) }} />
        <div className="form-text">
          Notes about this MAC address. <strong>Markdown is supported.</strong>
        </div>
      </div>

      <button className="btn btn-sm btn-primary" onClick={submit} disabled={!formIsReady() || formSubmitting}>
        {formSubmitting ? "Please wait ..." : submitText}
      </button>
    </React.Fragment>
  )

}

export default MacAddressContextForm;
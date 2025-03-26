import React, {useState} from "react";
import {notify} from "react-notify-toast";

export default function CotOutputForm(props) {

  const onSubmit = props.onSubmit;
  const submitTextProp = props.submitText;

  const [name, setName] = useState(props.name ? props.name : "");
  const [description, setDescription] = useState(props.description ? props.description : "");
  const [tapLeafType, setTapLeafType] = useState(props.tapLeafType ? props.tapLeafType : "a-f-G-U-U-M-S-E");
  const [address, setAddress] = useState(props.address ? props.address : "");
  const [port, setPort] = useState(props.port ? props.port : "");

  const [isSubmitting, setIsSubmitting] = React.useState(false);
  const [submitText, setSubmitText] = useState(submitTextProp);

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formIsReady = function() {
    return name && name.trim().length > 0
        && tapLeafType && tapLeafType.trim().length > 0
        && address && address.trim().length > 0
        && port && port > 0 && port <= 65535
  }

  const submit = function(e) {
    e.preventDefault();

    setIsSubmitting(true);
    setSubmitText(<span><i className="fa-solid fa-circle-notch fa-spin"></i> &nbsp;Creating ...</span>)

    onSubmit(name, description, tapLeafType, address, port, (error) => {
      if (error.response) {
        if (error.response.status === 422) {
          notify.show("Could not create output. Quota exceeded. Please contact your administrator.", "error")
        } else {
          notify.show("Could not create output.", "error")
        }
      }

      setIsSubmitting(false);
      setSubmitText(submitTextProp);
    })
  }


  return (
      <form>
        <div className="mb-3">
          <label htmlFor="name" className="form-label">Name</label>
          <input type="text" className="form-control" id="name"
                 value={name} onChange={(e) => { updateValue(e, setName) }} />
          <div className="form-text">A descriptive name of the new output.</div>
        </div>

        <div className="mb-3">
          <label htmlFor="name" className="form-label">Description <small>Optional</small></label>
          <textarea className="form-control" id="description"
                 value={description} onChange={(e) => { updateValue(e, setDescription) }} />
          <div className="form-text">An optional description that provides more detail.</div>
        </div>

        <div className="mb-3">
          <label htmlFor="tap_leaf_type" className="form-label">Tap Leaf Type</label>
          <select className="form-control" id="tap_leaf_type"
                  value={tapLeafType} onChange={(e) => { updateValue(e, setTapLeafType) }} >
            <option value="a-f-G-U-U-M-S-E">Ground-Based Friendly Electronic Warfare</option>
            <option value="a-f-G-U-U-M-R-S">Ground-Based Friendly Sensor</option>
            <option value="a-f-G-U-U-M-S">Ground-Based Friendly Signal Intelligence</option>
          </select>
          <div className="form-text">
            Which leaf type to use for Nzyme tap locations.
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="address" className="form-label">COT Server Address</label>
          <input type="text" className="form-control" id="address"
                 value={address} onChange={(e) => { updateValue(e, setAddress) }} />
          <div className="form-text">IP address or hostname of the target COT server.</div>
        </div>

        <div className="mb-3">
          <label htmlFor="port" className="form-label">COT Server Port</label>
          <input type="number" className="form-control" id="port" min={0} max={65535}
                 value={port} onChange={(e) => { updateValue(e, setPort) }} />
          <div className="form-text">Port of the target COT server.</div>
        </div>

        <button className="btn btn-primary" onClick={submit} disabled={!formIsReady() || isSubmitting}>
          {submitText}
        </button>
      </form>
  )


}
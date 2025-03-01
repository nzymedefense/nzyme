import React, {useState} from "react";
import {notify} from "react-notify-toast";

export default function UavTypeForm(props) {

  const onSubmit = props.onSubmit;
  const submitTextProp = props.submitText;

  const [matchType, setMatchType] = useState(props.matchType ? props.matchType : "");
  const [matchValue, setMatchValue] = useState(props.matchValue ? props.matchValue : "");
  const [defaultClassification, setDefaultClassification] = useState(props.defaultClassification ? props.defaultClassification : "");
  const [type, setType] = useState(props.type ? props.type : "");
  const [name, setName] = useState(props.name ? props.name : "");

  const [isSubmitting, setIsSubmitting] = React.useState(false);
  const [submitText, setSubmitText] = useState(submitTextProp);

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formIsReady = function() {
    return matchType && matchType.trim().length > 0
        && matchValue && matchValue.trim().length > 0
        && type && type.trim().length > 0
        && name && name.trim().length > 0
  }

  const submit = function(e) {
    e.preventDefault();

    setIsSubmitting(true);
    setSubmitText(<span><i className="fa-solid fa-circle-notch fa-spin"></i> &nbsp;Creating ...</span>)

    onSubmit(matchType, matchValue, defaultClassification, type, name, () => {
      // On failure.
      notify.show("Could not create custom UAV type.", "error");

      setIsSubmitting(false);
      setSubmitText(submitTextProp);
    })
  }

  return (
      <form>
        <div className="mb-3">
          <label htmlFor="match_type" className="form-label">Match Type</label>
          <select className="form-control" id="match_type" value={matchType} onChange={(e) => { updateValue(e, setMatchType) }}>
            <option value="EXACT">Serial Number Exact Match</option>
            <option value="PREFIX">Serial Number Prefix Match</option>
          </select>
          <div className="form-text">
            How to match the new type to a UAV serial number. The <em>exact match</em> must match the entire UAV serial
            number to apply the type to it, while the <em>prefix match</em> must only match the first characters of the
            serial number.
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="match_value" className="form-label">Match Value</label>
          <input type="text" className="form-control" id="match_value"
                 value={matchValue} onChange={(e) => { updateValue(e, setMatchValue) }} />
          <div className="form-text">
            The UAV serial number to match using the <span>match type</span> selected above.
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="type" className="form-label">Type</label>
          <select className="form-control" id="type" value={type} onChange={(e) => { updateValue(e, setType) }}>
            <option value="GENERIC">Generic</option>
            <option value="AERIAL_INTELLIGENCE">Aerial Intelligence</option>
            <option value="AGRICULTURAL">Agricultural</option>
            <option value="CARGO">Cargo / Heavy Lift</option>
            <option value="DELIVERY_DRONE">Delivery</option>
            <option value="PHOTO_VIDEO">Photography/Videography</option>
            <option value="PRO_MULTI">Professional Multi-Use</option>
            <option value="PRO_SURVEY">Professional Survey</option>
          </select>
          <div className="form-text">
            Type of UAV.
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="name" className="form-label">Name</label>
          <input type="text" className="form-control" id="name"
                 value={name} onChange={(e) => { updateValue(e, setName) }} />
          <div className="form-text">The type name or description of the matched UAV.</div>
        </div>

        <div className="mb-3">
          <label htmlFor="default_classification" className="form-label">Default Classification</label>
          <select className="form-control" id="default_classification" value={defaultClassification} onChange={(e) => { updateValue(e, setDefaultClassification) }}>
            <option value="">None (Unknown)</option>
            <option value="NEUTRAL">Neutral</option>
            <option value="FRIENDLY">Friendly</option>
            <option value="HOSTILE">Hostile</option>
          </select>
          <div className="form-text">
            Default classification to apply to the matched UAV. Can be overridden by privileged user after detection.
          </div>
        </div>

        <button className="btn btn-primary" onClick={submit} disabled={!formIsReady() || isSubmitting}>
          {submitText}
        </button>
      </form>
  )

}
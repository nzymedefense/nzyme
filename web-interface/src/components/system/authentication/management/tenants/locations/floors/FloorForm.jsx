import React, {useState} from "react";
import FormSubmitErrorMessage from "../../../../../../misc/FormSubmitErrorMessage";

function FloorForm(props) {

  const onSubmit = props.onSubmit;
  const submitText = props.submitText;
  const [number, setNumber] = useState(props.number ? props.number : "");
  const [name, setName] = useState(props.name ? props.name : "");

  const errorMessage = props.errorMessage;

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formIsReady = function() {
    return number && parseInt(number, 10) >= 0
  }

  const submit = function(e) {
    e.preventDefault();
    onSubmit(number, name);
  }

  return (
      <form>
        <div className="mb-3">
          <label htmlFor="number" className="form-label">Floor Number</label>
          <input type="number" className="form-control" id="number" aria-describedby="number" placeholder="0"
                 value={number} onChange={(e) => { updateValue(e, setNumber) }} />
          <div className="form-text">
            The floor number. For example, ground level would be 0 and the first floor would be 1. Must be unique for
            this location. Does not have to be continuous across the location and does not have to start at 0. For
            example, you could only have floors 20 and 25 if you are leasing space in a larger building or only deploy
            WiFi taps on select floors.
          </div>
        </div>

        <div className="mb-3">
          <label htmlFor="name" className="form-label">Name <small>Optional</small></label>
          <textarea className="form-control" id="description" rows="3" placeholder="Ground Level"
                    value={name} onChange={(e) => { updateValue(e, setName) }} />
          <div className="form-text">
            The name of this floor. Defaults to the floor number (like &quot;Floor 1&quot;) if not provided
          </div>
        </div>

        <button className="btn btn-sm btn-primary" onClick={submit} disabled={!formIsReady()}>
          {submitText}
        </button>

        <FormSubmitErrorMessage message={errorMessage} />
      </form>
  )

}

export default FloorForm;
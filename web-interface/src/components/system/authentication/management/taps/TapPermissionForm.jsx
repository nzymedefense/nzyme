import React, {useState} from "react";

function TapPermissionForm(props) {

  const onClick = props.onClick;
  const submitText = props.submitText;
  const [name, setName] = useState(props.name ? props.name : "");
  const [description, setDescription] = useState(props.description ? props.description : "");

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formIsReady = function() {
    return name && name.trim().length > 0 && description && description.trim().length > 0
  }

  const submit = function(e) {
    e.preventDefault();
    onClick(name, description);
  }

  return (
      <form>
        <div className="mb-3">
          <label htmlFor="name" className="form-label">Name</label>
          <input type="text" className="form-control" id="name" aria-describedby="name"
                 value={name} onChange={(e) => { updateValue(e, setName) }} />
          <div className="form-text">The name of the new tap.</div>
        </div>

        <div className="mb-3">
          <label htmlFor="description" className="form-label">Description</label>
          <textarea className="form-control" id="description" rows="3"
                    value={description} onChange={(e) => { updateValue(e, setDescription) }} />
          <div className="form-text">A short description of the new tap.</div>
        </div>

        <button className="btn btn-primary" onClick={submit} disabled={!formIsReady()}>
          {submitText}
        </button>
      </form>
  )

}

export default TapPermissionForm;
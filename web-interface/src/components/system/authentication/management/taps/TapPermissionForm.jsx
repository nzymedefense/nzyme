import React, {useState} from "react";
import TapPositionMap from "./TapPositionMap";
import LatitudeLongitude from "../../../../shared/LatitudeLongitude";

function TapPermissionForm(props) {

  const onClick = props.onClick;
  const submitText = props.submitText;
  const [name, setName] = useState(props.name ? props.name : "");
  const [description, setDescription] = useState(props.description ? props.description : "");

  const [longitude, setLongitude] = useState(props.longitude ? props.longitude : null);
  const [latitude, setLatitude] = useState(props.latitude ? props.latitude : null);

  const [additionalToggled, setAdditionalToggled] = useState(false);

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formIsReady = function() {
    return name && name.trim().length > 0 && description && description.trim().length > 0
  }

  const toggleAdditionalOptions = (e) => {
    e.preventDefault();
    setAdditionalToggled(!additionalToggled)
  }

  const submit = function(e) {
    e.preventDefault();
    onClick(name, description, latitude, longitude);
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

        <div className="mb-3">
          <i className="fa-solid fa-caret-right"></i>&nbsp;{' '}
          <a href="#" onClick={toggleAdditionalOptions}>
            Additional options
          </a>

          { additionalToggled ?
            <div className="mb-3 mt-3" id="additionalTapOptions">
              <div className="mb-3">
                <h4>Tap Location</h4>

                <p className="text-muted">
                  You can define a latitude/longitude position for this tap. Note that this configuration is independent
                  of a floor plan placement that you can make in the tenant tap location settings.
                </p>

                <TapPositionMap latitude={latitude}
                                longitude={longitude}
                                containerHeight={300}
                                editMode={true}
                                setLatitude={setLatitude}
                                setLongitude={setLongitude}
                                toggled={additionalToggled} /* Required to redraw on visibility toggle. */ />

                <div className="mt-2">
                  <strong>Selected Location:</strong> <LatitudeLongitude latitude={latitude} longitude={longitude} skipAccuracy={true} />{' '}
                  (Please click on the map to place the tap)
                </div>
              </div>
            </div> : null
          }
        </div>

        <button className="btn btn-primary" onClick={submit} disabled={!formIsReady()}>
          {submitText}
        </button>
      </form>
  )

}

export default TapPermissionForm;
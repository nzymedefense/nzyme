import React, {useState} from "react";
import LatLonMap from "../../../../shared/LatLonMap";
import LatitudeLongitude from "../../../../shared/LatitudeLongitude";
import LocationSelector from "../../../../shared/locations/LocationSelector";

function TapPermissionForm(props) {

  const onClick = props.onClick;
  const submitText = props.submitText;
  const defaultMapZoomLevel = props.defaultMapZoomLevel;

  const [name, setName] = useState(props.name ? props.name : "");
  const [description, setDescription] = useState(props.description ? props.description : "");

  const [location, setLocation] = useState(props.location ? props.location : "");

  const [longitude, setLongitude] = useState(props.longitude ? props.longitude : null);
  const [latitude, setLatitude] = useState(props.latitude ? props.latitude : null);

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const formIsReady = function() {
    return name && name.trim().length > 0 && description && description.trim().length > 0
  }

  const submit = function(e) {
    e.preventDefault();
    onClick(name, description, location, latitude, longitude);
  }

  const onLocationSelected = (location) => {
    if (!location) {
      setLocation(null);
    } else {
      setLocation(location);
    }
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
          <div className="mb-3 mt-4">
            <div className="mb-3">
              <h3>Tap Location <small>Optional</small></h3>

              <div className="mt-3">
                <h4>Location</h4>

                <LocationSelector location={location} onLocationSelected={onLocationSelected} />
              </div>

              <div className="mt-3">
                <h4>Coordinates</h4>

                <p className="text-muted">
                  Optionally, you can define a latitude/longitude position for this tap. Note that this configuration
                  is independent of a floor plan placement that you can make in the tenant tap location settings.
                </p>

                <LatLonMap latitude={latitude}
                           longitude={longitude}
                           containerHeight={300}
                           editMode={true}
                           setLatitude={setLatitude}
                           setLongitude={setLongitude}
                           defaultZoomLevel={defaultMapZoomLevel} />

                <div className="mt-2">
                  <strong>Selected Location:</strong> <LatitudeLongitude latitude={latitude} longitude={longitude} skipAccuracy={true} />{' '}
                  (Please click on the map to place the tap)
                </div>
              </div>
            </div>
          </div>
        </div>

        <button className="btn btn-primary" onClick={submit} disabled={!formIsReady()}>
          {submitText}
        </button>
      </form>
  )

}

export default TapPermissionForm;
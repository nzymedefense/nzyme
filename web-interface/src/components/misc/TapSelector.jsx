import React, {useEffect, useState} from "react";
import Store from "../../util/Store";
import TapsService from "../../services/TapsService";

const tapsService = new TapsService();

function TapSelector(props) {

  const [availableTaps, setAvailableTaps] = useState(null);
  const [selectedTaps, setSelectedTaps] = useState(null);

  useEffect(() => {
    tapsService.findAllTaps(setAvailableTaps);

    let lsTaps = Store.get("selected_taps");
    if (lsTaps === undefined || lsTaps === null || lsTaps !== Array) {
      lsTaps = [];
    }

    setSelectedTaps(lsTaps);
  }, [])

  const handleTapSelection = function(e, uuid) {
    e.preventDefault();

    const taps = [...selectedTaps];
    if (selectedTaps.includes(uuid)) {
      const idx = taps.indexOf(uuid);
      taps.splice(idx);
      setSelectedTaps(taps);
    } else {
      taps.push(uuid);
      setSelectedTaps(taps);
    }
  }

  const selectAllTaps = function(e) {
    e.preventDefault();
  }

  if (availableTaps === null || selectedTaps === null) {
    return (
        <div className="dropdown">
          <button className="btn btn-outline-secondary dropdown-toggle" type="button" disabled={true}>
            <i className="fa-solid fa-spinner fa-spin-pulse fa-sm"></i> Tap Selector Loading
          </button>
        </div>
    )
  }

  return (
    <React.Fragment>
      <div className="dropdown">
        <button className="btn btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown"
                aria-expanded="false" data-bs-auto-close="outside">
          All Taps Selected
        </button>
        <ul className="dropdown-menu">
          <li>
            <a className="dropdown-item" href="#" onClick={selectAllTaps}>
              Select App Taps
            </a>
          </li>
          <li><hr className="dropdown-divider" /></li>
          <li><h6 className="dropdown-header">Individual Taps</h6></li>
          {availableTaps.map(function(tap, i) {
            return (
                <li key={"tapselector-tap-" + i}>
                  <a className="dropdown-item" href="#" onClick={(e) => handleTapSelection(e, tap.uuid)}>
                    {tap.name}
                  </a>
                </li>
            )
          })}
        </ul>
      </div>
    </React.Fragment>
  )

}

export default TapSelector;
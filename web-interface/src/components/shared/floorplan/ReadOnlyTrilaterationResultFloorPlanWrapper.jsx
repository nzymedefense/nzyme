import React, {useEffect, useState} from "react";
import FloorPlan from "./FloorPlan";
import LoadingSpinner from "../../misc/LoadingSpinner";
import moment from "moment";
import {sanitizeHtml} from "../../../util/Tools";
import Dot11Service from "../../../services/Dot11Service";
import Paginator from "../../misc/Paginator";
import numeral from "numeral";

const dot11Service = new Dot11Service();

function ReadOnlyTrilaterationResultFloorPlanWrapper(props) {

  const data = props.data;
  const error = props.error;
  const onFloorSelected = props.onFloorSelected;

  const perPage = 10;
  const [page, setPage] = useState(1);
  const [floors, setFloors] = useState(null);

  const [floorSelectorToggled, setFloorSelectorToggled] = useState(false);

  useEffect(() => {
    if (data) {
      setFloors(null);
      dot11Service.findAllFloorsOfLocation(data.tenant_location.id, setFloors, perPage, (page - 1) * perPage)
    }
  }, [data, page]);

  const contextText = () => {
    return "Generated at " + moment(data.generated_at).format() +
        "<br /> Location \"" + sanitizeHtml(data.tenant_location.name) + "\"" +
        ", Floor \"" + sanitizeHtml(data.tenant_floor.name) + "\"<br />" +
        "Locating " + sanitizeHtml(data.target_description)
  }

  const onToggleFloorSelector = (e) => {
    e.preventDefault();
    setFloorSelectorToggled(!floorSelectorToggled);
  }

  const selectFloorLink = (floor) => {
    if (floor.id === data.tenant_floor.id) {
      return <span className="text-muted" title="This is the currently selected floor.">n/a</span>
    }

    if (floor.has_floor_plan && floor.tap_count >= 3) {
      return <a href="#" style={{fontWeight: "bold"}} onClick={(e) => {
        e.preventDefault();
        onFloorSelected(data.tenant_location.id, floor.id);
      }}>Select</a>;
    } else {
      return <span className="text-muted" title="You can only select floors that have a floor plan and at least 3 placed taps.">n/a</span>;
    }
  }

  const floorSelector = () => {
    if (!floorSelectorToggled) {
      return null;
    }

    if (!floors) {
      return <LoadingSpinner />
    }

    return (
        <React.Fragment>
          <div className="floorplan-floor-selector mb-3 mt-2">
          <h4>Change Floor</h4>
          <p className="mb-2">Total floors: {floors.count}</p>

          <table className="table table-sm table-hover table-striped">
            <thead>
            <tr>
              <th style={{width: 65}}>Number</th>
              <th>Name</th>
              <th>Plan Uploaded</th>
              <th>Placed Taps</th>
              <th>&nbsp;</th>
            </tr>
            </thead>
            <tbody>
            {floors.floors.map(function (key, i) {
              return (
                  <tr key={i}>
                    <td>{floors.floors[i].number}</td>
                    <td className={floors.floors[i].id === data.tenant_floor.id ? "text-bold" : null}>
                      {floors.floors[i].name}
                      {floors.floors[i].id === data.tenant_floor.id ? " (Current Floor)" : null}
                    </td>
                    <td>
                      {floors.floors[i].has_floor_plan ? <span><i className="fa-solid fa-circle-check text-success"></i>&nbsp; Yes</span> :
                          <span><i className="fa-solid fa-triangle-exclamation text-warning"></i>&nbsp; No</span>
                      }
                    </td>
                    <td>
                      {floors.floors[i].tap_count >= 3 ? <i className="fa-solid fa-circle-check text-success"></i> :
                          <i className="fa-solid fa-triangle-exclamation text-warning"></i>
                      }&nbsp; {numeral(floors.floors[i].tap_count).format("0,0")}
                    </td>
                    <td>{selectFloorLink(floors.floors[i])}</td>
                  </tr>
              )
            })}
            </tbody>
          </table>

          <Paginator itemCount={floors.count} perPage={perPage} setPage={setPage} page={page} />
          </div>
        </React.Fragment>
    )
  }

  if (error) {
    return (
        <React.Fragment>
          <div className="alert alert-info mb-0">
            {error}
            <hr />
            <button className="btn btn-secondary btn-sm" onClick={(e) => {
              e.preventDefault();
              onFloorSelected(null, null);
            }}>Guess Floor
            </button>
          </div>
        </React.Fragment>
    )
  }

  if (!data) {
    return <LoadingSpinner/>
  }

  return (
      <React.Fragment>
      <div className="row">
          <div className="col-md-9">
            <h5>Location: {data.tenant_location.name} / Floor: {data.tenant_floor.name}</h5>
          </div>

          <div className="col-md-3">
            <a href="#" className="btn btn-sm btn-secondary float-end" onClick={onToggleFloorSelector}>
              {floorSelectorToggled ? "Hide Floor Selector" : "Change Floor"}
            </a>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            {floorSelector()}
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <FloorPlan containerHeight={500}
                       floorHasPlan={true}
                       plan={data.plan}
                       taps={data.tenant_floor.tap_positions}
                       positions={data.locations}
                       contextText={contextText()}
                       editModeEnabled={false}/>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <p className="text-muted mt-3 mb-0">
              The trilateration functionality always uses data recorded by all taps on the selected floor and ignores
              manual tap selection. It guesses the initial floor it presents based on the strongest recorded signal
              strengths.
            </p>

            <p className="text-muted mt-3 mb-0">
              The likely closest floor will be shown in case of a floor that has not at least three placed taps with a
              recorded signal within the selected timeframe.
            </p>
          </div>
        </div>
      </React.Fragment>
  )

}

export default ReadOnlyTrilaterationResultFloorPlanWrapper;
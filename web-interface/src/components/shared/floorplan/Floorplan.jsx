import React, {useEffect, useState} from "react";

import 'leaflet/dist/leaflet.css';
import * as L from 'leaflet';
import LoadingSpinner from "../../misc/LoadingSpinner";
import {sanitizeHtml} from "../../../util/Tools";
import moment from "moment";

const yx = L.latLng;

const xy = function(x, y) {
  if (Array.isArray(x)) {
    return yx(x[1], x[0]);
  }
  return yx(y, x);
};

const transientTapIcon = L.icon({
  iconUrl: '/static/leaflet/icon-tap-transient.png',
  iconSize: [24, 24],
  iconAnchor: [16, 16],
  tooltipAnchor: [0, 0]
});

const onlineTapIcon = L.icon({
  iconUrl: '/static/leaflet/icon-tap.png',
  iconSize: [24, 24],
  iconAnchor: [16, 16],
  tooltipAnchor: [0, 0]
});

const offlineTapIcon = L.icon({
  iconUrl: '/static/leaflet/icon-tap-offline.png',
  iconSize: [24, 24],
  iconAnchor: [16, 16],
  tooltipAnchor: [0, 0]
});

function Floorplan(props) {

  const containerHeight = props.containerHeight;
  const floorHasPlan = props.floorHasPlan;
  const plan = props.plan;
  const taps = props.taps;
  const editModeEnabled = props.editModeEnabled;

  // For floor plan management. Can be safely omitted.
  const placedTap = props.placedTap;
  const onTapPlacementComplete = props.onTapPlacementComplete;
  const onRevisionSaved = props.onRevisionSaved;
  const onPlanDeleted = props.onPlanDeleted;

  const [map, setMap] = useState(null);
  const [localRevision, setLocalRevision] = useState(0);
  const [newPositions, setNewPositions] = useState({});

  useEffect(() => {
    const onBeforeUnload = (e) => {
      if (localRevision !== 0) {
        e.preventDefault();
        e.returnValue = "";
      }
    };
    window.addEventListener("beforeunload", onBeforeUnload);
    return () => {
      window.removeEventListener("beforeunload", onBeforeUnload);
    };
  }, [localRevision]);

  useEffect(() => {
    if (plan) {
      const bounds = [[0, 0], [plan.height, plan.width]];

      if (map) {
        // Reset map on reload.
        map.off();
        map.remove();
      }

      setMap(L.map("floorplan", {
        crs: L.CRS.Simple,
        minZoom: -5,
        maxBounds: bounds,
        maxBoundsViscosity: 1.0,
        scrollWheelZoom: false
      }));
    }
  }, [plan]);

  useEffect(() => {
    // Map was (re-) initialized.
    if (map) {
      const bounds = [[0, 0], [plan.height, plan.width]];
      L.imageOverlay("data:image/png;base64," + plan.image_base64, bounds).addTo(map);
      map.fitBounds(bounds);
      map.attributionControl.setPrefix("");
    }
  }, [map]);

  useEffect(() => {
    if (taps && map) {
      taps.forEach((tap) => {
        let iconImage;
        if (tap.active) {
          iconImage = onlineTapIcon;
        } else {
          iconImage = offlineTapIcon;
        }

        const icon = L.marker(xy(tap.y, tap.x), {
          icon: iconImage,
          draggable: editModeEnabled,
          autoPan: true
        }).addTo(map);

        icon.bindTooltip(tapTooltip(tap));

        icon.on("dragend", () => {
          setLocalRevision(prevRev => prevRev + 1)
          tempMarkTapPosition(tap.uuid, icon.getLatLng().lat, icon.getLatLng().lng)
        })
      })
    }
  }, [taps, map]);

  // New tap placed.
  useEffect(() => {
    if (plan && placedTap) {
      const newTap = L.marker(xy(Math.round(plan.width/2), Math.round(plan.height/2)), {
        icon: transientTapIcon,
        draggable: true,
        autoPan: true
      }).addTo(map);

      newTap.bindTooltip(tapTooltip(placedTap));

      newTap.on("dragend", () => {
        setLocalRevision(prevRev => prevRev + 1)
        tempMarkTapPosition(placedTap.uuid, newTap.getLatLng().lat, newTap.getLatLng().lng)
      })

      onTapPlacementComplete();
    }
  }, [placedTap]);

  const tapTooltip = (tap) => {
    return "<span class='floorplan-tooltip-title'>Tap</span><strong>&quot;" + sanitizeHtml(tap.name) + "&quot;</strong> " +
        (tap.active ? "<span class='text-success'>(Online)</span>" : "<span class='text-danger'>(Offline)</span>") +
        "<br/>Last Report: " + (tap.last_report ? moment(tap.last_report).fromNow() : "Never")
  }

  const tempMarkTapPosition = (id, x, y) => {
    setNewPositions(prevPositions => ({
      ...prevPositions,
      [id]: {x: x, y: y}
    }));
  }

  const saveButton = () => {
    if (!editModeEnabled) {
      return null;
    }

    return (
        <button className="btn btn-sm btn-primary" disabled={localRevision === 0}
                onClick={(e) => {
                  e.preventDefault();
                  onRevisionSaved(newPositions);
                }}>
          Save Tap Positions
        </button>
    )
  }

  const deleteButton = () => {
    if (!editModeEnabled) {
      return null;
    }

    return (
        <button className="btn btn-sm btn-danger"
                onClick={(e) => {
                  e.preventDefault();
                  onPlanDeleted();
                }}>
          Delete Floor Plan
        </button>
    )
  }

  if (!floorHasPlan) {
    return (
        <div className="alert alert-info mb-0">
          No floor plan uploaded for this floor.
        </div>
    )
  }

  if (!plan) {
    return <LoadingSpinner/>
  }

  return (
      <React.Fragment>
        <div id="floorplan" style={{height: containerHeight, backgroundColor: "#FFFFFF"}}/>

        {saveButton()} {deleteButton()}
      </React.Fragment>
  )

}

export default Floorplan;
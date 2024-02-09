import React, {useEffect, useState} from "react";

import 'leaflet/dist/leaflet.css';
import * as L from 'leaflet';
import LoadingSpinner from "../../misc/LoadingSpinner";
import {sanitizeHtml} from "../../../util/Tools";

const yx = L.latLng;

const xy = function(x, y) {
  if (Array.isArray(x)) {
    return yx(x[1], x[0]);
  }
  return yx(y, x);
};

const transientTapIcon = L.icon({
  iconUrl: '/static/leaflet/icon-tap-transient.png',
  iconSize: [16, 16],
  iconAnchor: [8, 8],
  tooltipAnchor: [0, 0],
});

const existingTapIcon = L.icon({
  iconUrl: '/static/leaflet/icon-tap.png',
  iconSize: [16, 16],
  iconAnchor: [8, 8],
  tooltipAnchor: [0, 0],
});

function Floorplan(props) {

  const containerHeight = props.containerHeight;
  const floorHasPlan = props.floorHasPlan;
  const plan = props.plan;
  const taps = props.taps;

  // For floor plan management. Can be safely omitted.
  const placedTap = props.placedTap;
  const onTapPlacementComplete = props.onTapPlacementComplete;
  const onRevisionSaved = props.onRevisionSaved;

  const [map, setMap] = useState(null);
  const [localRevision, setLocalRevision] = useState(0);
  const [newPositions, setNewPositions] = useState({});

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
      taps.map((tap) => {
        const icon = L.marker(xy(tap.y, tap.x), {
          icon: existingTapIcon,
          draggable: true,
          autoPan: true}
        ).addTo(map);

        icon.bindTooltip("Tap &quot;" + sanitizeHtml(tap.name) + "&quot;");

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
        autoPan: true}
      ).addTo(map);

      newTap.bindTooltip("Tap &quot;" + sanitizeHtml(placedTap.name) + "&quot;");

      newTap.on("dragend", () => {
        setLocalRevision(prevRev => prevRev + 1)
        tempMarkTapPosition(placedTap.uuid, newTap.getLatLng().lat, newTap.getLatLng().lng)
      })

      onTapPlacementComplete();
    }
  }, [placedTap]);

  const tempMarkTapPosition = (id, x, y) => {
    setNewPositions(prevPositions => ({
      ...prevPositions,
      [id]: {x: x, y: y}
    }));
  }

  if (!floorHasPlan) {
    return (
        <div className="alert alert-info mb-0">
          No floor plan uploaded for this floor.
        </div>
    )
  }

  if (!plan) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div id="floorplan" style={{height: containerHeight, backgroundColor: "#FFFFFF"}}/>

        <button className="btn btn-sm btn-primary" disabled={localRevision === 0}
                onClick={(e) => {e.preventDefault(); onRevisionSaved(newPositions); }}>
          Save Tap Positions
        </button>
      </React.Fragment>
  )

}

export default Floorplan;
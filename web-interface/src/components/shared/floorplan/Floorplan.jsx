import React, {useEffect, useState} from "react";

import 'leaflet/dist/leaflet.css';
import * as L from 'leaflet';
import LoadingSpinner from "../../misc/LoadingSpinner";

const yx = L.latLng;

const xy = function(x, y) {
  if (Array.isArray(x)) {
    return yx(x[1], x[0]);
  }
  return yx(y, x);
};

function Floorplan(props) {

  const floorHasPlan = props.floorHasPlan;
  const plan = props.plan;

  const placedTap = props.placedTap;
  const onTapPlacementComplete = props.onTapPlacementComplete;

  const [map, setMap] = useState(null);

  const containerHeight = props.containerHeight;

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

  // New tap placed.
  useEffect(() => {
    if (plan && placedTap) {
      const tapIcon = L.icon({
        iconUrl: '/static/leaflet/icon-tap.png',
        iconSize: [16, 16],
        iconAnchor: [8, 8],
        tooltipAnchor: [0, 0],
      });

      const newTap = L.marker(xy(Math.round(plan.width/2), Math.round(plan.height/2)), {
        icon: tapIcon,
        draggable: true,
        autoPan: true}
      ).addTo(map);

      newTap.bindTooltip(placedTap.name);

      newTap.on("dragend", () => {
        console.log(newTap.getLatLng());
      })

      onTapPlacementComplete();
    }
  }, [placedTap]);

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
        <div id="floorplan" style={{height: containerHeight, backgroundColor: "#FFFFFF"}} />
      </React.Fragment>
  )

}

export default Floorplan;
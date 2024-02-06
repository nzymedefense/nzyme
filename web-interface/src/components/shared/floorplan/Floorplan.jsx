import React, {useEffect} from "react";

import 'leaflet/dist/leaflet.css';
import * as L from 'leaflet';

const yx = L.latLng;

const xy = function(x, y) {
  if (Array.isArray(x)) {
    return yx(x[1], x[0]);
  }
  return yx(y, x);
};

function Floorplan(props) {

  const plan = props.plan;
  const planWidth = props.planWidth;
  const planHeight = props.planHeight;

  const containerHeight = props.containerHeight;

  useEffect(() => {
    if (plan) {
      const bounds = [[0, 0], [planHeight, planWidth]];

      const map = L.map("floorplan", {
        crs: L.CRS.Simple,
        minZoom: -5,
        maxBounds: bounds,
        maxBoundsViscosity: 1.0,
        scrollWheelZoom: false
      });
      const image = L.imageOverlay("/static/floorplan_test.png", bounds)
          .addTo(map);
      map.fitBounds(bounds);

      map.attributionControl.setPrefix("");

      // Markers.
      const tapIcon = L.icon({
        iconUrl: '/static/leaflet/icon-tap.png',
        iconSize: [16, 16],
        iconAnchor: [8, 8],
        tooltipAnchor: [0, 0],
      });
      const tap1 = L.marker(xy(130, 800), {icon: tapIcon, draggable: true, autoPan: true}).addTo(map);
      tap1.bindTooltip("Sensor 1");

      tap1.on("dragend", () => {
        console.log(tap1.getLatLng());
      })
    }
  }, []);

  if (!plan) {
    return (
        <div className="alert alert-info mb-0">
          No floor plan uploaded for this floor.
        </div>
    )
  }

  return (
      <React.Fragment>
        <div id="floorplan" style={{height: containerHeight, backgroundColor: "#FFFFFF"}} />
      </React.Fragment>
  )

}

export default Floorplan;
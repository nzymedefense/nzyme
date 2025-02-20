import React, {useEffect, useState} from "react";

import 'leaflet/dist/leaflet.css';
import * as L from 'leaflet';
import '../../../lib/Control.FullScreen';
import '../../../lib/Control.FullScreen.css';

export default function UavMap(props) {

  const uav = props.uav;
  const containerHeight = props.containerHeight;
  const id = props.id;

  const [map, setMap] = useState(null);
  const [mapInitialized, setMapInitialized] = useState(false);

  useEffect(() => {
    if (uav && uav.summary.latitude && uav.summary.longitude && !mapInitialized) {
      setMap(L.map(id,{
        scrollWheelZoom: false,
        fullscreenControl: true,
        fullscreenControlOptions: {
          position: "topleft"
        }
      }));

      setMapInitialized(true);
    }
  }, [id, uav]);

  useEffect(() => {
    if (mapInitialized) {
      map.attributionControl.setPrefix("");
      map.setView([uav.summary.latitude, uav.summary.longitude], 16)

      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
      }).addTo(map);
    }
  }, [mapInitialized])

  if (!uav || !uav.summary.latitude || !uav.summary.longitude) {
    return <div className="alert alert-info mb-0">No Location Data available.</div>
  }

  return (
    <React.Fragment>
      <div id={props.id} style={{height: containerHeight}}/>
    </React.Fragment>
  )

}
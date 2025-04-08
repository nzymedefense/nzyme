import React, {useEffect, useState} from "react";

import 'leaflet/dist/leaflet.css';
import * as L from 'leaflet';
import '../../../lib/Control.FullScreen';
import '../../../lib/Control.FullScreen.css';
import '../../../lib/easy-button';
import '../../../lib/easy-button.css';
import {latLng} from "leaflet/src/geo";
import moment from "moment/moment";

export default function UavMap(props) {

  const uav = props.uav;
  const containerHeight = props.containerHeight;

  const lastKnownUavPosition = props.lastKnownUavPosition;

  // Optional.
  const onRefresh = props.onRefresh;
  const lastKnownOperatorPosition = props.lastKnownOperatorPosition;

  const [map, setMap] = useState(null);
  const [mapInitialized, setMapInitialized] = useState(false);

  const uavIcon = L.icon({
    iconUrl: window.appConfig.assetsUri + 'static/uav/uav.png',
    iconSize: [24, 24],
    iconAnchor: [12, 12],
    tooltipAnchor: [0, 0]
  });

  const operatorIcon = L.icon({
    iconUrl: window.appConfig.assetsUri + 'static/uav/operator.png',
    iconSize: [24, 24],
    iconAnchor: [12, 12],
    tooltipAnchor: [0, 0]
  });

  const lastKnownUavPositionTooltip = (pos) => {
    return "Last known UAV position at " + moment(pos.timestamp).format() + " (" +  moment(pos.timestamp).fromNow() + ")";
  }

  const lastKnownOperatorPositionTooltip = (pos) => {
    return "Last known operator position at " + moment(pos.timestamp).format() + " (" +  moment(pos.timestamp).fromNow() + ")";
  }

  useEffect(() => {
    if (uav && uav.summary.latitude && uav.summary.longitude && !mapInitialized) {
      setMap(L.map("uav-map", {
        scrollWheelZoom: false,
        fullscreenControl: true,
        fullscreenControlOptions: {
          position: "topleft"
        }
      }));

      setMapInitialized(true);
    }
  }, [uav]);

  useEffect(() => {
    if (mapInitialized) {
      map.invalidateSize(true);
      map.attributionControl.setPrefix("");
      map.setView([uav.summary.latitude, uav.summary.longitude], 16)

      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
      }).addTo(map);

      if (onRefresh) {
        L.easyButton({
          states: [{
            stateName: "refresh",
            icon: '<i class="fa-solid fa-refresh"></i>',
            title: "Refresh",
            onClick: onRefresh
          }]
        }).addTo(map);
      }

      if (lastKnownOperatorPosition) {
        const lastKnownOperatorPositionIcon = L.marker(latLng(lastKnownOperatorPosition.lat, lastKnownOperatorPosition.lon), {
          icon: operatorIcon,
          draggable: false,
          autoPan: true
        }).addTo(map);

        lastKnownOperatorPositionIcon.bindTooltip(lastKnownOperatorPositionTooltip(lastKnownOperatorPosition))
      }

      const lastKnownUavPositionIcon = L.marker(latLng(lastKnownUavPosition.lat, lastKnownUavPosition.lon), {
        icon: uavIcon,
        draggable: false,
        autoPan: true
      }).addTo(map);

      lastKnownUavPositionIcon.bindTooltip(lastKnownUavPositionTooltip(lastKnownUavPosition))
    }
  }, [mapInitialized])

  if (!uav || !uav.summary.latitude || !uav.summary.longitude) {
    return <div className="alert alert-info mb-0">No Location Data available.</div>
  }

  return (
    <React.Fragment>
      <div id="uav-map" style={{height: containerHeight}}/>
    </React.Fragment>
  )

}
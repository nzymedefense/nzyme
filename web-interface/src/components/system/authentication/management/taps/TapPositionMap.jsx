import React, {useEffect, useState} from 'react';

import 'leaflet/dist/leaflet.css';
import * as L from 'leaflet';
import {latLng} from "leaflet/src/geo";

export default function TapPositionMap(props) {

  const containerHeight = props.containerHeight;
  const toggled = props.toggled; // Required to redraw on visibility toggle.
  const editMode = props.editMode;

  // Optional.
  const setLatitude = props.setLatitude;
  const setLongitude = props.setLongitude;
  const latitude = props.latitude;
  const longitude = props.longitude;

  const [map, setMap] = useState(null);
  const [zoomLevel, setZoomLevel] = useState(9);
  const tapIcon = L.icon({
    iconUrl: window.appConfig.assetsUri + 'static/leaflet/icon-tap.png',
    iconSize: [24, 16],
    iconAnchor: [12, 8],
    tooltipAnchor: [0, 0]
  });

  const placeMarker = (latlng) => {
    map.eachLayer(function (layer) {
      if (layer.options.nzymeType === "tap-marker") {
        layer.remove();
      }
    });

    L.marker(latlng, {
      nzymeType: "tap-marker",
      icon: tapIcon,
      draggable: false,
      autoPan: true
    }).addTo(map);

    setLatitude(latlng.lat);
    setLongitude(latlng.lng);
  }

  useEffect(() => {
    if (!map) {
      setMap(L.map("tap-location-map", {
        scrollWheelZoom: false
      }));
    }
  }, []);

  useEffect(() => {
    if (map) {
      map.attributionControl.setPrefix("");

      if (latitude && longitude) {
        map.setView(latLng(latitude, longitude), zoomLevel)
        placeMarker(latLng(latitude, longitude));
      } else {
        map.setView([29.73422, -95.38231], zoomLevel)
      }

      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
      }).addTo(map);

      map.on('zoomend', function() {
        setZoomLevel(map.getZoom());
      });

      if (editMode) {
        map.on("click", (e) => placeMarker(e.latlng));
      }
    }
  }, [map, latitude, longitude])

  useEffect(() => {
    if (map) {
      map.invalidateSize(false);
    }
  }, [toggled]);

  return (
      <React.Fragment>
        <div id="tap-location-map" style={{height: containerHeight}}/>
      </React.Fragment>
  )

}
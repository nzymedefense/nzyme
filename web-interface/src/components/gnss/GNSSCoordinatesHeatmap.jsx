import React, {useEffect, useState} from "react";
import {latLng} from "leaflet/src/geo";

// TODO SHOW ALL SELECTED TAPS AND THEIR POSITIONS, MAX DISTANCE BETWEEN COORDS, HEATMAP CONTROLS, REACT TO REVISION

export default function GNSSCoordinatesHeatmap(props) {

  const containerHeight = props.containerHeight;
  const coordinates = props.coordinates;

  // Optional.
  const latitude = props.latitude;
  const longitude = props.longitude;
  const defaultZoomLevel = props.defaultZoomLevel;

  const [map, setMap] = useState(null);
  const [zoomLevel, setZoomLevel] = useState(defaultZoomLevel ? defaultZoomLevel : 16);

  function getDefaultCenter(coords) {
    if (!coords || coords.length === 0) {
      return [0,0];
    }

    const n = coords.length;
    if (n === 0) return null;

    const { sumLat, sumLon } = coords.reduce(
      (acc, { lat, lon }) => {
        acc.sumLat += lat;
        acc.sumLon += lon;
        return acc;
      },
      { sumLat: 0, sumLon: 0 }
    );

    return [ sumLat / n, sumLon / n ];
  }

  useEffect(() => {
    if (!map) {
      setMap(L.map("gnss-coordinates-heatmap", {
        scrollWheelZoom: false
      }));
    }
  }, []);

  useEffect(() => {
    if (map) {
      map.attributionControl.setPrefix("");

      if (latitude && longitude) {
        map.setView(latLng(latitude, longitude), zoomLevel)
      } else {
        if (coordinates) {
          map.setView(getDefaultCenter(coordinates), zoomLevel)
        }
      }

      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
      }).addTo(map);

      map.on('zoomend', function() {
        setZoomLevel(map.getZoom());
      });
    }
  }, [map, latitude, longitude])

  // Instant positions.
  useEffect(() => {
    if (map) {
      map.setView(getDefaultCenter(coordinates), zoomLevel)

      // Remove all previous positions.
      map.eachLayer(function (layer) {
        if (layer.options.nzymeType === "heatmap-marker") {
          layer.remove();
        }
      });

      // Heatmap data.
      if (coordinates) {
        const points = coordinates.map(({ lat, lon }) => [lat, lon, 1]);

        L.heatLayer(points, {
          nzymeType: "heatmap-marker",
          radius: 20,
          opacity: 1,
          maxZoom: 0,
          blur: 15,
          max: 1.0
        }).addTo(map);
      }
    }
  }, [coordinates, map])

  return (
    <React.Fragment>
      <div id="gnss-coordinates-heatmap" style={{height: containerHeight}}/>
    </React.Fragment>
  )

}
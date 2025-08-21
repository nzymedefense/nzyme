import React, {useEffect, useState} from "react";
import {latLng} from "leaflet/src/geo";
import {sanitizeHtml} from "../../util/Tools";
import '../../../lib/Control.FullScreen';
import '../../../lib/Control.FullScreen.css';

export default function GNSSCoordinatesHeatmap(props) {

  const containerHeight = props.containerHeight;
  const coordinates = props.coordinates;

  // Optional.
  const latitude = props.latitude;
  const longitude = props.longitude;
  const defaultZoomLevel = props.defaultZoomLevel;

  const [map, setMap] = useState(null);
  const [zoomLevel, setZoomLevel] = useState(defaultZoomLevel ? defaultZoomLevel : 16);

  const onlineTapIcon = L.icon({
    iconUrl: window.appConfig.assetsUri + 'static/leaflet/icon-tap.png',
    iconSize: [24, 16],
    iconAnchor: [12, 8],
    tooltipAnchor: [0, 0]
  });

  const offlineTapIcon = L.icon({
    iconUrl: window.appConfig.assetsUri + 'static/leaflet/icon-tap-offline.png',
    iconSize: [24, 16],
    iconAnchor: [12, 8],
    tooltipAnchor: [0, 0]
  });

  function getDefaultCenter(coords) {
    if (!coords || !coords.coordinates || coords.coordinates.length === 0) {
      return [0,0];
    }

    const n = coords.coordinates.length;
    if (n === 0) return null;

    const { sumLat, sumLon } = coords.coordinates.reduce(
      (acc, { lat, lon }) => {
        acc.sumLat += lat;
        acc.sumLon += lon;
        return acc;
      },
      { sumLat: 0, sumLon: 0 }
    );

    return [ sumLat / n, sumLon / n ];
  }

  const tapTooltip = (tap) => {
    return "Tap &quot;" + sanitizeHtml(tap.name) + "&quot; " +
      (tap.active ? "<span class='text-success'>(Online)</span>" : "<span class='text-danger'>(Offline)</span>")
  }

  useEffect(() => {
    if (!map) {
      setMap(L.map("gnss-coordinates-heatmap", {
        scrollWheelZoom: false,
        fullscreenControl: true,
        fullscreenControlOptions: {
          position: "topleft"
        }
      }));
    }
  }, []);

  useEffect(() => {
    if (map) {
      map.attributionControl.setPrefix("");

      if (latitude && longitude) {
        map.setView(latLng(latitude, longitude), zoomLevel)
      } else {
        if (coordinates && coordinates.coordinates) {
          map.setView(getDefaultCenter(coordinates.coordinates), zoomLevel)
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

  // Coordinates.
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
      if (coordinates && coordinates.coordinates) {
        const points = coordinates.coordinates.map(({ lat, lon }) => [lat, lon, 1]);

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

  // Render taps.
  useEffect(() => {
    if (coordinates && coordinates.tap_locations && map) {
      coordinates.tap_locations.forEach((tap) => {
        let iconImage;
        if (tap.active) {
          iconImage = onlineTapIcon;
        } else {
          iconImage = offlineTapIcon;
        }

        const icon = L.marker(latLng(tap.lat, tap.lon), {
          nzymeType: "tap-marker",
          icon: iconImage
        }).addTo(map);

        icon.bindTooltip(tapTooltip(tap));
      })
    }
  }, [coordinates, map]);

  return (
    <React.Fragment>
      <div id="gnss-coordinates-heatmap" style={{height: containerHeight}}/>
    </React.Fragment>
  )

}
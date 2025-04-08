import React, {useEffect, useState} from "react";

import 'leaflet/dist/leaflet.css';
import * as L from 'leaflet';
import '../../../lib/Control.FullScreen';
import '../../../lib/Control.FullScreen.css';
import '../../../lib/easy-button';
import '../../../lib/easy-button.css';
import {latLng} from "leaflet/src/geo";
import moment from "moment/moment";
import numeral from "numeral";
import {metersToFeet, uavHorizontalAccuracyNoHtml, uavVerticalAccuracyNoHtml} from "../../util/Tools";
import UavVerticalAccuracy from "./util/UavVerticalAccuracy";

export default function UavMap(props) {

  const uav = props.uav;
  const containerHeight = props.containerHeight;
  const plottedTrack = props.plottedTrack;
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

  const vectorPositionIcon = L.icon({
    iconUrl: window.appConfig.assetsUri + 'static/uav/vector_position.png',
    iconSize: [8, 8],
    iconAnchor: [4, 4],
    tooltipAnchor: [0, 0]
  });

  const lastKnownUavPositionTooltip = (pos) => {
    // Watch for XSS here.
    return "Last known UAV position at " + moment(pos.timestamp).format() + " (" +  moment(pos.timestamp).fromNow() + ")";
  }

  const lastKnownOperatorPositionTooltip = (pos) => {
    // Watch for XSS here.
    return "Last known operator position at " + moment(pos.timestamp).format() + " (" +  moment(pos.timestamp).fromNow() + ")";
  }

  const vectorPositionTooltip = (v) => {
    // Watch for XSS here.
    let tt = "Vector at " + moment(v.timestamp).format() + " (" +  moment(v.timestamp).fromNow() + ") <br />";

    if (v.accuracy_horizontal) {
      tt += "Horizontal Accuracy: " + uavHorizontalAccuracyNoHtml(v.accuracy_horizontal) + "<br />";
    }

    if (v.operational_status) {
      tt += "Operational Status: " + v.operational_status + "<br \>";
    }

    if (v.speed !== null) {
      tt += "Speed: " + v.speed + " ft/s<br \>";
    }

    if (v.vertical_speed !== null) {
      tt += "Vertical Speed: " + v.vertical_speed + " ft/s<br \>";
    }

    if (v.ground_track !== null) {
      tt += "Ground Track: " + v.ground_track + "&deg;<br \>";
    }

    // We prefer to show height if we have it.
    if (v.height !== null && v.height !== undefined && v.height !== 0) {
      if (v.height_type === "AboveGround") {
        tt += "Height: " + numeral(metersToFeet(v.height)).format("0,0") + " ft AGL ("
            + uavVerticalAccuracyNoHtml(v.accuracy_vertical) + " accuracy)<br/>";
      } else if (v.height_type === "AboveTakeoffLocation") {
        tt += "Height: " + numeral(metersToFeet(v.height)).format("0,0") + " ft ATL ("
            + uavVerticalAccuracyNoHtml(v.accuracy_vertical) + " accuracy)<br/>";
      }
    }

    // Pressure altitude is the next most useful info if there is no height available.
    if (v.altitude_pressure !== null && v.altitude_pressure !== undefined && v.altitude_pressure !== 0) {
      tt += "Height: " + numeral(metersToFeet(v.altitude_pressure)).format("0,0") + " ft PA ("
          + uavVerticalAccuracyNoHtml(v.accuracy_barometer) + " accuracy)<br/>";
    }

    // Geodetic altitude is the fallback.
    if (v.altitude_geodetic !== null && v.altitude_geodetic !== undefined && v.altitude_geodetic !== 0) {
      tt += "Height: " + numeral(metersToFeet(v.altitude_geodetic)).format("0,0") + " ft GA ("
          + uavVerticalAccuracyNoHtml(v.accuracy_vertical) + " accuracy)<br/>";
    }

    return tt;
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

  useEffect(() => {
    if (mapInitialized && plottedTrack) {
      const trackLatLngs = [];
      plottedTrack.vectors.forEach(vector => {
        trackLatLngs.push([vector.latitude, vector.longitude]);

        const pos = L.marker(latLng(vector.latitude, vector.longitude), {
          icon: vectorPositionIcon,
          draggable: false,
          autoPan: true
        }).addTo(map);

        pos.bindTooltip(vectorPositionTooltip(vector));
      })

      var track = L.polyline(trackLatLngs, {color: "#1d30d7"})
          .addTo(map);
      map.fitBounds(track.getBounds());
    }
  }, [plottedTrack, mapInitialized]);

  if (!uav || !uav.summary.latitude || !uav.summary.longitude) {
    return <div className="alert alert-info mb-0">No Location Data available.</div>
  }

  return (
    <React.Fragment>
      <div id="uav-map" style={{height: containerHeight}}/>
    </React.Fragment>
  )

}
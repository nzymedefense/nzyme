import React, {useEffect, useState} from "react";

import 'leaflet/dist/leaflet.css';
import * as L from 'leaflet';
import '../../../../lib/Control.FullScreen';
import '../../../../lib/Control.FullScreen.css';
import '../../../../lib/leaflet-messagebox';
import '../../../../lib/leaflet-messagebox.css';
import '../../../../lib/leaflet-slider';
import '../../../../lib/leaflet-slider.css';
import '../../../../lib/easy-button';
import '../../../../lib/easy-button.css';
import "../../../../lib/leaflet-heat"
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

function FloorPlan(props) {

  // Always required.
  const containerHeight = props.containerHeight;
  const floorHasPlan = props.floorHasPlan;
  const plan = props.plan;
  const editModeEnabled = props.editModeEnabled;

  // Placements, Optional.
  const taps = props.taps;
  const positions = props.positions;

  // Additional info. Optional.
  const contextText = props.contextText;

  // Reload hook. Optional.
  const onRefresh = props.onRefresh;

  // For floor plan management. Optional.
  const placedTap = props.placedTap;
  const onTapPlacementComplete = props.onTapPlacementComplete;
  const onRevisionSaved = props.onRevisionSaved;
  const onPlanDeleted = props.onPlanDeleted;

  const [map, setMap] = useState(null);
  const [localRevision, setLocalRevision] = useState(0);
  const [newPositions, setNewPositions] = useState({});

  const [showTaps, setShowTaps] = useState(false);

  const [heatmapIntensitySlider, setHeatmapIntensitySlider] = useState(null);
  const [heatmapIntensity, setHeatmapIntensity] = useState(0.7);

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
      const bounds = [[0, 0], [plan.length_pixels, plan.width_pixels]];

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
        scrollWheelZoom: false,
        fullscreenControl: true,
        fullscreenControlOptions: {
          position: "topleft"
        }
      }));
    }
  }, [plan]);

  useEffect(() => {
    // Map was (re-) initialized.
    if (map) {
      const bounds = [[0, 0], [plan.length_pixels, plan.width_pixels]];
      L.imageOverlay("data:image/png;base64," + plan.image_base64, bounds).addTo(map);
      map.fitBounds(bounds);
      map.attributionControl.setPrefix("");

      if (!editModeEnabled) {
        if (contextText) {
          const box = L.control.messagebox({timeout: 2147483647, position: "bottomleft"}).addTo(map);
          box.show(contextText);
        }

        if (onRefresh) {
          L.easyButton({
            states: [{
              stateName: "refresh",
              icon: '<i class="fa-solid fa-refresh"></i>',
              title: "Refresh",
              onClick: function (btn, map) {
                onRefresh();
              }
            }]
          }).addTo(map);
        }

        L.easyButton({
          states: [{
            stateName: "show-taps",
            icon: '<i class="fa-solid fa-server"></i>',
            title: "Show Taps",
            onClick: function (btn, map) {
              setShowTaps(true);
              btn.state("hide-taps");
            }
          }, {
            stateName: "hide-taps",
            icon: '<i class="fa-solid fa-ban"></i>',
            title: "Hide Taps",
            onClick: function (btn, map) {
              setShowTaps(false);
              btn.state("show-taps");
            }
          }]
        }).addTo(map);
      }
    }
  }, [map]);

  useEffect(() => {
    if (map && !editModeEnabled) {
      if (heatmapIntensitySlider) {
        map.removeControl(heatmapIntensitySlider);
      }

      const slider = L.control.slider(function(value) {
        setHeatmapIntensity(value);
      }, {
        max: 1,
        min: 0,
        value: heatmapIntensity,
        step: 0.1,
        showValue: false,
        title: "Heatmap Intensity",
        size: "250px",
        orientation: "horizontal",
        id: "heatmap-intensity-slider",
        logo: '<i class="fa-solid fa-fill-drip"></i>',
        position: "topleft"
      }).addTo(map);

      setHeatmapIntensitySlider(slider);
    }
  }, [map, heatmapIntensity]);

  // Render taps.
  useEffect(() => {
    if (taps && map && (showTaps || editModeEnabled)) {
      taps.forEach((tap) => {
        let iconImage;
        if (tap.active) {
          iconImage = onlineTapIcon;
        } else {
          iconImage = offlineTapIcon;
        }

        const icon = L.marker(xy(tap.y, tap.x), {
          nzymeType: "tap-marker",
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
  }, [taps, map, showTaps]);

  // Remove taps on hide event.
  useEffect(() => {
    if (map && !editModeEnabled && !showTaps) {
      // Remove all previous positions.
      map.eachLayer(function (layer) {
        if (layer.options.nzymeType === "tap-marker") {
          layer.remove();
        }
      });
    }
  }, [map, showTaps]);

  // New tap placed.
  useEffect(() => {
    if (plan && placedTap) {
      const newTap = L.marker(xy(Math.round(plan.width_pixels/2), Math.round(plan.length_pixels/2)), {
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

  // Instant positions.
  useEffect(() => {
    if (map) {
      // Remove all previous positions.
      map.eachLayer(function (layer) {
        if (layer.options.nzymeType === "heatmap-marker") {
          layer.remove();
        }
      });

      // Heatmap data.
      if (positions) {
        const heatmapData = [];
        Object.keys(positions).forEach((bucket) => {
          const position = positions[bucket];
          heatmapData.push([position.x, position.y, heatmapIntensity])
        })

        L.heatLayer(heatmapData, {
          nzymeType: "heatmap-marker",
          radius: 20,
          opacity: 1,
          maxZoom: 0,
          blur: 15,
          max: 1.0
        }).addTo(map);
      }
    }
  }, [positions, map, heatmapIntensity])

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

export default FloorPlan;
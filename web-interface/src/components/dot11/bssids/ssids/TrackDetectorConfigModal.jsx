import React, {useState} from "react";
import Dot11Service from "../../../../services/Dot11Service";
import {notify} from "react-notify-toast";
import TrackDetectorConfigModalButton from "./TrackDetectorConfigModalButton";

const dot11Service = new Dot11Service();

function TrackDetectorConfigModal(props) {

  const bssid = props.bssid;
  const ssid = props.ssid;
  const frequency = props.frequency;
  const tapUUID = props.tapUUID;
  const config = props.config;

  const setRevision = props.setRevision;

  const [formSubmitted, setFormSubmitted] = useState(false);

  const [frameThreshold, setFrameThreshold] = useState(config.frame_threshold);
  const [gapThreshold, setGapThreshold] = useState(config.gap_threshold);
  const [centerlineJitter, setCenterlineJitter] = useState(config.signal_centerline_jitter);

  const updateConfig = (e) => {
    e.preventDefault();

    if (!confirm("Really update track detector configuration?")) {
      return;
    }

    dot11Service.updateTrackDetectorConfig(
        bssid, ssid, frequency, tapUUID, frameThreshold, gapThreshold, centerlineJitter, () => {
          notify.show("Track detector configuration updated.", "success");
          setFormSubmitted(true);
        }
    );
  }

  const onSuccessModalClose = (e) => {
    e.preventDefault();
    setRevision(prevRev => prevRev + 1);
  }

  const formReady = () => {
    return parseInt(frameThreshold, 10) > 0
        && parseInt(gapThreshold, 10) > 0
        && parseInt(centerlineJitter, 10) > 0;
  }

  return (
      <div className="modal fade"
           id="track-detector-config"
           tabIndex="-1">
        <div className="modal-dialog">
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5">Track Detector Configuration</h1>
              <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div className="modal-body">
              <p>
                The default track detector config is not always able to reliably detect signal tracks in all
                environments. You can change the detector configuration for this specific BSSID, SSID and channel
                combination.
              </p>

              <p>
                Note that custom configurations are applied for all tenants of your organization. You can learn more
                about signal track configurations in the{' '}
                <a href="https://go.nzyme.org/wifi-network-monitoring-signal-tracks" target="_blank">documentation</a>.
              </p>

              <div className="mb-3">
                <label htmlFor="wf_config_frame_threshold" className="form-label">Frame Threshold</label>
                <input type="text"
                       className="form-control"
                       id="wf_config_frame_threshold"
                       onChange={(e) => setFrameThreshold(e.target.value)}
                       value={frameThreshold} />
                <div id="wf_config_frame_threshold_help" className="form-text">
                  Minimum number of minutes with successively recorded frames required to start a new track.{' '}
                  <strong>Default:</strong> {config.frame_threshold_default}
                </div>
              </div>
              <div className="mb-3">
                <label htmlFor="wf_config_gap_threshold" className="form-label">Gap Threshold</label>
                <input type="text"
                       className="form-control"
                       id="wf_config_gap_threshold"
                       onChange={(e) => setGapThreshold(e.target.value)}
                       value={gapThreshold} />
                <div id="wf_config_gap_threshold_help" className="form-text">
                  Maximum number of minutes with no recorded frames allowed before a track ends.{' '}
                  <strong>Default:</strong> {config.gap_threshold_default}
                </div>
              </div>
              <div className="mb-3">
                <label htmlFor="wf_config_jitter_threshold" className="form-label">Signal Centerline Jitter</label>
                <input type="text"
                       className="form-control"
                       id="wf_config_jitter_threshold"
                       onChange={(e) => setCenterlineJitter(e.target.value)}
                       value={centerlineJitter}/>
                <div id="wf_config_jitter_threshold_help" className="form-text">
                  Maximum dBm of strength a signal is allowed to deviate from the track centerline. Think of this like
                  the <i>width</i> of the track. Signals outside of the allowed jitter range will not break a track,
                  but also not taken into account for track calculation.{' '}
                  <strong>Default:</strong> {config.signal_centerline_jitter_default}
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <TrackDetectorConfigModalButton formSubmitted={formSubmitted}
                                              onSuccessModalClose={onSuccessModalClose}
                                              onSubmit={updateConfig}
                                              disabled={!formReady()} />

            </div>
          </div>
        </div>
      </div>
  )

}

export default TrackDetectorConfigModal;
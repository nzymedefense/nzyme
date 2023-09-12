import React, {useState} from "react";

function TrackDetectorConfigModal(props) {

  const config = props.config;

  const [frameThreshold, setFrameThreshold] = useState(config.frame_threshold);
  const [gapThreshold, setGapThreshold] = useState(config.gap_threshold);
  const [centerlineJitter, setCenterlineJitter] = useState(config.signal_centerline_jitter);

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
              <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Close</button>
              <button type="button" className="btn btn-primary">Save changes</button>
            </div>
          </div>
        </div>
      </div>
  )

}

export default TrackDetectorConfigModal;
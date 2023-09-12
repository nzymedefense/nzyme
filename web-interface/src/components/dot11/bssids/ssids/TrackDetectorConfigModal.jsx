import React from "react";

function TrackDetectorConfigModal(props) {

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
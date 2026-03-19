import React, {useContext} from "react";
import AppliedFilterList from "../AppliedFilterList";
import {TapContext} from "../../../../App";

export default function SaveFilterAsMonitorDialog({filters, onSave, onClose}) {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  return (
    <React.Fragment>
      <div className="modal-backdrop fade show"></div>
      <div className="modal fade show" style={{display: "block"}}>
        <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable">
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5">Save Filter as Monitor</h1>
              <button type="button" className="btn-close" onClick={onClose}></button>
            </div>
            <div className="modal-body">
              <p>
                A <em>monitor</em> is a search that runs automatically on a schedule using your selected filters. If
                the number of results exceeds the configured threshold, it triggers a detection event.
              </p>

              <h4>Monitor Details</h4>

              <div className="mb-3">
                <label htmlFor="monitor-name" className="form-label">Name</label>
                <input type="text" className="form-control" id="monitor-rule-name" />
                <div className="form-text">
                  The name of this monitor. Give it a name that helps to quickly identify what it is supposed to
                  alert on.
                </div>
              </div>

              <div className="mb-3">
                <label htmlFor="monitor-description" className="form-label">Description <small>Optional</small></label>
                <textarea className="form-control" id="description" />
                <div className="form-text">An optional description that provides more detail.</div>
              </div>

              <h4>Taps</h4>
              
              {JSON.stringify(selectedTaps)}

              <h4>Filters</h4>

              <AppliedFilterList filters={filters} hideHeadline={true}/>

              <h4>Trigger</h4>

              <div className="mb-3">
                <label htmlFor="monitor-condition" className="form-label">Trigger Condition: Result Count</label>
                <input type="number" min={0} className="form-control" id="monitor-condition" />
                <div className="form-text">
                  If the number of results exceeds the configured threshold, it triggers a detection event.
                </div>
              </div>

              <div className="mb-3">
                <label htmlFor="monitor-interval" className="form-label">Interval</label>
                <input type="number" min={1} className="form-control" id="monitor-interval" />
                <div className="form-text">How often to execute the monitor. Default: Run every minute.</div>
              </div>

            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={onClose}>Close</button>
              <button type="button" className="btn btn-primary">Create Monitor</button>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}
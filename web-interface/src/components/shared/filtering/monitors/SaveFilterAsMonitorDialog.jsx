import React, {useContext, useEffect, useState} from "react";
import AppliedFilterList from "../AppliedFilterList";
import {TapContext} from "../../../../App";
import TapsService from "../../../../services/TapsService";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";
import {onNumberInputKeyDown} from "../../../../util/Tools";

const tapsService = new TapsService();

export default function SaveFilterAsMonitorDialog({filters, onSave, onClose}) {

  const [organizationId, tenantId] = useSelectedTenant();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [taps, setTaps] = useState(null)

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [triggerCondition, setTriggerCondition] = useState(0);
  const [interval, setInterval] = useState(1);

  const [showError, setShowError] = useState(false);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [complete, setComplete] = useState(false);

  const formReady = () => {
    return name && name.trim().length > 0 && triggerCondition >= 0 && interval > 0
  }

  const onSuccess = () => {
    setShowError(false);
    setIsSubmitting(false);
    setComplete(true);
  }

  const onFailure = () => {
    setIsSubmitting(false);
    setShowError(true);
  }

  const submitButton = () => {
    if (complete) {
      return (
        <button type="button" className="btn btn-success w-100" onClick={onClose}>
          Done! Close dialog.
        </button>
      )
    } else {
      return (
        <button type="button"
                disabled={!formReady() || isSubmitting}
                className="btn btn-primary"
                onClick={(e) => {
                  e.preventDefault();
                  setShowError(false);
                  setIsSubmitting(true);
                  onSave(name, description, selectedTaps, triggerCondition, interval, filters, onSuccess, onFailure)
                }}>
          { isSubmitting ? <span><i className="fa-solid fa-circle-notch fa-spin"></i> &nbsp;Saving ...</span> : "Create Monitor" }
        </button>
      )
    }
  }

  const tapTable = () => {
    if (selectedTaps.length === 1 && selectedTaps[0] === "*") {
      return <div className="alert alert-info mb-0">This monitor will always use data from all your taps.</div>
    }

    return (
      <table className="table table-sm table-hover table-striped mt-0">
        <thead>
        <tr>
          <th>Name</th>
          <th>Online</th>
        </tr>
        </thead>
        <tbody>
        {taps.filter(tap => selectedTaps.includes(tap.uuid)).map((tap, i) => {
          return (
            <tr key={i}>
              <td>{tap.name}</td>
              <td>{tap.is_online ?
                <span className="text-success">Online</span>
                : <span className="text-warning">Offline</span>}
              </td>
            </tr>
          )
        })}
        </tbody>
      </table>
    )
  }

  useEffect(() => {
    tapsService.findAllTapsHighLevel(organizationId, tenantId, (r) => setTaps(r.data.taps));
  }, [selectedTaps]);

  if (taps === null) {
    return (
      <React.Fragment>
        <div className="modal-backdrop fade show"></div>
        <div className="modal fade show" style={{display: "block"}}>
          <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
            <div className="modal-content">
              <div className="modal-header">
                <h1 className="modal-title fs-5">Save Filter as Monitor</h1>
                <button type="button" className="btn-close" onClick={onClose}></button>
              </div>
              <div className="modal-body">
                <LoadingSpinner />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={onClose}>Close</button>
                <button type="button" className="btn btn-primary" disabled={true}>Create Monitor</button>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
    )
  }

  if (selectedTaps.length === 0) {
    return (
      <React.Fragment>
        <div className="modal-backdrop fade show"></div>
        <div className="modal fade show" style={{display: "block"}}>
          <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
            <div className="modal-content">
              <div className="modal-header">
                <h1 className="modal-title fs-5">Save Filter as Monitor</h1>
                <button type="button" className="btn-close" onClick={onClose}></button>
              </div>
              <div className="modal-body">
                <div className="alert alert-warning mb-0">You must select at least one tap to create a monitor.</div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={onClose}>Close</button>
                <button type="button" className="btn btn-primary" disabled={true}>Create Monitor</button>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
    )
  }

  return (
    <React.Fragment>
      <div className="modal-backdrop fade show"></div>
      <div className="modal fade show" style={{display: "block"}}>
        <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5">Save Filter as Monitor</h1>
              <button type="button" className="btn-close" onClick={onClose} disabled={isSubmitting}></button>
            </div>
            <div className="modal-body">
              <p>
                A <em>monitor</em> is a search that runs automatically on a schedule using your selected filters. If
                the number of results exceeds the configured threshold, it triggers a detection event.
              </p>

              <h3 className="mt-4 mb-2">Monitor Details</h3>

              <div className="mb-3">
                <label htmlFor="monitor-name" className="form-label">Name</label>
                <input type="text"
                       className="form-control"
                       id="monitor-rule-name"
                       value={name}
                       onChange={e => setName(e.target.value)} />
                <div className="form-text">
                  The name of this monitor. Give it a name that helps to quickly identify what it is supposed to
                  alert on.
                </div>
              </div>

              <div className="mb-3">
                <label htmlFor="monitor-description" className="form-label">Description <small>Optional</small></label>
                <textarea className="form-control"
                          id="description"
                          value={description}
                          onChange={e => setDescription(e.target.value)} />
                <div className="form-text">An optional description that provides more detail.</div>
              </div>

              <h3 className="mt-4 mb-2">Taps</h3>

              <p className="help-text mb-2">Data from the following taps will be considered for the monitor.</p>

              {tapTable()}

              <h3 className="mt-4 mb-2">Filters</h3>

              <AppliedFilterList filters={filters} hideHeadline={true}/>

              <h3 className="mt-4 mb-2">Trigger</h3>

              <div className="mb-3">
                <label htmlFor="monitor-condition" className="form-label">Trigger Condition: Result Count</label>
                <div className="input-group">
                  <input type="number"
                         min={0}
                         onKeyDown={onNumberInputKeyDown}
                         className="form-control"
                         id="monitor-condition"
                         value={triggerCondition}
                         onChange={e => setTriggerCondition(e.target.value)}/>
                  <span className="input-group-text">Results</span>
                </div>
                <div className="form-text">
                  If the number of results exceeds the configured threshold, it triggers a detection event.
                </div>
                </div>

                <div className="mb-3">
                  <label htmlFor="monitor-interval" className="form-label">Interval</label>
                  <div className="input-group">
                  <input type="number"
                         min={1}
                         onKeyDown={onNumberInputKeyDown}
                         className="form-control"
                         id="monitor-interval"
                         value={interval}
                         onChange={e => setInterval(e.target.value)}/>
                    <span className="input-group-text">Minutes</span>
                  </div>
                  <div className="form-text">How often to execute the monitor. Default: Run every minute.</div>
                </div>

              {showError ?
                <div className="alert alert-danger mb-0" role="alert">Something went wrong. Please try again or contact
                  your administrator.</div> : null}

              </div>
              <div className="modal-footer">
                { complete ? null :
                  <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>
                    Close</button> }
                {submitButton()}
              </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}
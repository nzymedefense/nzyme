import React, {useEffect, useState} from "react";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";
import Dot11Service from "../../../../services/Dot11Service";
import KnownNetworksTable from "./KnownNetworksTable";
import {toast} from "react-toastify";

const dot11Service = new Dot11Service();

export default function ApproveKnownNetworksDialog({onClose, onComplete}) {

  const [organizationId, tenantId] = useSelectedTenant();

  const [regex, setRegex] = useState("");
  const [testClick, setTestClick] = useState(null);

  const [matchedSsids, setMatchedSsids] = useState(null);
  const [perPage, setPerPage] = useState(10);
  const [page, setPage] = useState(1);
  const [orderColumn, setOrderColumn] = useState("ssid");
  const [orderDirection, setOrderDirection] = useState("ASC");

  const [showError, setShowError] = useState(false);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [complete, setComplete] = useState(false);

  const formReady = () => {
    return regex && regex.trim().length > 0
  }

  const testReady = () => {
    return !complete && regex && regex.trim().length > 0
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
                onClick={onSubmit}>
          { isSubmitting ? <span><i className="fa-solid fa-circle-notch fa-spin"></i> &nbsp;Approving ...</span> : "Approve All Matching SSIDs" }
        </button>
      )
    }
  }

  const test = (e) => {
    e.preventDefault();

    if (!testReady()) {
      setTestClick(null);
    }

    setTestClick(new Date());
  }

  const matchedTable = () => {
    if (matchedSsids === null) {
      return <div className="alert alert-info">
        Click the <em>Test</em> button to test the regular expression. <em>(Optional)</em>
      </div>
    }

    if (matchedSsids.networks.length === 0) {
      return <div className="alert alert-warning">No SSIDs matched.</div>
    }

    return (
      <KnownNetworksTable networks={matchedSsids}
                          page={page}
                          setPage={setPage}
                          perPage={perPage}
                          orderColumn={orderColumn}
                          setOrderColumn={setOrderColumn}
                          orderDirection={orderDirection}
                          setOrderDirection={setOrderDirection} />
    )
  }

  const onSubmit = (e) => {
    e.preventDefault();
    setShowError(false);
    setIsSubmitting(true);

    dot11Service.approveAllKnownNetworksByPattern(organizationId, tenantId, regex, () => {
      setIsSubmitting(false);
      setComplete(true);
      onComplete();
    }, () => {
      toast.error("Could not approve networks.");
      setIsSubmitting(false);
    })
  }

  useEffect(() => {
    setMatchedSsids(null);
    setShowError(false);

    if (testClick !== null) {
      dot11Service.findAllKnownNetworksByPattern(
        organizationId, tenantId, regex, orderColumn, orderDirection, perPage, (page-1)*perPage, setMatchedSsids, () => {
          setShowError(true);
        }
      )
    }
  }, [testClick, complete, page, orderColumn, orderDirection, organizationId, tenantId]);

  return (
    <>
      <div className="modal-backdrop fade show"></div>
      <div className="modal fade show" style={{display: "block"}}>
        <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
          <div className="modal-content">
            <div className="modal-header">
              <h1 className="modal-title fs-5">Approve Known SSIDs/Networks by Pattern</h1>
              <button type="button" className="btn-close" onClick={onClose}></button>
            </div>
            <div className="modal-body">

              <div className="mb-3">
                <label htmlFor="regex" className="form-label">Regular Expression</label>

                <div className="input-group">
                  <input type="text"
                         className="form-control"
                         id="regex"
                         value={regex}
                         onChange={e => setRegex(e.target.value)}/>
                  <button className="btn btn-primary"
                          type="button"
                          onClick={test}
                          disabled={!testReady() || isSubmitting}>
                    Test
                  </button>
                </div>
                <div className="form-text" onClick={test}>
                  All SSIDs matching this regular expression will be approved.
                </div>
              </div>

              <h4>Matched SSIDs</h4>
              {matchedTable()}

              {showError ?
                <div className="alert alert-danger mb-0" role="alert">
                  Test failed. Make sure that the regular expression is valid. Try <code>.*</code> to match all networks.
                </div> : null}
            </div>
            <div className="modal-footer">
              {complete ? null :
                <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>
                  Close</button>}
              {submitButton()}
            </div>
          </div>
        </div>
      </div>
    </>
  )

}
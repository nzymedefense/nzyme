import React, {useEffect, useState} from "react";
import ApiRoutes from "../../../../util/ApiRoutes";
import {Navigate, useParams} from "react-router-dom";
import Dot11Service from "../../../../services/Dot11Service";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import {notify} from "react-notify-toast";
import moment from "moment/moment";
import CustomBanditFingerprints from "./CustomBanditFingerprints";

const dot11Service = new Dot11Service();

function CustomBanditDetailsPage() {

  const {id} = useParams();

  const [bandit, setBandit] = useState();

  const [revision, setRevision] = useState(0);

  const [newFingerprint, setNewFingerprint] = useState("");
  const [fingerprintFormSubmitting, setFingerprintFormSubmitting] = useState(false);

  const [redirect, setRedirect] = useState(false);

  const fingerprintFormEnabled = () => {
    return newFingerprint.trim().length === 64 && !bandit.fingerprints.includes(newFingerprint)
  }

  const addFingerprint = (fingerprint) => {
    setFingerprintFormSubmitting(true);
    dot11Service.addFingerprintToCustomBandit(bandit.id, fingerprint, () => {
      notify.show('Fingerprint added to bandit.', 'success');
      setFingerprintFormSubmitting(false);
      setNewFingerprint("");
      setRevision(revision+1);
    })
  }

  const onDeleteFingerprint = (e, fingerprint) => {
    e.preventDefault();

    if (!confirm("Really delete bandit fingerprint?")) {
      return;
    }

    dot11Service.deleteFingerprintOfCustomBandit(bandit.id, fingerprint, () => {
      notify.show('Fingerprint deleted.', 'success');
      setRevision(revision+1);
    })
  }

  const onDelete = (e) => {
    e.preventDefault();

    if (!confirm("Really delete bandit?")) {
      return;
    }

    dot11Service.deleteCustomBandit(bandit.id, () => {
      notify.show('Custom bandit deleted.', 'success');
      setRedirect(true);
    });
  }

  useEffect(() => {
    dot11Service.findCustomBandit(id, setBandit);
  }, [id, revision]);

  if (redirect) {
    return <Navigate to={ApiRoutes.DOT11.MONITORING.BANDITS.INDEX} />
  }

  if (!bandit) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-7">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.INDEX}>Monitoring</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.BANDITS.INDEX}>Bandits</a></li>
                <li className="breadcrumb-item">Custom</li>
                <li className="breadcrumb-item active" aria-current="page">{bandit.name}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-5">
            <span className="float-end">
              <a className="btn btn-secondary" href={ApiRoutes.DOT11.MONITORING.BANDITS.INDEX}>Back</a>{' '}
              <a className="btn btn-danger" href="" onClick={onDelete}>Delete</a>{' '}
              <a className="btn btn-primary" href={ApiRoutes.DOT11.MONITORING.BANDITS.EDIT(bandit.id)}>Edit</a>
            </span>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>
              Custom Bandit &quot;{bandit.name}&quot;
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Metadata</h3>

                <dl className="mb-0">
                  <dt>Created at</dt>
                  <dd title={moment(bandit.created_at).format()}>{moment(bandit.created_at).fromNow()}</dd>
                  <dt>Last changed at</dt>
                  <dd title={moment(bandit.updated_at).format()}>{moment(bandit.updated_at).fromNow()}</dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Description</h3>

                <p className="mb-0">
                  {bandit.description}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Fingerprints</h3>

                <p>
                  You can learn more about fingerprints in
                  the <a href="https://go.nzyme.org/wifi-fingerprinting">documentation</a>.
                </p>

                <CustomBanditFingerprints fingerprints={bandit.fingerprints} onDelete={onDeleteFingerprint} />

                <div className="input-group mb-0 mt-3">
                  <input type="text"
                         className="form-control"
                         placeholder="500f6ae3f2724911c6a36da4185992f5f2fb6b51fdfeab1bf7264d12123fbc96"
                         value={newFingerprint}
                         onChange={(e) => setNewFingerprint(e.target.value)} />
                  <button className="btn btn-secondary"
                          disabled={!fingerprintFormEnabled()}
                          onClick={() => { addFingerprint(newFingerprint) }}>
                    {fingerprintFormSubmitting ? "Please wait..." : "Add Fingerprint"}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default CustomBanditDetailsPage;
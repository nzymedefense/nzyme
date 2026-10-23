import React, {useEffect, useState} from "react";
import ReconService from "../../../../services/ethernet/ReconService";
import {ALLOWED_RECON_SCAN_TYPE_VALUES} from "../../../shared/filtering/validators/ReconScanTypeValidator";
import {reconScanTypeToName} from "../ReconScanType";
import ReconExclusionImpact from "./ReconExclusionImpact";

const reconService = new ReconService();

const EMPTY = {
  name: "",
  description: "",
  enabled: true,
  scan_types: [],
  source_cidr: "",
  source_mac: "",
  target_cidr: ""
};

const criteriaOf = (values) => {
  return {
    scan_types: values.scan_types,
    source_cidr: values.source_cidr,
    source_mac: values.source_mac,
    target_cidr: values.target_cidr
  }
}

const hasCriteria = (values) => {
  return values.scan_types.length > 0 || values.source_cidr.trim() !== ""
    || values.source_mac.trim() !== "" || values.target_cidr.trim() !== "";
}

export default function ReconExclusionForm({organizationId, tenantId, initialValues = undefined, submitText,
                                             onSubmit, isCreate = false}) {

  const [values, setValues] = useState({...EMPTY, ...(initialValues ? initialValues : {})});
  const [applyToExisting, setApplyToExisting] = useState(true);

  const [impact, setImpact] = useState(null);
  const [criteriaError, setCriteriaError] = useState(null);
  const [previewPending, setPreviewPending] = useState(false);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);

  const criteriaKey = JSON.stringify(criteriaOf(values));

  // Live preview, debounced.
  useEffect(() => {
    if (!hasCriteria(values)) {
      setImpact(null);
      setCriteriaError(null);
      setPreviewPending(false);
      return;
    }

    setPreviewPending(true);
    const timer = setTimeout(() => {
      reconService.previewExclusion(organizationId, tenantId, criteriaOf(values), (result) => {
        setImpact(result);
        setCriteriaError(null);
        setPreviewPending(false);
      }, (error) => {
        setImpact(null);
        setPreviewPending(false);
        if (error && error.response && error.response.status === 422 && error.response.data) {
          setCriteriaError(error.response.data.message);
        } else {
          setCriteriaError("Could not check the criteria.");
        }
      });
    }, 400);

    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [criteriaKey, organizationId, tenantId]);

  const update = (field) => (e) => {
    const value = e.target.type === "checkbox" ? e.target.checked : e.target.value;
    setValues({...values, [field]: value});
  }

  const toggleType = (type) => (e) => {
    const types = e.target.checked
      ? [...values.scan_types, type]
      : values.scan_types.filter((t) => t !== type);
    setValues({...values, scan_types: ALLOWED_RECON_SCAN_TYPE_VALUES.filter((t) => types.includes(t))});
  }

  const formIsReady = () => {
    return values.name.trim().length > 0 && hasCriteria(values) && !criteriaError && !previewPending && !isSubmitting;
  }

  const submit = (e) => {
    e.preventDefault();

    if (!formIsReady()) {
      return;
    }

    setIsSubmitting(true);
    setSubmitError(null);

    const payload = {
      name: values.name.trim(),
      description: values.description,
      enabled: values.enabled,
      ...criteriaOf(values)
    };

    if (isCreate) {
      payload.apply_to_existing_scans = applyToExisting && impact && impact.matching_scans > 0;
    }

    onSubmit(payload, (error) => {
      setIsSubmitting(false);
      if (error && error.response && error.response.status === 422 && error.response.data) {
        setSubmitError(error.response.data.message);
      } else {
        setSubmitError("Could not save the exclusion.");
      }
    });
  }

  return (
    <form onSubmit={submit}>
      <div className="mb-3">
        <label htmlFor="exclusion-name" className="form-label">Name</label>
        <input type="text" className="form-control" id="exclusion-name" maxLength={128}
               value={values.name} onChange={update("name")} />
      </div>

      <div className="mb-3">
        <label htmlFor="exclusion-description" className="form-label">Description <small>Optional</small></label>
        <textarea className="form-control" id="exclusion-description" rows={2}
                  value={values.description} onChange={update("description")} />
        <div className="form-text">Why this device should never show up as a scanner.</div>
      </div>

      <div className="form-check mb-4">
        <input className="form-check-input" type="checkbox" id="exclusion-enabled"
               checked={values.enabled} onChange={update("enabled")} />
        <label className="form-check-label" htmlFor="exclusion-enabled">Enabled</label>
      </div>

      <h5>Scanning Device</h5>
      <p className="text-muted">
        The device doing the probing. Leave a field empty to not match on it. All filled fields must match.
      </p>

      <div className="row">
        <div className="col-md-6 mb-3">
          <label htmlFor="exclusion-source-cidr" className="form-label">IP Address or Network</label>
          <input type="text" className="form-control" id="exclusion-source-cidr"
                 placeholder="192.168.0.10"
                 value={values.source_cidr} onChange={update("source_cidr")} />
          <div className="form-text">
            A single address like <code>192.168.0.10</code> or a network in CIDR notation like{' '}
            <code>10.0.0.0/8</code>. IPv4 and IPv6.
          </div>
        </div>

        <div className="col-md-6 mb-3">
          <label htmlFor="exclusion-source-mac" className="form-label">MAC Address</label>
          <input type="text" className="form-control" id="exclusion-source-mac"
                 placeholder="74:56:3C:43:77:AF"
                 value={values.source_mac} onChange={update("source_mac")} />
          <div className="form-text">
            Use the MAC address for devices that change their IP address, like a scanner on DHCP.
          </div>
        </div>
      </div>

      <h5 className="mt-2">Scan</h5>

      <div className="row">
        <div className="col-md-6 mb-3">
          <label className="form-label">Scan Types</label>
          <div>
            {ALLOWED_RECON_SCAN_TYPE_VALUES.map((type) => {
              return (
                <div className="form-check" key={type}>
                  <input className="form-check-input" type="checkbox" id={"exclusion-type-" + type}
                         checked={values.scan_types.includes(type)} onChange={toggleType(type)} />
                  <label className="form-check-label" htmlFor={"exclusion-type-" + type}>
                    {reconScanTypeToName(type)}
                  </label>
                </div>
              )
            })}
          </div>
          <div className="form-text">Leave all unchecked to match every type.</div>
        </div>

        <div className="col-md-6 mb-3">
          <label htmlFor="exclusion-target-cidr" className="form-label">Target Network</label>
          <input type="text" className="form-control" id="exclusion-target-cidr"
                 placeholder="10.0.5.0/24"
                 value={values.target_cidr} onChange={update("target_cidr")} />
          <div className="form-text">
            Only exclude probing of this network. A port scan matches by its target host, a sweep matches when
            every host it probed is inside the network. Useful for lab or honeypot networks.
          </div>
        </div>
      </div>

      <h5 className="mt-2">Existing Scans</h5>

      <div className="mb-3">
        {!hasCriteria(values) ?
          <div className="alert alert-secondary mb-0">Fill in at least one criterion above.</div>
          : criteriaError ?
            <div className="alert alert-danger mb-0">{criteriaError}</div>
            : previewPending ?
              <div className="alert alert-secondary mb-0">
                <i className="fa-solid fa-circle-notch fa-spin"></i> Checking existing scans ...
              </div>
              : <ReconExclusionImpact impact={impact} />}
      </div>

      {isCreate && impact && impact.matching_scans > 0 && !criteriaError ?
        <div className="form-check mb-3">
          <input className="form-check-input" type="checkbox" id="exclusion-apply"
                 checked={applyToExisting} onChange={(e) => setApplyToExisting(e.target.checked)} />
          <label className="form-check-label" htmlFor="exclusion-apply">
            Delete the matching scans now.
          </label>
        </div> : null}

      {!isCreate && impact && impact.matching_scans > 0 ?
        <p className="text-muted">
          Existing scans are not deleted when saving. Use <em>Delete Matching Scans</em> on the exclusion page to
          remove them.
        </p> : null}

      {submitError ? <div className="alert alert-danger">{submitError}</div> : null}

      <button className="btn btn-primary" type="submit" disabled={!formIsReady()}>
        {isSubmitting ? <span><i className="fa-solid fa-circle-notch fa-spin"></i> Saving ...</span> : submitText}
      </button>
    </form>
  )

}

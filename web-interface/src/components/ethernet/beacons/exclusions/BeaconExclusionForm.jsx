import React, {useEffect, useState} from "react";
import BeaconsService from "../../../../services/ethernet/BeaconsService";
import {ALLOWED_BEACON_TYPE_VALUES} from "../../../shared/filtering/validators/BeaconTypeValidator";
import BeaconExclusionImpact from "./BeaconExclusionImpact";

const beaconsService = new BeaconsService();

const EMPTY = {
  name: "",
  description: "",
  enabled: true,
  destination_cidr: "",
  destination_host: "",
  destination_ports: "",
  beacon_types: [],
  source_cidr: "",
  source_mac: ""
};

const criteriaOf = (values) => {
  return {
    destination_cidr: values.destination_cidr,
    destination_host: values.destination_host,
    destination_ports: values.destination_ports,
    beacon_types: values.beacon_types,
    source_cidr: values.source_cidr,
    source_mac: values.source_mac
  }
}

const hasCriteria = (values) => {
  return values.destination_cidr.trim() !== "" || values.destination_host.trim() !== ""
    || values.destination_ports.trim() !== "" || values.beacon_types.length > 0
    || values.source_cidr.trim() !== "" || values.source_mac.trim() !== "";
}

export default function BeaconExclusionForm({organizationId, tenantId, initialValues = undefined, submitText,
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
      beaconsService.previewExclusion(organizationId, tenantId, criteriaOf(values), (result) => {
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
      ? [...values.beacon_types, type]
      : values.beacon_types.filter((t) => t !== type);
    setValues({...values, beacon_types: ALLOWED_BEACON_TYPE_VALUES.filter((t) => types.includes(t))});
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
      payload.apply_to_existing_beacons = applyToExisting && impact && impact.matching_beacons > 0;
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
        <div className="form-text">Why this traffic should never show up as a beacon.</div>
      </div>

      <div className="form-check mb-4">
        <input className="form-check-input" type="checkbox" id="exclusion-enabled"
               checked={values.enabled} onChange={update("enabled")} />
        <label className="form-check-label" htmlFor="exclusion-enabled">Enabled</label>
      </div>

      <h5>Destination</h5>
      <p className="text-muted">
        What the devices call out to. Leave a field empty to not match on it. All filled fields must match.
      </p>

      <div className="row">
        <div className="col-md-6 mb-3">
          <label htmlFor="exclusion-destination-host" className="form-label">Host Name</label>
          <input type="text" className="form-control" id="exclusion-destination-host"
                 placeholder="*.example.com"
                 value={values.destination_host} onChange={update("destination_host")} />
          <div className="form-text">
            TLS server name, SOCKS tunneled host or DNS domain. Use <code>*</code> as wildcard.{' '}
            <code>*.example.com</code> matches <code>example.com</code> and all of its subdomains.
          </div>
        </div>

        <div className="col-md-6 mb-3">
          <label htmlFor="exclusion-destination-cidr" className="form-label">IP Address or Network</label>
          <input type="text" className="form-control" id="exclusion-destination-cidr"
                 placeholder="10.0.0.0/8"
                 value={values.destination_cidr} onChange={update("destination_cidr")} />
          <div className="form-text">
            A single address like <code>95.216.195.133</code> or a network in CIDR notation. IPv4 and IPv6.
          </div>
        </div>
      </div>

      <div className="row">
        <div className="col-md-6 mb-3">
          <label htmlFor="exclusion-destination-ports" className="form-label">Ports</label>
          <input type="text" className="form-control" id="exclusion-destination-ports"
                 placeholder="443"
                 value={values.destination_ports} onChange={update("destination_ports")} />
          <div className="form-text">
            Single ports and ranges, separated by commas: <code>80,443,8000-8100</code>
          </div>
        </div>

        <div className="col-md-6 mb-3">
          <label className="form-label">Beacon Types</label>
          <div>
            {ALLOWED_BEACON_TYPE_VALUES.map((type) => {
              return (
                <div className="form-check form-check-inline" key={type}>
                  <input className="form-check-input" type="checkbox" id={"exclusion-type-" + type}
                         checked={values.beacon_types.includes(type)} onChange={toggleType(type)} />
                  <label className="form-check-label" htmlFor={"exclusion-type-" + type}>{type}</label>
                </div>
              )
            })}
          </div>
          <div className="form-text">Leave all unchecked to match every type.</div>
        </div>
      </div>

      <h5 className="mt-2">Calling Device</h5>
      <p className="text-muted">
        Limit the exclusion to specific devices. Leave empty to exclude the destination for all devices.
      </p>

      <div className="row">
        <div className="col-md-6 mb-3">
          <label htmlFor="exclusion-source-cidr" className="form-label">IP Address or Network</label>
          <input type="text" className="form-control" id="exclusion-source-cidr"
                 placeholder="192.168.0.0/24"
                 value={values.source_cidr} onChange={update("source_cidr")} />
        </div>

        <div className="col-md-6 mb-3">
          <label htmlFor="exclusion-source-mac" className="form-label">MAC Address</label>
          <input type="text" className="form-control" id="exclusion-source-mac"
                 placeholder="74:56:3C:43:77:AF"
                 value={values.source_mac} onChange={update("source_mac")} />
        </div>
      </div>

      <h5 className="mt-2">Existing Beacons</h5>

      <div className="mb-3">
        {!hasCriteria(values) ?
          <div className="alert alert-secondary mb-0">Fill in at least one criterion above.</div>
          : criteriaError ?
            <div className="alert alert-danger mb-0">{criteriaError}</div>
            : previewPending ?
              <div className="alert alert-secondary mb-0">
                <i className="fa-solid fa-circle-notch fa-spin"></i> Checking existing beacons ...
              </div>
              : <BeaconExclusionImpact impact={impact} />}
      </div>

      {isCreate && impact && impact.matching_beacons > 0 && !criteriaError ?
        <div className="form-check mb-3">
          <input className="form-check-input" type="checkbox" id="exclusion-apply"
                 checked={applyToExisting} onChange={(e) => setApplyToExisting(e.target.checked)} />
          <label className="form-check-label" htmlFor="exclusion-apply">
            Delete the matching beacons now. Beacons where only some devices match lose those devices.
          </label>
        </div> : null}

      {!isCreate && impact && impact.matching_beacons > 0 ?
        <p className="text-muted">
          Existing beacons are not deleted when saving. Use <em>Delete now</em> in the exclusions list to remove them.
        </p> : null}

      {submitError ? <div className="alert alert-danger">{submitError}</div> : null}

      <button className="btn btn-primary" type="submit" disabled={!formIsReady()}>
        {isSubmitting ? <span><i className="fa-solid fa-circle-notch fa-spin"></i> Saving ...</span> : submitText}
      </button>
    </form>
  )

}

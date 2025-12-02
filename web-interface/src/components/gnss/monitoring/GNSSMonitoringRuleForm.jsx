import React, {useEffect, useState} from "react";
import TapsService from "../../../services/TapsService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import GNSSConstellationConditionForm from "./conditions/forms/GNSSConstellationConditionForm";
import GNSSFixQualityConditionForm from "./conditions/forms/GNSSFixQualityConditionForm";
import {notify} from "react-notify-toast";
import GNSSFixDistanceConditionForm from "./conditions/forms/GNSSFixDistanceConditionForm";
import GNSSPDOPConditionForm from "./conditions/forms/GNSSPDOPConditionForm";
import GNSSClockDriftConditionForm from "./conditions/forms/GNSSClockDriftConditionForm";
import conditionTypeSetToDescription from "./conditions/descriptions/GNSSConditionsDescriptionFactory";
import conditionTypeToTitle from "./conditions/GNSSConditionTypeTitleFactory";

const tapsService = new TapsService();

export default function GNSSMonitoringRuleForm(props) {

  const [organizationId, tenantId] = useSelectedTenant();

  const onSubmit = props.onSubmit;
  const submitTextProp = props.submitText;

  const [name, setName] = useState(props.name ? props.name : "");
  const [description, setDescription] = useState(props.description ? props.description : "");

  const [tapLimiterType, setTapLimiterType] = useState(props.selectedTaps != null && props.selectedTaps.length > 0 ? "SELECTED" : "ALL");
  const [selectedTaps, setSelectedTaps] = useState(props.selectedTaps ? props.selectedTaps.map((t) => t.uuid) : [])

  const [selectedConditionType, setSelectedConditionType] = useState("CONSTELLATION");
  const [conditionFormType, setConditionFormType] = useState(null);

  const [conditions, setConditions] = useState(props.conditions ? props.conditions : {});

  const [availableTaps, setAvailableTaps] = useState(null);

  const [isSubmitting, setIsSubmitting] = React.useState(false);
  const [submitText, setSubmitText] = useState(submitTextProp);

  useEffect(() => {
    tapsService.findAllTapsHighLevel(organizationId, tenantId, (r) => setAvailableTaps(r.data.taps))
  }, [organizationId, tenantId]);

  useEffect(() => {
    if (tapLimiterType === "ALL") {
      // Reset selected taps if ALL is selected.
      setSelectedTaps([]);
    }
  }, [tapLimiterType]);

  const updateValue = function(e, setter) {
    setter(e.target.value);
  }

  const makeConditionKey = (cond) => JSON.stringify(cond);

  function addCondition(prev, type, cond) {
    const list = prev[type] ?? [];
    const key = makeConditionKey(cond);

    if (list.some((x) => makeConditionKey(x) === key)) {
      notify.show("This condition already exists.", "error");
      return prev;
    }

    return {
      ...prev,
      [type]: [...list, cond],
    };
  }

  function removeCondition(prev, type, cond) {
    const key = makeConditionKey(cond);
    const list = prev[type] ?? [];

    const newList = list.filter((x) => makeConditionKey(x) !== key);

    // If nothing was removed, just return prev (avoids re-renders)
    if (newList.length === list.length) return prev;

    const next = { ...prev, [type]: newList };

    // Clean up if the list is now empty
    if (newList.length === 0) {
      delete next[type];
    }

    return next;
  }

  const onConditionAdded = (c) => {
    const type = conditionFormType || selectedConditionType;
    if (!type) return;

    setConditions((prev) => addCondition(prev, type, c));
    setConditionFormType(null);
  };

  const onConditionRemoved = (c, type) => {
    if (!confirm("Really delete condition?")) {
      return;
    }

    setConditions((prev) => removeCondition(prev, type, c));
  };

  const onTapSelected = (uuid) => {
    setSelectedTaps(prev => {
      if (prev.includes(uuid)) {
        // Remove tap.
        return prev.filter(id => id !== uuid);
      } else {
        // Add tap.
        return [...prev, uuid];
      }
    });
  }

  const tapLimiterForm = () => {
    if (tapLimiterType === "SELECTED") {
      if (availableTaps.length === 0) {
        return (
            <div className="alert alert-warning mb-3">
              You have no Nzyme taps. You need Nzyme taps to perform the selected action.
            </div>
        )
      }

      return (
          <div className="mb-3">
            <label className="form-label">Selected Taps</label>

            <table className="table table-sm table-hover table-striped">
              <thead>
              <tr>
                <th style={{width: 40}} title="Selected">Sel.</th>
                <th>Name</th>
                <th>Online</th>
              </tr>
              </thead>
              <tbody>
              {availableTaps.map((tap, i) => {
                return (
                    <tr key={i}>
                      <td>
                        <input className="form-check-input"
                               type="checkbox"
                               checked={selectedTaps.includes(tap.uuid)}
                               onChange={() => onTapSelected(tap.uuid)} />
                      </td>
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
          </div>
      )
    }

    return null;
  }

  const submit = function(e) {
    e.preventDefault();

    setIsSubmitting(true)
    setSubmitText("Please wait...");
    onSubmit(name, description, conditions, selectedTaps, () => {
      // Failure.
      notify.show("Could not update monitoring rule.", "error");
      setSubmitText(submitTextProp)
      setIsSubmitting(false);
    });
  }

  const formIsReady = function() {
    /*
     * Must have a name and also conditions that are not "CONSTELLATION", because that
     * constellation by itself makes no sense.
     */
    return name && name.trim().length > 0 && Object.keys(conditions).length > 0
      && !(Object.keys(conditions).length === 1 && conditions["CONSTELLATION"])
      && (tapLimiterType === "ALL" || (tapLimiterType === "SELECTED" && selectedTaps.length > 0));
  }

  const configuredConditionsTable = () => {
    if (Object.keys(conditions).length === 0) {
      return <div className="alert alert-info mb-3">No conditions configured yet.</div>
    }

    return (
        <>
          <p>Click on a condition to remove it.</p>

          <table className="mb-3 table table-sm table-hover table-striped">
            <thead>
            <tr>
              <th>Type</th>
              <th>Condition</th>
            </tr>
            </thead>
            <tbody>
            {Object.keys(conditions).map((type, i) => {
              return (
                  <tr key={i}>
                    <td>{conditionTypeToTitle(type)}</td>
                    <td title="Click on a condition to remove it.">
                      {conditionTypeSetToDescription(type, conditions[type], onConditionRemoved)}
                    </td>
                  </tr>
              )
            })}
            </tbody>
          </table>
        </>
    )
  }

  const newConditionForm = () => {
    if (!conditionFormType) {
      return null;
    }

    let form = null;
    switch (conditionFormType) {
      case "CONSTELLATION":
        form = <GNSSConstellationConditionForm onConditionAdded={onConditionAdded} />;
        break;
      case "FIX_QUALITY":
        form = <GNSSFixQualityConditionForm onConditionAdded={onConditionAdded} />;
        break;
      case "FIX_DISTANCE":
        form = <GNSSFixDistanceConditionForm onConditionAdded={onConditionAdded} />;
        break;
      case "PDOP":
        form = <GNSSPDOPConditionForm onConditionAdded={onConditionAdded} />;
        break;
      case "CLOCK_DRIFT":
        form = <GNSSClockDriftConditionForm onConditionAdded={onConditionAdded} />;
        break;
    }

    return <div className="condition-form mb-3">{form}</div>
  }

  if (availableTaps == null) {
    return <LoadingSpinner />
  }

  return (
      <form>
        <p>Triggers a detection event when all conditions are met.</p>

        <div className="mb-3">
          <label htmlFor="name" className="form-label">Name</label>
          <input type="text" className="form-control" id="name"
                 value={name} onChange={(e) => { updateValue(e, setName) }} />
          <div className="form-text">A descriptive name of the rule.</div>
        </div>

        <div className="mb-3">
          <label htmlFor="description" className="form-label">Description <small>Optional</small></label>
          <textarea className="form-control" id="description"
                    value={description} onChange={(e) => { updateValue(e, setDescription) }} />
          <div className="form-text">An optional description that provides more detail.</div>
        </div>

        <div className="mb-3">
          <label htmlFor="tap_limiter_type" className="form-label">Taps</label>
          <select className="form-control" id="tap_limiter_type"
                  value={tapLimiterType} onChange={(e) => { updateValue(e, setTapLimiterType) }} >
            <option value="ALL">All Taps</option>
            <option value="SELECTED">Selected Taps Only</option>
          </select>
          <div className="form-text">
            You can limit this rule to specific taps.
          </div>
        </div>

        {tapLimiterForm()}

        <hr />

        <h3>Add Condition</h3>

        <p>
          Conditions of the same type are <code>OR</code> connected, all other conditions are <code>AND</code> connected.
        </p>

        <div className="input-group mb-3">
          <select className="form-control"
                  id="new_condition"
                  value={selectedConditionType}
                  onChange={(e) => updateValue(e, setSelectedConditionType)}>
            <option value="CONSTELLATION">Constellation</option>
            <option value="FIX_QUALITY">Fix Quality</option>
            <option value="FIX_DISTANCE">Fix Distance from Tap</option>
            <option value="PDOP">Dilution of Precision (PDOP)</option>
            <option value="CLOCK_DRIFT">Clock Drift</option>
            <option value="NOISE">Noise</option>
            <option value="JAMMING_INDICATOR">Jamming Indicator</option>
            <option value="AGC_COUNT">AGC Adjustment Counts</option>
          </select>
          <button className="btn btn-primary"
                  onClick={(e) => { e.preventDefault(); setConditionFormType(selectedConditionType) }}>
            Create
          </button>
        </div>

        {newConditionForm()}

        <hr />

        <h3>Configured Conditions</h3>

        {configuredConditionsTable()}

        <button className="btn btn-primary" onClick={submit} disabled={!formIsReady() || isSubmitting}>
          {submitText}
        </button>
      </form>
  )

}
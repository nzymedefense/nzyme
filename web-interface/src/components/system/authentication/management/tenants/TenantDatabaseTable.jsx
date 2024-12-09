import React, {useEffect, useState} from "react";
import SystemService from "../../../../../services/SystemService";
import {notify} from "react-notify-toast";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import numeral from "numeral";
import {humanReadableDatabaseCategoryName} from "../../../../../util/Tools";

const systemService = new SystemService();

export default function TenantDatabaseTable(props) {

  const tenant = props.tenant;

  const [sizes, setSizes] = useState(null);

  const [selectedCategory, setSelectedCategory] = useState(null);
  const [retentionTimeDays, setRetentionTimeDays] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [revision, setRevision] = useState(new Date());

  const purge = (e, category) => {
    e.preventDefault();

    if (!confirm("Really purge all data of this tenant in the selected data category? This cannot be undone.")) {
      return;
    }

    systemService.purgeDatabaseTenantCategory(category, tenant.organization_id, tenant.id, () => {
      setRevision(new Date());
      notify.show('Data purge request submitted. It can take a while to complete.', 'success');
    });
  }

  const openRetentionTimeDialog = (e, category, currentRetentionTimeDays) => {
    e.preventDefault();

    setSelectedCategory(category);
    setRetentionTimeDays(currentRetentionTimeDays);
  }

  const readyToSubmit = () => {
    return retentionTimeDays && retentionTimeDays > 0;
  }

  const submit = (e) => {
    e.preventDefault();

    if (retentionTimeDays <= 0) {
      return;
    }

    // REST: category, retentionTimeDays. update submitting/submitted. handle submitted in button to close
  }

  useEffect(() => {
    if (tenant) {
      setSizes(null);
      systemService.getDatabaseTenantSizes(setSizes, tenant.organization_id, tenant.id,);
    }
  }, [tenant, revision]);

  if (!tenant || !sizes) {
    return <LoadingSpinner />;
  }

  return (
    <React.Fragment>
      <table className="table table-sm table-hover table-striped mb-0">
        <thead>
        <tr>
          <th>Data Category</th>
          <th>Rows</th>
          <th>Retention Time</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody>
        {Object.keys(sizes.categories).sort().map((key, i) => {
          return (
            <tr key={i}>
              <td>{humanReadableDatabaseCategoryName(key)}</td>
              <td>{numeral(sizes.categories[key].rows).format("0,0")}</td>
              <td>
                <a href="#"
                   onClick={(e) => openRetentionTimeDialog(e, key, sizes.categories[key].retention_days)}
                   data-bs-toggle="modal"
                   data-bs-target="#retention-time-configuration-modal"
                   title="Configure Retention Time">
                  {numeral(sizes.categories[key].retention_days).format("0,0")} Days
                </a>
              </td>
              <td><a href="#" onClick={(e) => purge(e, key)}>Purge</a></td>
            </tr>
          )
        })}
        </tbody>
      </table>

      <div className="modal" id="retention-time-configuration-modal"
           data-bs-keyboard="false" data-bs-backdrop="static">
        <div className="modal-dialog">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">Edit Retention Time</h5>
            </div>

            <div className="modal-body">
              <label className="mb-2" htmlFor="retention-time-days">
                Retention time of data category <em>{humanReadableDatabaseCategoryName(selectedCategory)}</em>:
              </label>
              <div className="input-group">
                <input type="number"
                       className="form-control"
                       autoComplete="off"
                       id="retention-time-days"
                       value={retentionTimeDays} onChange={(e) => setRetentionTimeDays(e.target.value)}/>
                <span className="input-group-text">Days</span>
              </div>
            </div>

            <div className="modal-footer">
            <button type="button"
                      className="btn btn-secondary"
                      data-bs-dismiss="modal">
                Cancel
              </button>

              <button type="button"
                      className="btn btn-primary"
                      onClick={submit}
                      disabled={!readyToSubmit()}>
                {submitting
                  ? <span><i className="fa-solid fa-circle-notch fa-spin"></i> &nbsp;Saving ...</span>
                  : 'Save Changes'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  )

}
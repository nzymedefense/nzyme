import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import GnssService from "../../../services/GnssService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import ApiRoutes from "../../../util/ApiRoutes";
import CardTitleWithControls from "../../shared/CardTitleWithControls";
import moment from "moment";
import {truncate} from "../../../util/Tools";
import conditionTypeToTitle from "./conditions/GNSSConditionTypeTitleFactory";
import conditionTypeSetToDescription from "./conditions/descriptions/GNSSConditionsDescriptionFactory";

const gnssService = new GnssService();

export default function GNSSMonitoringRuleDetailsPage() {

  const {uuid} = useParams();
  const [organizationId, tenantId] = useSelectedTenant();

  const [rule, setRule] = useState(null);

  useEffect(() => {
    gnssService.findMonitoringRule(uuid, organizationId, tenantId, setRule)
  }, [uuid]);

  const tapsTable = () => {
    if (!rule.taps) {
      return (
          <div className="alert alert-info mt-2 mb-0">
            This monitoring rule is using data recorded by all taps. Tap scope is not limited.
          </div>
      )
    }

    return (
        <>
          <p className="help-text">
            Only data from the following taps is monitored.
          </p>

          <ul className="mb-0">
            {rule.taps.map((tap, i) => {
              return <li key={i}>{tap.name}</li>
            })}
          </ul>
        </>
    )
  }

  const conditionsTable = () => {
    if (rule.conditions.length === 0) {
      return (
          <div className="alert alert-info mt-2 mb-0">
            No conditions defined. This rule will not detect anything.
          </div>
      )
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
            {Object.keys(rule.conditions).map((type, i) => {
              return (
                  <tr key={i}>
                    <td>{conditionTypeToTitle(type)}</td>
                    <td>{conditionTypeSetToDescription(type, rule.conditions[type], null)}</td>
                  </tr>
              )
            })}
            </tbody>
          </table>
        </>
    );
  }

  if (!rule) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.GNSS.CONSTELLATIONS}>GNSS</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.GNSS.MONITORING.INDEX}>Monitoring</a></li>
                <li className="breadcrumb-item">Rules</li>
                <li className="breadcrumb-item">{rule.name}</li>
                <li className="breadcrumb-item active" aria-current="page">Details</li>
              </ol>
            </nav>
          </div>

          <div className="col-2">
            <a href={ApiRoutes.GNSS.MONITORING.INDEX} className="btn btn-secondary float-end">Back</a>
          </div>
        </div>

        <div className="row mt-2">
          <div className="col-12">
            <h1>GNSS Monitoring Rule &quot;{rule.name}&quot;</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="row">
              <div className="col-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Description" slim={true} />

                    {rule.description ? truncate(rule.description, 100, true)
                        : <span className="text-muted">n/a</span> }
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Metadata" slim={true} />

                    <dl className="mb-0">
                      <dt>Created At</dt>
                      <dd title={moment(rule.created_at).format()}>{moment(rule.created_at).fromNow()}</dd>
                      <dt>Updated At</dt>
                      <dd title={moment(rule.updated_at).format()}>{moment(rule.updated_at).fromNow()}</dd>
                    </dl>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Taps" slim={true} />

                    {tapsTable()}
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-12">
                <div className="card">
                  <div className="card-body">
                    <CardTitleWithControls title="Conditions" slim={true} />

                    {conditionsTable()}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <CardTitleWithControls title="Delete Rule" slim={true} />

                <button className="btn btn-danger mt-2" onClick={(e) => {e.preventDefault();}}>Delete Rule</button>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )
}
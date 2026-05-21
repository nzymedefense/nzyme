import React, {useEffect, useState} from "react";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";
import LoadingSpinner from "../misc/LoadingSpinner";
import LocationsService from "../../services/LocationsService";

import numeral from "numeral";
import LocationTemperature from "./shared/LocationTemperature";
import LocationWind from "./shared/LocationWind";
import LocationVisibility from "./shared/LocationVisibility";

const locationsService = new LocationsService();

export default function LocationsTable() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [locations, setLocations] = useState(null);

  useEffect(() => {
    locationsService.findAll(organizationId, tenantId, setLocations)
  }, []);

  const renderAlertCount = (count) => {
    if (count === null) {
      return <span className="text-muted">n/a</span>
    }
    if (count === 0) {
      return <span className="text-muted">None</span>;
    }
    return <span className="badge text-bg-danger">{numeral(count).format("0,0")}</span>;
  };

  if (locations === null) {
    return <LoadingSpinner />
  }

  if (locations.length === 0) {
    return <div className="alert alert-info mb-0">No locations configured. Learn more about
      locations in the <a href="https://go.nzyme.org/locations">documentation</a>.</div>
  }

  return (
    <table className="table table-sm table-hover table-striped">
      <thead>
      <tr>
        <th>Name</th>
        <th>Taps</th>
        <th>Detection Alerts</th>
        <th>Weather</th>
        <th>Severe Environmental Alerts</th>
      </tr>
      </thead>
      <tbody>
      {locations.map((l, i) => {
        return <tr key={i}>
          <td><a href="#">{l.name}</a></td>
          <td>{numeral(l.tap_count).format("0,0")}</td>
          <td>{renderAlertCount(l.alert_count)}</td>
          <td>
            <LocationTemperature environment={l.environment} />,{' '}
            <LocationWind environment={l.environment} />,{' '}
            <LocationVisibility environment={l.environment} />
          </td>
          <td>{renderAlertCount(l.environment ? l.environment.alerts.length : null)}</td>
        </tr>
      })}
      </tbody>
    </table>
  )

}
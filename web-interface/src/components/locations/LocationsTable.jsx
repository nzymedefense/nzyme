import React, {useEffect, useState} from "react";
import useSelectedTenant from "../system/tenantselector/useSelectedTenant";
import LoadingSpinner from "../misc/LoadingSpinner";
import LocationsService from "../../services/LocationsService";

import numeral from "numeral";

const locationsService = new LocationsService();

export default function LocationsTable() {

  const [organizationId, tenantId] = useSelectedTenant();

  const [locations, setLocations] = useState(null);

  useEffect(() => {
    locationsService.findAll(organizationId, tenantId, setLocations)
  }, []);

  const expand = (e, id) => {
    e.preventDefault();
  }

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
        <th>Alerts</th>
        <th>Weather</th>
        <th>Environmental Warnings</th>
      </tr>
      </thead>
      <tbody>
      {locations.map((l, i) => {
        return <tr key={i}>
          <td><a href="#" onClick={(e) => expand(e, l.id)}>{l.name}</a></td>
          <td>{numeral(l.tap_count).format("0,0")}</td>
          <td>{numeral(l.alert_count).format("0,0")}</td>
        </tr>
      })}
      </tbody>
    </table>
  )

}
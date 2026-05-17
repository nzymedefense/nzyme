import React, {useEffect, useState} from "react";
import LocationsService from "../../../services/LocationsService";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import LoadingSpinner from "../../misc/LoadingSpinner";

const locationsService = new LocationsService();

export default function LocationSelector({location, onLocationSelected}) {

  const [organizationId, tenantId] = useSelectedTenant();

  const [locations, setLocations] = useState(null);

  const [selected, setSelected] = useState(location ? location : "");

  useEffect(() => {
    locationsService.findAll(organizationId, tenantId, setLocations)
  }, []);

  if (locations === null) {
    return <LoadingSpinner />
  }

  if (locations.length === 0) {
    return <div className="alert alert-info mb-0">No locations configured.</div>
  }

  return (
    <select className="form-select"
            value={selected}
            onChange={(e) => {e.preventDefault(); onLocationSelected(e.target.value); setSelected(e.target.value);}}>
      <option value="">Select a tap location</option>
      {locations.map((location, i) => {
        return <option value={location.id} key={i}>{location.name}</option>
      })}
    </select>
  )

}
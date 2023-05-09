import React, {useEffect, useState} from "react";
import IntegrationsService from "../../../../services/IntegrationsService";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import GeoIpDetailsProxy from "./GeoIpDetailsProxy";

const integrationsService = new IntegrationsService();

function GeoIpProviderOverview() {

  const [selectedGeoIp, setSelectedGeoIp] = useState(null);
  const [geoIpSummary, setGeoIpSummary] = useState(null);

  useEffect(() => {
    integrationsService.getGeoIpSummary(function(response) {
      setGeoIpSummary(response.data);
      setSelectedGeoIp(response.data.active_provider);
    })
  }, [])

  if (!geoIpSummary || !selectedGeoIp) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <h5>Choose a provider:</h5>
        <select className="form-select" defaultValue={geoIpSummary.active_provider}
                onChange={(e) => setSelectedGeoIp(e.target.value)}>
          <option value="noop">None</option>
          <option value="ipinfo_free">IPinfo.io Free</option>
        </select>

        <GeoIpDetailsProxy provider={selectedGeoIp} summary={geoIpSummary} />
      </React.Fragment>
  )

}

export default GeoIpProviderOverview;
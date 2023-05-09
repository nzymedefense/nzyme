import React, {useEffect, useState} from "react";
import IntegrationsService from "../../../../services/IntegrationsService";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import GeoIpDetailsProxy from "./GeoIpDetailsProxy";
import geoIpProviderName from "./GeoIpProviderName";
import GeoIpProviderName from "./GeoIpProviderName";

const integrationsService = new IntegrationsService();

function GeoIpProviderOverview() {

  const [selectedProvider, setSelectedProvider] = useState(null);
  const [summary, setSummary] = useState(null);
  const [localRevision, setLocalRevision] = useState(0);

  const activateProvider = function(providerName) {
    integrationsService.activateGeoIpProvider(providerName, function() {
      setLocalRevision(localRevision+1);
    })
  }

  useEffect(() => {
    setSummary(null);
    setSelectedProvider(null);
    integrationsService.getGeoIpSummary(function(response) {
      setSummary(response.data);
      setSelectedProvider(response.data.active_provider);
    })
  }, [localRevision])

  if (!summary || !selectedProvider) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <dl>
          <dt>Active Provider:</dt>
          <dd><GeoIpProviderName id={summary.active_provider} /></dd>
        </dl>

        <h5>Choose a provider:</h5>
        <select className="form-select" defaultValue={summary.active_provider}
                onChange={(e) => setSelectedProvider(e.target.value)}>
          <option value="noop">None</option>
          <option value="ipinfo_free">IPinfo.io Free</option>
        </select>

        <hr className="mt-3" />

        <GeoIpDetailsProxy activateProvider={activateProvider}
                           provider={selectedProvider}
                           activeProvider={summary.active_provider} />
      </React.Fragment>
  )

}

export default GeoIpProviderOverview;
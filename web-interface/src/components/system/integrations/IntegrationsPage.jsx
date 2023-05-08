import React, {useEffect, useState} from "react";

function IntegrationsPage() {

  const [selectedGeoIp, setSelectedGeoIp] = useState(null);
  const [geoIpConfiguration, setGeoIpConfiguration] = useState(null);

  useEffect(() => {

  })

  // MOVE to own component, check if info pulled, loading etc.

  return (
      <div>
        <div className="row">
          <div className="col-md-12">
            <h1>Integrations</h1>
          </div>
        </div>

        <div className="row">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Geo IP</h3>

                <p>
                  Geo IP integrations let nzyme translate IP addresses into corresponding geo information like country,
                  city or even  longitude and latitude as well as other related metadata.
                </p>

                <select className="form-select" defaultValue={'default'}
                        onChange={(e) => setSelectedGeoIp(e.target.value)}>
                  <option value="default" disabled>Select a GeoIP Integration</option>
                  <option value="ipinfo_free">IPinfo.io Free</option>
                </select>

                {selectedGeoIp}
              </div>
            </div>
          </div>
        </div>

      </div>
  )

}

export default IntegrationsPage;
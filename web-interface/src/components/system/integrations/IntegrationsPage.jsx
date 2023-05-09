import React from "react";
import GeoIpProviderOverview from "./geoip/GeoIpProviderOverview";

function IntegrationsPage() {

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

                <GeoIpProviderOverview />
              </div>
            </div>
          </div>
        </div>

      </div>
  )

}

export default IntegrationsPage;
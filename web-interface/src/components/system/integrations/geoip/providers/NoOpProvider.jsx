import React from "react";

function NoOpProvider(props) {

  const activeProvider = props.activeProvider;
  const activateProvider = props.activateProvider;

  const PROVIDER_ID = "noop";

  return (
      <React.Fragment>
        <div className="geoip-provider-configuration mt-3">
          <h3>Disabled Geo IP</h3>

          <p>
            All nzyme geo IP enrichment disabled.
          </p>


          { activeProvider === PROVIDER_ID ?
              null : <button className="btn btn-sm btn-warning"
                             onClick={() => activateProvider(PROVIDER_ID)}>
                Disable Geo IP Functionality
              </button> }
        </div>
      </React.Fragment>
  )

}

export default NoOpProvider;
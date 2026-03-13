import React from 'react'
import Versioncheck from './Versioncheck'
import usePageTitle from "../../util/UsePageTitle";

function VersionPage () {

  usePageTitle("Version");

  return (
        <div>
            <div className="row">
                <div className="col-md-12">
                    <h1>Version</h1>
                </div>
            </div>

            <div className="row">
                <div className="col-md-12">
                    <div className="card">
                        <div className="card-body">
                            <Versioncheck />
                        </div>
                    </div>
                </div>
            </div>

        </div>
  )
}

export default VersionPage

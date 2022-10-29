import React from "react";
import Routes from "../../util/ApiRoutes";

function RetroConfigurationPage() {

    return (
        <div>
            <div className="row">
                <div className="col-md-12">
                    <nav aria-label="breadcrumb">
                        <ol className="breadcrumb">
                            <li className="breadcrumb-item"><a href={Routes.RETRO.SEARCH.INDEX}>Retrospective</a></li>
                            <li className="breadcrumb-item active" aria-current="page">Configuration</li>
                        </ol>
                    </nav>
                </div>
            </div>

            <div className="row">
                <div className="col-md-12">
                    <h1>Configuration</h1>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-12">
                    <div className="card">
                        <div className="card-body">
                            <h3>All Settings</h3>


                        </div>
                    </div>
                </div>
            </div>
        </div>
    )

}

export default RetroConfigurationPage;
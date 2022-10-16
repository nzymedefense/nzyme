import React, {useState, useEffect} from "react";
import PluginsService from "../../services/PluginsService";
import LoadingSpinner from "../misc/LoadingSpinner";
import ServiceSummary from "./ServiceSummary";

const pluginsService = new PluginsService();

function RetroPage() {

    const [plugins, setPlugins] = useState(null);

    useEffect(() => {
        pluginsService.findInitializedPlugins(setPlugins);
    }, [setPlugins]);

    if (plugins === null) {
        return <LoadingSpinner />;
    }

    if (!plugins.includes("retroplugin")) {
        return (
            <div className="alert alert-info">
                The Retrospective plugin is not installed.
            </div>
        )
    }

    return (
        <div>
            <div className="row">
                <div className="col-md-12">
                    <h1>Retrospective</h1>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3>Service Summary</h3>
                            <ServiceSummary />
                        </div>
                    </div>
                </div>
            </div>

        </div>
    )

}

export default RetroPage;
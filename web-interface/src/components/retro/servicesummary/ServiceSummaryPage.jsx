import React, {useEffect, useState} from "react";
import Routes from "../../../util/ApiRoutes";
import ServiceSummaryOverview from "./ServiceSummaryOverview";
import LoadingSpinner from "../../misc/LoadingSpinner";
import RetroService from "../../../services/RetroService";

const retroService = new RetroService();

function ServiceSummaryPage() {

    const [summary, setSummary] = useState(null);

    useEffect(() => {
        retroService.getServiceSummary(setSummary);
    }, [setSummary]);

    if (!summary) {
        return <LoadingSpinner />;
    }

    return (
        <div>
            <div className="row">
                <div className="col-md-12">
                    <nav aria-label="breadcrumb">
                        <ol className="breadcrumb">
                            <li className="breadcrumb-item"><a href={Routes.RETRO.SEARCH.INDEX}>Retrospective</a></li>
                            <li className="breadcrumb-item active" aria-current="page">Service Summary</li>
                        </ol>
                    </nav>
                </div>
            </div>

            <div className="row">
                <div className="col-md-12">
                    <h1>Service Summary</h1>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-12">
                    <div className="card">
                        <div className="card-body">
                            <h3>Overview</h3>

                            <ServiceSummaryOverview summary={summary} />
                        </div>
                    </div>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-12">
                    <div className="card">
                        <div className="card-body">
                            <h3>Raw Data</h3>

                            <pre className="json">
                                {JSON.stringify(summary, null, 2)}
                            </pre>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )

}

export default ServiceSummaryPage;
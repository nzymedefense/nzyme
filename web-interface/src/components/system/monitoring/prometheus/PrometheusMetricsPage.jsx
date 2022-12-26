import React, {useEffect, useState} from "react";
import Routes from "../../../../util/ApiRoutes";
import ConfigurationValue from "../../../configuration/ConfigurationValue";
import MonitoringService from "../../../../services/MonitoringService";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import ConfigurationModal from "../../../configuration/modal/ConfigurationModal";

const monitoringService = new MonitoringService();

function PrometheusMetricsPage() {

    const [configuration, setConfiguration] = useState(null);
    const [localRevision, setLocalRevision] = useState(0);

    useEffect(() => {
        monitoringService.getPrometheusExporterConfiguration(setConfiguration);
    }, [localRevision]);

    if (!configuration) {
        return <LoadingSpinner />;
    }

    return (
        <div className="row">
            <div className="col-md-10">
                <nav aria-label="breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item"><a href={Routes.SYSTEM.MONITORING.INDEX}>Monitoring &amp; Metrics</a></li>
                        <li className="breadcrumb-item">Exporters</li>
                        <li className="breadcrumb-item active" aria-current="page">Prometheus</li>
                    </ol>
                </nav>
            </div>
            <div className="col-md-2">
                <a className="btn btn-primary float-end" href={Routes.SYSTEM.TAPS.INDEX}>Back</a>
            </div>

            <div className="row">
                <div className="col-md-12">
                    <h1>Prometheus Exporter</h1>
                </div>
            </div>

            <div className="row">
                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3>Configuration</h3>

                            <table className="table table-sm table-hover table-striped">
                                <thead>
                                <tr>
                                    <th>Configuration</th>
                                    <th>Value</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td>REST Report Enabled</td>
                                    <td>
                                        <ConfigurationValue value={configuration.prometheus_rest_report_enabled.value}
                                                            configKey={configuration.prometheus_rest_report_enabled.key}
                                                            required={true}
                                                            awaitingRestart={false}
                                                            boolean={true} />
                                    </td>
                                    <td>
                                        <ConfigurationModal config={configuration.prometheus_rest_report_enabled}
                                                            setGlobalConfig={setConfiguration}
                                                            changeWarning={false}
                                                            setLocalRevision={setLocalRevision}
                                                            dbUpdateCallback={monitoringService.updateConfiguration} />
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    )

}

export default PrometheusMetricsPage;
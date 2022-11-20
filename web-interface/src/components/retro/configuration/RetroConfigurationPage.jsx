import React, {useCallback, useEffect, useState} from "react";
import Routes from "../../../util/ApiRoutes";
import InlineHelp from "../../misc/InlineHelp";
import RetroService from "../../../services/RetroService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import ConfigurationModal from "../../configuration/modal/ConfigurationModal";
import ConfigurationValue from "../../configuration/ConfigurationValue";
import IncompleteConfigurationWarning from "./IncompleteConfigurationWarning";
import RestartRequiredWarning from "../../configuration/RestartRequiredWarning";

const retroService = new RetroService();

function RetroConfigurationPage() {

    const [configuration, setConfiguration] = useState(null);
    const [serviceSummary, setServiceSummary] = useState(null);
    const [localRevision, setLocalRevision] = useState(0);

    useEffect(() => {
        retroService.getConfiguration(setConfiguration);
        retroService.getServiceSummary(setServiceSummary);
    }, [localRevision]);

    if (!configuration || !serviceSummary) {
        return <LoadingSpinner />;
    }

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

            <IncompleteConfigurationWarning show={!serviceSummary.is_configured} />
            <RestartRequiredWarning awaiting={serviceSummary.config_awaiting_restart} />

            <div className="row mt-3">
                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3>Data Store</h3>

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
                                    <td>Type</td>
                                    <td>Filesystem <InlineHelp text="This is currently the only available retro storage type." /></td>
                                    <td></td>
                                </tr>
                                <tr>
                                    <td>Fileystem Path</td>
                                    <td>
                                        <ConfigurationValue value={configuration.writer_fs_base_path.value} configKey={configuration.writer_fs_base_path.key} required={true} awaitingRestart={serviceSummary.config_awaiting_restart} />{' '}
                                        {configuration.writer_fs_base_path_computed_absolute ? "(" + configuration.writer_fs_base_path_computed_absolute + ")" : null }
                                    </td>
                                    <td>
                                        <ConfigurationModal config={configuration.writer_fs_base_path} setGlobalConfig={setConfiguration} setLocalRevision={setLocalRevision} />
                                    </td>
                                </tr>
                                <tr>
                                    <td>Searcher Threads</td>
                                    <td><ConfigurationValue value={configuration.searcher_fs_threadpool.value} configKey={configuration.searcher_fs_threadpool.key} required={true} awaitingRestart={serviceSummary.config_awaiting_restart} /></td>
                                    <td>
                                        <ConfigurationModal config={configuration.searcher_fs_threadpool} setGlobalConfig={setConfiguration} changeWarning={true}  setLocalRevision={setLocalRevision} />
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

export default RetroConfigurationPage;
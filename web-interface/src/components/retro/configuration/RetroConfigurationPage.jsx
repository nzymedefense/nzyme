import React, {useEffect, useState} from "react";
import Routes from "../../../util/ApiRoutes";
import InlineHelp from "../../misc/InlineHelp";
import RetroService from "../../../services/RetroService";
import LoadingSpinner from "../../misc/LoadingSpinner";
import ConfigurationModal from "../../configuration/ConfigurationModal";

const retroService = new RetroService();

function RetroConfigurationPage() {

    const [configuration, setConfiguration] = useState(null);

    useEffect(() => {
        retroService.getConfiguration(setConfiguration);
    }, [setConfiguration]);

    if (!configuration) {
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
                                    <td>{configuration.writer_fs_base_path.value} ({configuration.writer_fs_base_path_computed_absolute})</td>
                                    <td>
                                        <ConfigurationModal config={configuration.writer_fs_base_path} />
                                    </td>
                                </tr>
                                <tr>
                                    <td>Searcher Threads</td>
                                    <td>{configuration.searcher_fs_threadpool.value}</td>
                                    <td>
                                        <a href="#" data-bs-toggle="modal" data-bs-target="#configuration-dialog">Edit</a>
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
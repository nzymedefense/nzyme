import React, {useEffect, useState} from "react";
import Routes from "../../../util/ApiRoutes";
import InlineHelp from "../../misc/InlineHelp";
import RetroService from "../../../services/RetroService";
import LoadingSpinner from "../../misc/LoadingSpinner";

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
                <div className="col-md-12">
                    <div className="card">
                        <div className="card-body">
                            <h3>Data Store</h3>

                            <dl>
                                <dt>Type</dt>
                                <dd>
                                    Filesystem <InlineHelp text="This is currently the only available retro storage type." />
                                </dd>

                                <dt>Filesystem Path</dt>
                                <dd>
                                    {configuration.writer_fs_base_path} ({configuration.writer_fs_base_path_computed_absolute})
                                </dd>

                                <dt>Searcher Threads</dt>
                                <dd>
                                    {configuration.searcher_fs_threadpool}
                                </dd>
                            </dl>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )

}

export default RetroConfigurationPage;
import React from "react";

function ConfigurationModal(props) {

    // TODO build entirely from props.config, build list in config page from config response, too.

    return (
        <React.Fragment>
            <a href="#"
               data-bs-toggle="modal"
               data-bs-target="#configuration-dialog">
                Edit
            </a>

            <div className="modal fade" id="configuration-dialog" data-bs-backdrop="static" data-bs-keyboard="false"
                 tabIndex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title" id="staticBackdropLabel">Edit Configuration Value</h5>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"
                                    aria-label="Close"></button>
                        </div>
                        <div className="modal-body">
                            <label htmlFor="config-value" className="form-label">{props.config.key}</label>

                            <input type="text" className="form-control" id="config-value" />

                            <div id="passwordHelpBlock" className="form-text">
                                Default Value: {props.config.default_value ? props.config.default_value : "None" }
                            </div>

                            <div className="alert alert-primary mt-2">
                                Changing this configuration does <strong>not</strong> require a restart of nzyme.
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                            <button type="button" className="btn btn-primary">Save Changes</button>
                        </div>
                    </div>
                </div>
            </div>
        </React.Fragment>
    )

}

export default ConfigurationModal;
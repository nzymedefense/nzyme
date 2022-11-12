import React from "react";
import DefaultValue from "./DefaultValue";
import RestartRequired from "./RestartRequired";

function ConfigurationModal(props) {

    // TODO build entirely from props.config, build list in config page from config response, too.

    return (
        <React.Fragment>
            <a href="web-interface/src/components/configuration/modal/ConfigurationModal#"
               data-bs-toggle="modal"
               data-bs-target={"#configuration-dialog-" + props.config.key}>
                Edit
            </a>

            <div className="modal fade" id={"configuration-dialog-" + props.config.key} data-bs-keyboard="true"
                 tabIndex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title" id="staticBackdropLabel">Edit Configuration Value</h5>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"
                                    aria-label="Close"></button>
                        </div>
                        <div className="modal-body">
                            <label htmlFor="config-value" className="form-label">
                                {props.config.key_human_readable}
                            </label>

                            <input type="text" className="form-control" id="config-value" value={props.config.value} />

                            <div id="passwordHelpBlock" className="form-text">
                                <DefaultValue value={props.config.default_value} />
                            </div>

                            <RestartRequired required={props.config.requires_restart} />
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
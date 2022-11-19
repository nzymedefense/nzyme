import React, {useCallback, useEffect, useState} from "react";
import DefaultValue from "./DefaultValue";
import RestartRequired from "./RestartRequired";
import ConfigurationInputField from "./ConfigurationInputField";
import RetroService from "../../../services/RetroService";

const retroService = new RetroService();

function ConfigurationModal(props) {

    const [formDisabled, setFormDisabled] = useState(true);
    const [inputValue, setInputValue] = useState(props.config.value);

    useEffect(() => {
        const config = props.config;

        if (inputValue === undefined) {
            setFormDisabled(true);
        } else {
            if (config.constraints && config.constraints.length > 0) {
                for (const constraint of config.constraints) {
                    const cData = constraint.data;

                    switch (constraint.type) {
                        case "STRING_LENGTH":
                            setFormDisabled(inputValue < cData.min || inputValue > cData.max)
                            break;
                        case "NUMBER_RANGE":
                            const numValue = parseInt(inputValue, 10);
                            setFormDisabled(isNaN(numValue) || numValue < cData.min || numValue > cData.max);
                            break;
                        default:
                            setFormDisabled(true);
                    }
                }
            } else {
                setFormDisabled(false);
            }
        }
    }, [inputValue, props.config])

    const updateValue = useCallback(() => {
        const updated = [];
        updated[props.config.key] = inputValue;

        //retroService.setConfiguration(updated)
    }, [inputValue, props]);

    return (
        <React.Fragment>
            <a href="web-interface/src/components/configuration/modal/ConfigurationModal#"
               data-bs-toggle="modal"
               data-bs-target={"#configuration-dialog-" + props.config.key}>
                Edit
            </a>

            <div className="modal fade configuration-dialog" id={"configuration-dialog-" + props.config.key} data-bs-keyboard="true"
                 tabIndex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">Edit Configuration Value</h5>
                            <button type="button" data-bs-dismiss="modal" aria-label="Close" className="modal-close">
                                <i className="fa-solid fa-xmark"></i>
                            </button>
                        </div>

                        <div className="modal-body">
                            <div>
                                <label htmlFor="config-value" className="form-label float-start">
                                    {props.config.key_human_readable}
                                </label>

                                <span className="float-end">
                                    {props.config.help_tag ?
                                        <a href={"https://go.nzyme.org/" + props.config.help_tag} className="configuration-help" target="_blank">
                                            Help
                                        </a>
                                        : null }
                                </span>
                            </div>

                            <ConfigurationInputField type={props.config.value_type} value={inputValue} setValue={setInputValue} />

                            <div className="form-text">
                                <DefaultValue value={props.config.default_value} />
                            </div>

                            <RestartRequired required={props.config.requires_restart} />
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                            <button type="button"
                                    className="btn btn-primary"
                                    onClick={updateValue}
                                    disabled={formDisabled}>
                                Save Changes
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </React.Fragment>
    )

}

export default ConfigurationModal;
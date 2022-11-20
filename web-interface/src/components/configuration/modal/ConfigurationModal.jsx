import React, {useCallback, useEffect, useState} from "react";
import DefaultValue from "./DefaultValue";
import RestartRequired from "./RestartRequired";
import ConfigurationInputField from "./ConfigurationInputField";
import RetroService from "../../../services/RetroService";
import ConfigurationSubmitButton from "./ConfigurationSubmitButton";
import ConfigurationCloseButton from "./ConfigurationCloseButton";
import ConfigurationUpdateFailedWarning from "./ConfigurationUpdateFailedWarning";
import ThreatLevelMidnight from "./ThreatLevelMidnight";

const retroService = new RetroService();

function ConfigurationModal(props) {

    const [formDisabled, setFormDisabled] = useState(true);
    const [formSubmitting, setFormSubmitting] = useState(false);
    const [formSubmittedSuccessfully, setFormSubmittedSuccessfully] = useState(false);
    const [formSubmittedWithError, setFormSubmittedWithError] = useState(false);
    const [changeWarningAck, setChangeWarningAck] = useState(false);

    const [inputValue, setInputValue] = useState(props.config.value);

    useEffect(() => {
        const config = props.config;

        if (props.changeWarning && !changeWarningAck) {
            setFormDisabled(true);
            return;
        }

        if (inputValue === undefined || inputValue === props.config.value) {
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
    }, [inputValue, changeWarningAck, props.config])

    const updateValue = useCallback(() => {
        setFormSubmittedWithError(false);
        setFormSubmitting(true);
        setFormDisabled(true);

        retroService.updateConfiguration({
            [props.config.key]: inputValue
        }, function () {
            setFormSubmitting(false);
            setFormDisabled(false);
            setFormSubmittedSuccessfully(true);
        }, function () {
            setFormSubmittedWithError(true);
            setFormSubmitting(false);
            setFormDisabled(false);
        });
    }, [inputValue, props]);

    const resetOnCancel = useCallback(() => {
        setInputValue(props.config.value);
        setChangeWarningAck(false);
        setFormSubmittedWithError(false);
    }, [props.config]);

    const resetOnFinish = useCallback(() => {
        setChangeWarningAck(false);
        setFormSubmittedSuccessfully(false);
    }, []);

    return (
        <React.Fragment>
            <a href="web-interface/src/components/configuration/modal/ConfigurationModal#"
               data-bs-toggle="modal"
               data-bs-target={"#configuration-dialog-" + props.config.key}>
                Edit
            </a>

            <div className="modal configuration-dialog" id={"configuration-dialog-" + props.config.key}
                 data-bs-keyboard="false" data-bs-backdrop="static" tabIndex="-1"
                 aria-labelledby="staticBackdropLabel" aria-hidden="true">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">Edit Configuration Value</h5>
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
                            <ConfigurationUpdateFailedWarning failed={formSubmittedWithError} />
                            <ThreatLevelMidnight
                                enabled={props.changeWarning}
                                helpTag={props.config.help_tag}
                                configKey={props.config.key}
                                changeWarningAck={changeWarningAck}
                                setChangeWarningAck={setChangeWarningAck} />
                        </div>

                        <div className="modal-footer">
                            <ConfigurationCloseButton
                                submitting={formSubmitting}
                                submittedSuccessfully={formSubmittedSuccessfully}
                                onClick={resetOnCancel} />

                            <ConfigurationSubmitButton
                                onClick={updateValue}
                                disabled={formDisabled}
                                submitting={formSubmitting}
                                submittedSuccessfully={formSubmittedSuccessfully}
                                onFinishedClick={resetOnFinish} />
                        </div>
                    </div>
                </div>
            </div>
        </React.Fragment>
    )

}

export default ConfigurationModal;
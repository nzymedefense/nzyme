import React from "react";
import ConfigurationValueHelp from "./ConfigurationValueHelp";

function InputLabel(props) {

    if (props.config.value_type === "BOOLEAN") {
        return <ConfigurationValueHelp helpTag={props.config.help_tag} />;
    }

    return (
        <div>
            <label htmlFor="config-value" className="form-label float-start">
                {props.config.key_human_readable}
            </label>

            <ConfigurationValueHelp helpTag={props.config.help_tag} />
        </div>
    )

}

export default InputLabel;
import React from "react";

function InputLabel(props) {

    if (props.config.value_type === "BOOLEAN") {
        return null;
    }

    return (
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
    )

}

export default InputLabel;
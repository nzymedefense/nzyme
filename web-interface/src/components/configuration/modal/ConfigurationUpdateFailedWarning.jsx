import React from "react";

function ConfigurationUpdateFailedWarning(props) {

    if (props.failed) {
        return (
            <div className="alert alert-danger">
                <i className="fa-solid fa-shake fa-triangle-exclamation"></i> Could not save the configuration. Please
                check your nzyme log file for errors. <a href="https://go.nzyme.org/config-update-failed" target="_blank">Help</a>
            </div>
        )
    } else {
        return null;
    }

}

export default ConfigurationUpdateFailedWarning;
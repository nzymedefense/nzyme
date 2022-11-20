import React from "react";

function RestartRequiredWarning(props) {

    if (Array.isArray(props.awaiting) && props.awaiting.length > 0) {
        return (
            <div className="alert alert-info">
                Values that have been changed and require a restart to take effect are marked in red. Please restart
                nzyme or change the values back to their original value if you changed your mind.z
            </div>
        )
    } else {
        return null;
    }

}

export default RestartRequiredWarning;
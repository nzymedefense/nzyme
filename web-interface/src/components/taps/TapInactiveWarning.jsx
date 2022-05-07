import React from "react";

function TapInactiveWarning(props) {

    if (!props.tap.active) {
        return (
            <div className="alert alert-danger">
                <i className="fa-solid fa-triangle-exclamation"></i> This tap has not recently reported data and is
                offline. Some data you see on this page is likely outdated.
            </div>
        )
    } else {
        return null;
    }

}

export default TapInactiveWarning;
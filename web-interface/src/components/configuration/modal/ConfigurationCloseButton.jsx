import React from "react";

function ConfigurationCloseButton(props) {

    if (props.submittedSuccessfully) {
        return null;
    } else {
        return (
            <button type="button"
                    className="btn btn-secondary"
                    data-bs-dismiss="modal"
                    onClick={props.onClick}
                    disabled={props.submitting}>
                Cancel
            </button>
        )
    }

}

export default ConfigurationCloseButton;
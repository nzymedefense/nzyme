import React from "react";

function EncryptedConfigurationValue(props) {

    if (props.isSet) {
        if (Array.isArray(props.awaitingRestart) && props.awaitingRestart.includes(props.configKey)) {
            return (
                <React.Fragment>
                    <i>[configured, encrypted]</i>{' '}
                    <strong className="text-danger">(restart required)</strong>
                </React.Fragment>
            )
        } else {
            return <i>[configured, encrypted]</i>
        }
    } else {
        if (props.required) {
            return (
                <React.Fragment>
                    <i>(none)</i>{' '}
                    <strong className="text-danger">(required setting)</strong>
                </React.Fragment>
            )
        } else {
            return <i>(none)</i>
        }
    }

}

export default EncryptedConfigurationValue;
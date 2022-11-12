import React from 'react';

function RestartRequired(props) {

    if (props.required) {
        return (
            <div className="alert alert-warning mt-2">
                Changing this configuration <strong>does require a restart of nzyme</strong>.
            </div>
        )
    } else {
        return (
            <div className="alert alert-primary mt-2">
                Changing this configuration does <strong>not</strong> require a restart of nzyme.
            </div>
        )
    }

}

export default RestartRequired;
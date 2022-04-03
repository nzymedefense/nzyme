import React from 'react';

function DarkMode(props) {

    if (props.enabled) {
        return (
            <link rel="stylesheet" href="/static/css/dark.css" />
        )
    } else {
        return null;
    }

}

export default DarkMode;
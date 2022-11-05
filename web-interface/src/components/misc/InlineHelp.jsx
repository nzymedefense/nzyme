import React from "react";

function InlineHelp(props) {

    return (
        <i className="fa-regular fa-circle-question inline-help" title={props.text}></i>
    )

}

export default InlineHelp;
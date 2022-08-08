import React from "react";
import numeral from "numeral";

/*
 * This is the worst thing I've done yet
 */
function FormattedGauge(props) {

    if (props.name.includes("bytes")) {
        return <span title={props.value}>{numeral(props.value).format("0b")}</span>;
    }

    if (props.value.toString().includes(".")) {
        return <span title={props.value}>{numeral(props.value).format("0,0.00")}</span>;
    }

    return <span title={props.value}>{numeral(props.value).format("0,0")}</span>;

}

export default FormattedGauge;
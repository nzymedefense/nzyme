import React from "react";
import numeral from "numeral";

/*
 * This is the worst thing I've done yet
 */
function FormattedGauge(props) {

    let format = "0,0.[00]";
    let rawValue = props.value;
    let value = props.value;
    let suffix = "";

    if (props.name.includes("percent")) {
        format = "0,0.[00] %";
        value = value/100;
    } else if(props.name.includes("bytes")) {
        format = "0,0.00 b";
    } else if(props.name.includes("bit_sec")) {
        format = "0,0.00";
        if (value > 1_000_000_000) {
            value = value/1024/1024/1024;
            suffix = "Gbit/sec";
        } else {
            value = value/1024/1024;
            suffix = "Mbit/sec";
        }
    }

    return <span title={rawValue}>{numeral(value).format(format)} {suffix}</span>;

}

export default FormattedGauge;
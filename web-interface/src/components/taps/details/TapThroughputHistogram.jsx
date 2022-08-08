import React from "react";
import SimpleLineChart from "../../charts/SimpleLineChart";

function TapThroughputHistogram(props) {

    return <SimpleLineChart
        height={200}
        data={formatData(props.data.values)}
        customMarginLeft={85}
        customMarginRight={25}
        ticksuffix={" Mbit/sec"}
        tickformat={"f"}
    />

}

function formatData(data) {
    const result = {};

    Object.keys(data).sort().forEach(function (key) {
        result[key] = data[key].average/1024/1024;
    })

    return result;
}

export default TapThroughputHistogram;
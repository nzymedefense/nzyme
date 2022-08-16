import React from "react";
import SimpleLineChart from "../../../../charts/SimpleLineChart";
import LoadingSpinner from "../../../../misc/LoadingSpinner";

function TapMetricsGaugeChart(props) {

    if (!props.data) {
        return <LoadingSpinner />
    }

    return <SimpleLineChart
        height={200}
        data={formatData(props.data.values, props.conversion)}
        customMarginLeft={85}
        customMarginRight={25}
        ticksuffix={props.valueType ? " " + props.valueType : undefined}
        tickformat={"f"}
    />

}

// todo detect byte etc

function formatData(data, conversion) {
    const result = {};

    Object.keys(data).sort().forEach(function (key) {
        if (conversion) {
            result[key] = conversion(data[key].average);
        } else {
            result[key] = data[key].average;
        }
    })

    return result;
}

export default TapMetricsGaugeChart;
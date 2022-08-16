import React, {useEffect, useState} from "react";
import TapMetricsGaugeChart from "./charts/TapMetricGaugeChart";
import TapsService from "../../../../services/TapsService";

const tapsService = new TapsService();

function fetchData(tapName, metricName, setData) {
    tapsService.findGaugeMetricHistogramOfTap(tapName, metricName, setData);
}

function throughputConversion(x) {
    return x/1024/1024;
}

function byteConversion(x) {
    // if > 100000 ...
    return x;
}

function TapMetricsChartProxy(props) {

    const [data, setData] = useState(null);

    useEffect(() => {
        fetchData(props.tapName, props.name, setData);
        const id = setInterval(() => fetchData(props.tapName, props.name, setData), 5000);
        return () => clearInterval(id);
    }, [props, setData]);

    if (props.type === "gauge") {
        let conversion = undefined;
        let valueType = undefined;

        if (props.name.includes("bit") && props.name.includes("sec")) {
            conversion = throughputConversion;
            valueType = "Mbit/sec";
        }

        if (props.name.includes("byte")) {
            conversion = byteConversion();
            valueType = "FOO";
        }

        return (
            <TapMetricsGaugeChart data={data} conversion={conversion} valueType={valueType} />
        )
    } else {
        return <div className="alert alert-danger">Unknown metric type.</div>
    }

}

export default TapMetricsChartProxy;
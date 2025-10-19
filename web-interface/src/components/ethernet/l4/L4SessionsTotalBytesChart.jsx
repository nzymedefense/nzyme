import React from "react";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import MultiLineChart from "../../widgets/charts/MultiLineChart";

function byteConversion (x) {
  return x / 1024 / 1024
}

export default function L4SessionsTotalBytesChart({statistics, setTimeRange}) {

  if (!statistics) {
    return <GenericWidgetLoadingSpinner height={200} />
  }

  const formatData = data => {
    const bytesTcp = {};
    const bytesUdp = {};
    const bytesTotal = {}

    // Sort timestamps so the x‑axis is in order
    Object.keys(data).sort().forEach(timestamp => {
      const row = data[timestamp] || {};

      const tcp = row["bytes_tcp"] ?? 0;
      const udp = row["bytes_udp"] ?? 0;

      bytesTcp[timestamp] = byteConversion(tcp);
      bytesUdp[timestamp] = byteConversion(udp);
      bytesTotal[timestamp] = byteConversion(tcp + udp);
    });

    return {
      bytes_tcp: bytesTcp,
      bytes_udp: bytesUdp,
      bytes_total: bytesTotal
    };
  };

  return <MultiLineChart
      height={200}
      lineWidth={1}
      customMarginBottom={35}
      customMarginLeft={75}
      seriesNames={{bytes_tcp: "TCP Bytes", bytes_udp: "UDP Bytes", bytes_total: "Total Bytes" }}
      data={formatData(statistics.statistics)}
      ticksuffix={" MB"}
      tickformat={".2~f"}
      setTimeRange={setTimeRange}
  />

}
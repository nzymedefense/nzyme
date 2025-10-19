import React from "react";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import ByteSizeCard from "../../widgets/presentation/ByteSizeCard";

export default function L4SessionsNumbers({ statistics, timeRange, setTimeRange }) {

  if (!statistics) {
    return (
        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <GenericWidgetLoadingSpinner height={135} />
              </div>
            </div>
          </div>
        </div>
    )
  }

  return (
      <div className="row mt-3">
        <div className="col-md-3">
            <ByteSizeCard title="Total Traffic"
                          setTimeRange={setTimeRange}
                          timeRange={timeRange}
                          value={statistics.numbers.bytes_tcp + statistics.numbers.bytes_udp} />
        </div>

        <div className="col-md-3">
          <ByteSizeCard title="Total Internal Traffic"
                        setTimeRange={setTimeRange}
                        timeRange={timeRange}
                        value={statistics.numbers.bytes_internal_tcp + statistics.numbers.bytes_internal_udp} />
        </div>
      </div>
  )

}
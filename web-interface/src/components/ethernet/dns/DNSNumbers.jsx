import React from 'react'
import NumberCard from '../../widgets/presentation/NumberCard'
import ByteSizeCard from '../../widgets/presentation/ByteSizeCard'

function DNSNumbers (props) {
  let totalPackets, totalTrafficBytes, totalNxDomains;

  if (props.data && props.data.traffic_summary) {
    totalPackets = props.data.traffic_summary.total_packets;
    totalTrafficBytes = props.data.traffic_summary.total_traffic_bytes;
    totalNxDomains = props.data.traffic_summary.total_nxdomains;
  }

  return (
        <React.Fragment>
            <div className="col-md-4">
                <NumberCard title="Queries &amp; Responses"
                            fixedAppliedTimeRange={props.fixedAppliedTimeRange}
                            value={totalPackets} />
            </div>

            <div className="col-md-4">
                <ByteSizeCard title="Traffic"
                              fixedAppliedTimeRange={props.fixedAppliedTimeRange}
                              value={totalTrafficBytes} />
            </div>

            <div className="col-md-4">
                <NumberCard title="NXDOMAIN Answers"
                            fixedAppliedTimeRange={props.fixedAppliedTimeRange}
                            value={totalNxDomains} />
            </div>
        </React.Fragment>
  )
}

export default DNSNumbers

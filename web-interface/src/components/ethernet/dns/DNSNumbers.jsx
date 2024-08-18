import React, {useContext, useEffect, useState} from 'react'
import NumberCard from '../../widgets/presentation/NumberCard'
import ByteSizeCard from '../../widgets/presentation/ByteSizeCard'
import {Presets} from "../../shared/timerange/TimeRange";
import {TapContext} from "../../../App";
import DNSService from "../../../services/ethernet/DNSService";

const dnsService = new DNSService()

export default function DNSNumbers() {

  const [packets, setPackets] = useState(null);
  const [traffic, setTraffic] = useState(null);
  const [nxdomains, setNxdomains] = useState(null);

  const [packetsTimeRange, setPacketsTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [trafficTimeRange, setTrafficTimeRange] = useState(Presets.RELATIVE_HOURS_24);
  const [nxdomainsTimeRange, setNxdomainsTimeRange] = useState(Presets.RELATIVE_HOURS_24);

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  useEffect(() => {
    setPackets(null);
    dnsService.getGlobalStatisticsValue(packetsTimeRange, selectedTaps, "packets", setPackets);
  }, [selectedTaps, packetsTimeRange]);

  useEffect(() => {
    setTraffic(null);
    dnsService.getGlobalStatisticsValue(trafficTimeRange, selectedTaps, "traffic", setTraffic);
  }, [selectedTaps, trafficTimeRange]);

  useEffect(() => {
    setNxdomains(null);
    dnsService.getGlobalStatisticsValue(nxdomainsTimeRange, selectedTaps, "nxdomains", setNxdomains);
  }, [selectedTaps, nxdomainsTimeRange]);

  return (
        <React.Fragment>
            <div className="col-md-4">
                <NumberCard title="Queries &amp; Responses"
                            setTimeRange={setPacketsTimeRange}
                            timeRange={packetsTimeRange}
                            value={packets} />
            </div>

            <div className="col-md-4">
                <ByteSizeCard title="Traffic"
                              setTimeRange={setTrafficTimeRange}
                              timeRange={trafficTimeRange}
                              value={traffic} />
            </div>

            <div className="col-md-4">
                <NumberCard title="NXDOMAIN Answers"
                            helpLink="https://go.nzyme.org/ethernet-dns-nxdomain"
                            setTimeRange={setNxdomainsTimeRange}
                            timeRange={nxdomainsTimeRange}
                            value={nxdomains} />
            </div>
        </React.Fragment>
  )
}
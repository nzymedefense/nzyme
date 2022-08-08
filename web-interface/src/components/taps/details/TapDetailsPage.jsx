import React, {useEffect, useState} from "react";
import TapsService from "../../../services/TapsService";
import {useParams} from "react-router-dom";
import LoadingSpinner from "../../misc/LoadingSpinner";
import moment from "moment";
import Routes from "../../../util/ApiRoutes";
import numeral from "numeral";
import byteAverageToMbit from "../../../util/Tools";
import Buses from "./Buses";
import TapInactiveWarning from "./TapInactiveWarning";
import CaptureConfiguration from "../capture/CaptureConfiguration";
import TapMetrics from "./metrics/TapMetrics";
import TapThroughputHistogram from "./TapThroughputHistogram";

const tapsService = new TapsService();

function fetchData(tapName, setTap, setTapMetrics, setThroughputHistogram) {
    tapsService.findTap(tapName, setTap);
    tapsService.findMetricsOfTap(tapName, setTapMetrics);
    tapsService.findGaugeMetricHistogramOfTap(tapName, "system.captures.throughput_bit_sec", setThroughputHistogram);
}

function TapDetailsPage() {

    const { tapName } = useParams();

    const [tap, setTap] = useState(null);
    const [tapMetrics, setTapMetrics] = useState(null);
    const [throughputHistogram, setThroughputHistogram] = useState(null);

    useEffect(() => {
        fetchData(tapName, setTap, setTapMetrics, setThroughputHistogram);
        const id = setInterval(() => fetchData(tapName, setTap, setTapMetrics, setThroughputHistogram), 5000);
        return () => clearInterval(id);
    }, [tapName, setTap, setTapMetrics, setThroughputHistogram]);

    if (!tap) {
        return <LoadingSpinner />
    }

    return (

        <div>
            <div className="row">
                <div className="col-md-10">
                    <nav aria-label="breadcrumb">
                        <ol className="breadcrumb">
                            <li className="breadcrumb-item"><a href={Routes.SYSTEM.TAPS.INDEX}>Taps</a></li>
                            <li className="breadcrumb-item active" aria-current="page">{tap.name}</li>
                        </ol>
                    </nav>
                </div>
                <div className="col-md-2">
                    <a className="btn btn-primary float-end" href={Routes.SYSTEM.TAPS.INDEX}>Back</a>
                </div>
            </div>

            <div className="row">
                <h1>Tap "{tap.name}"</h1>
            </div>

            <TapInactiveWarning tap={tap} />

            <div className="row mt-3">
                <div className="col-md-4">
                    <div className="card">
                        <div className="card-body">
                            <h3>Throughput</h3>
                            <dl>
                                <dt>Throughput</dt>
                                <dd>
                                    {numeral(tap.processed_bytes.average/10).format('0 b')}/sec ({byteAverageToMbit(tap.processed_bytes.average)})
                                </dd>

                                <dt>Total data processed since last restart</dt>
                                <dd>
                                    {numeral(tap.processed_bytes.total).format('0.0 b')}
                                </dd>
                            </dl>
                        </div>
                    </div>
                </div>

                <div className="col-md-4">
                    <div className="card">
                        <div className="card-body">
                            <h3>Metrics</h3>
                            <dl>
                                <dt>CPU Load</dt>
                                <dd>
                                    {numeral(tap.cpu_load).format('0.0')}%
                                </dd>

                                <dt>System-Wide Memory Usage</dt>
                                <dd>
                                    {numeral(tap.memory_used).format('0 b')} / {numeral(tap.memory_total).format('0 b')} ({numeral(tap.memory_used/tap.memory_total*100).format('0.0')}%)
                                </dd>
                            </dl>
                        </div>
                    </div>
                </div>

                <div className="col-md-4">
                    <div className="card">
                        <div className="card-body">
                            <h3>Details</h3>
                            <dl>
                                <dt>Last Seen</dt>
                                <dd>
                                    <span title={moment(tap.updated_at).format()}>
                                        {moment(tap.updated_at).fromNow()}
                                    </span>
                                </dd>

                                <dt>First Seen</dt>
                                <dd>
                                    <span title={moment(tap.created_at).format()}>
                                        {moment(tap.created_at).fromNow()}
                                    </span>
                                </dd>
                            </dl>
                        </div>
                    </div>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-12">
                    <div className="card">
                        <div className="card-body">
                            <h3>Throughput</h3>

                            <TapThroughputHistogram data={throughputHistogram} />
                        </div>
                    </div>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-12">
                    <div className="card">
                        <div className="card-body">
                            <h3>Capture Configuration</h3>

                            <CaptureConfiguration tap={tap} />
                        </div>
                    </div>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-12">
                    <div className="card">
                        <div className="card-body">
                            <h3>Buses &amp; Channels</h3>

                            <Buses tap={tap} />
                        </div>
                    </div>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3>Metrics</h3>

                            <TapMetrics metrics={tapMetrics} />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )

}

export default TapDetailsPage;
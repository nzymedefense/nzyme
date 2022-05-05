import React, {useEffect, useState} from "react";
import TapsService from "../../services/TapsService";
import {useParams} from "react-router-dom";
import LoadingSpinner from "../misc/LoadingSpinner";
import moment from "moment";
import Routes from "../../util/ApiRoutes";
import numeral from "numeral";
import byteAverageToMbit from "../../util/Tools";

import Plot from 'react-plotly.js'

const tapsService = new TapsService();

function fetchData(tapName, setTap) {
    tapsService.findTap(tapName, setTap);
}

function TapDetailsPage() {

    const { tapName } = useParams();

    const [tap, setTap] = useState(null);

    useEffect(() => {
        fetchData(tapName, setTap);
        const id = setInterval(() =>  fetchData(tapName, setTap), 5000);
        return () => clearInterval(id);
    }, [tapName, setTap]);

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
                <div className="col-md-4">
                    <div className="card">
                        <div className="card-body">
                            <h3>Test</h3>

                            <Plot
                                data={[
                                    {
                                        domain: { x: [0, 1], y: [0, 1] },
                                        value: 270,
                                        title: { text: "Speed" },
                                        type: "indicator",
                                        mode: "gauge+number",
                                        gauge: {
                                            threshold: {
                                                line: {color: "red", width: 4},
                                                thickness: 0.75,
                                                value: 290
                                            }
                                        }
                                    }
                                ]}

                                useResizeHandler={true}
                                style={{width: "100%", height: "100%"}}
                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )

}

export default TapDetailsPage;
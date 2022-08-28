import React, {useEffect, useState} from "react";
import DNSStatisticsChart from "./DNSStatisticsChart";
import EthernetService from "../../../services/EthernetService";

function byteConversion(x) {
    return x/1024;
}

const ethernetService = new EthernetService();

function fetchData(hours, setStatistics) {
    ethernetService.findDNSStatistics(hours, setStatistics);
}

function DNSOverviewPage() {

    const [statistics, setStatistics] = useState(null);

    useEffect(() => {
        fetchData(24, setStatistics);
        const id = setInterval(() =>  fetchData(24, setStatistics), 5000);
        return () => clearInterval(id);
    }, [setStatistics]);


    return (
        <div>
            <div className="row">
                <div className="col-md-12">
                    <h1>DNS</h1>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3>Queries/Minute</h3>

                            <DNSStatisticsChart statistics={statistics} attribute="request_count" />
                        </div>
                    </div>
                </div>

                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3>NXDOMAIN/Minute</h3>

                            <DNSStatisticsChart statistics={statistics} attribute="nxdomain_count"  />
                        </div>
                    </div>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3>Query Bytes/Minute</h3>

                            <DNSStatisticsChart statistics={statistics}
                                                attribute="request_bytes"
                                                conversion={byteConversion}
                                                valueType="KB" />
                        </div>
                    </div>
                </div>

                <div className="col-md-6">
                    <div className="card">
                        <div className="card-body">
                            <h3>Response Bytes/Minute</h3>

                            <DNSStatisticsChart statistics={statistics}
                                                attribute="response_bytes"
                                                conversion={byteConversion}
                                                valueType="KB" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )

}

export default DNSOverviewPage;
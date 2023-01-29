import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import ClusterService from "../../../../../services/ClusterService";
import LoadingSpinner from "../../../../misc/LoadingSpinner";
import NodeInactiveWarning from "./NodeInactiveWarning";
import Routes from "../../../../../util/ApiRoutes";
import moment from "moment/moment";
import numeral from "numeral";
import ProcessArguments from "./ProcessArguments";
import CpuLoadIndicator from "./CpuLoadIndicator";
import MemoryUseIndicator from "./MemoryUseIndicator";
import TapReportStatisticsChart from "./TapReportStatisticsChart";

const clusterService = new ClusterService()

function fetchData (uuid, setNode) {
  clusterService.findNode(uuid, setNode)
}

function NodeDetailsPage() {

  const { uuid } = useParams()

  const [node, setNode] = useState(null)

  useEffect(() => {
    fetchData(uuid, setNode)
    const id = setInterval(() => fetchData(uuid, setNode), 5000)
    return () => clearInterval(id)
  }, [uuid, setNode])

  if (!node) {
    return <LoadingSpinner />
  }

  return (
      <div>
        <div className="row">
          <div className="col-md-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={Routes.SYSTEM.CLUSTER.INDEX}>Nodes</a></li>
                <li className="breadcrumb-item active" aria-current="page">{node.name}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-2">
            <a className="btn btn-primary float-end" href={Routes.SYSTEM.CLUSTER.INDEX}>Back</a>
          </div>

          <div className="col-md-12">
            <h1>Node &quot;{node.name}&quot;</h1>
          </div>
        </div>

        <NodeInactiveWarning node={node} />

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Node Information</h3>
                <dl>
                  <dt>Last Seen</dt>
                  <dd>
                    <span title={moment(node.last_seen).format()}>
                      {moment(node.last_seen).fromNow()}{' '}
                      <span className="text-muted">
                        ({moment(node.last_seen).format()})
                      </span>
                    </span>
                  </dd>
                  <dt>Start Time</dt>
                  <dd>
                    <span title={moment(node.process_start_time).format()}>
                      {moment(node.process_start_time).fromNow()}{' '}
                      <span className="text-muted">
                        ({moment(node.process_start_time).format()})
                      </span>
                    </span>
                  </dd>
                </dl>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Host Information</h3>
                <dl>
                  <dt>Operating System</dt>
                  <dd>{node.os_information}</dd>
                  <dt>Maximum CPU Threads</dt>
                  <dd>{node.cpu_thread_count}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <h3>System CPU Load</h3>
                <CpuLoadIndicator load={node.cpu_system_load} />

                <div className="node-metrics-gauge-summary">
                  {numeral(node.cpu_system_load).format("0")}% system CPU load
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <h3>System Memory Use</h3>
                <MemoryUseIndicator total={node.memory_bytes_total} used={node.memory_bytes_used} />

                <div className="node-metrics-gauge-summary">
                  {numeral(node.memory_bytes_used).format("0.0b")} of {' '}
                  {numeral(node.memory_bytes_total).format("0.0b")} system memory used
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card">
              <div className="card-body">
                <h3>JVM Heap Memory Use</h3>
                <MemoryUseIndicator total={node.heap_bytes_total} used={node.heap_bytes_used} />

                <div className="node-metrics-gauge-summary">
                  {numeral(node.heap_bytes_used).format("0.0b")} of {' '}
                  {numeral(node.heap_bytes_total).format("0.0b")} JVM heap memory used
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Total Data Received from Taps</h3>

                <TapReportStatisticsChart nodeId={node.uuid} />
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Process Arguments</h3>

                <ProcessArguments arguments={node.process_arguments} />
              </div>
            </div>
          </div>
        </div>

      </div>
  )

}

export default NodeDetailsPage;

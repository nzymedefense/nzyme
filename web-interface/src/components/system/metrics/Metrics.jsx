import React from 'react'

import TimerRow from './TimerRow'
import LoadingSpinner from '../ProbesTable'

import numeral from 'numeral'
import SystemService from '../../../services/SystemService'

class Metrics extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      systemMetrics: undefined
    }

    this.systemService = new SystemService()
    this.systemService.getMetrics = this.systemService.getMetrics.bind(this)
  }

  componentDidMount () {
    const self = this

    this.systemService.getMetrics()
    setInterval(function () {
      self.systemService.getMetrics()
    }, 5000)
  }

  render () {
    if (!this.state.systemMetrics) {
      return <LoadingSpinner/>
    } else {
      return (
                <div>
                    <div className="row">
                        <div className="col-md-12">
                            <h3>Leader Metrics</h3>

                            <div className="row">
                                <div className="col-md-3">
                                    <dl>
                                        <dt>Heap Memory Usage:</dt>
                                        <dd>
                                            {numeral(this.state.systemMetrics.mem_heap_used.value).format('0.0b')}
                                            &nbsp;of&nbsp;
                                            {numeral(this.state.systemMetrics.mem_heap_max.value).format('0.0b')}
                                            &nbsp;({numeral(this.state.systemMetrics.mem_heap_usage_percent.value).format('0.0%')})
                                        </dd>
                                    </dl>
                                </div>

                                <div className="col-md-3">
                                    <dl>
                                        <dt>Non-Heap Memory Usage:</dt>
                                        <dd>
                                            {numeral(this.state.systemMetrics.mem_nonheap_used.value).format('0.0b')}
                                        </dd>
                                    </dl>
                                </div>

                                <div className="col-md-3">
                                    <dl>
                                        <dt>Ground Station Traffic:</dt>
                                        <dd>
                                            {numeral(this.state.systemMetrics.groundstation_rx.count).format('0.0b')} RX
                                            &nbsp;/&nbsp;
                                            {numeral(this.state.systemMetrics.groundstation_tx.count).format('0.0b')} TX
                                        </dd>
                                    </dl>
                                </div>

                                <div className="col-md-3">
                                    <dl>
                                        <dt>Ground Station Transmit Queue:</dt>
                                        <dd>
                                            {numeral(this.state.systemMetrics.groundstation_queue_size.value).format('0,0')} entries
                                        </dd>
                                    </dl>
                                </div>
                            </div>

                            <div className="row">
                                <div className="col-md-3">
                                    <dl>
                                        <dt>Total Database Size:</dt>
                                        <dd>{numeral(this.state.systemMetrics.database_size.value).format('0.0b')}</dd>
                                    </dl>
                                </div>
                            </div>

                            <div className="row">
                                <div className="col-md-12">
                                    <table className="table table-sm table-hover table-striped">
                                        <thead>
                                        <tr>
                                            <th>Metric</th>
                                            <th>Maximum</th>
                                            <th>Minimum</th>
                                            <th>Mean</th>
                                            <th>99th Percentile</th>
                                            <th>Standard Deviation</th>
                                            <th>Calls</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                            <TimerRow title="802.11 Frame Processing" timer={this.state.systemMetrics.frame_timing}/>
                                            <TimerRow title="802.11 Tagged Parameter Parsing" timer={this.state.systemMetrics.tagged_params_parse_timing}/>
                                            <TimerRow title="802.11 Fingerprinting" timer={this.state.systemMetrics.tagged_params_fingerprint_timing}/>
                                            <TimerRow title="Contact Identifier" timer={this.state.systemMetrics.contact_identifier_timing}/>
                                            <TimerRow title="Beacon Rate Monitor Executions" timer={this.state.systemMetrics.beaconrate_monitor_timing}/>
                                            <TimerRow title="OUI Lookup" timer={this.state.systemMetrics.oui_lookup_timing}/>
                                            <TimerRow title="Signal Tables Mutex Acquisition" timer={this.state.systemMetrics.signaltables_mutex_wait}/>
                                            <TimerRow title="Signal Track Monitor Executions" timer={this.state.systemMetrics.signaltrack_monitor_timing}/>
                                            <TimerRow title="Ground Station Frame Encryption" timer={this.state.systemMetrics.groundstation_encryption_timing}/>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
      )
    }
  }
}

export default Metrics

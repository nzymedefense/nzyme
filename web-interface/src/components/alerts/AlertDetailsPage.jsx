import React, { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'

import moment from 'moment'

import FrameCount from './FrameCount'
import AlertField from './AlertField'
import ApiRoutes from '../../util/ApiRoutes'
import AlertsService from '../../services/AlertsService'
import LoadingSpinner from '../misc/LoadingSpinner'

const alertsService = new AlertsService()

function fetchData (alertId, stateHook) {
  alertsService.findOne(alertId, stateHook)
}

function AlertDetailsPage () {
  const [alert, setAlert] = useState()
  const { alertId } = useParams()

  useEffect(() => {
    fetchData(alertId, setAlert)
    const id = setInterval(() => fetchData(alertId, setAlert), 5000)
    return () => clearInterval(id)
  }, [alertId])

  if (!alert) {
    return <LoadingSpinner />
  } else {
    return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <nav aria-label="breadcrumb">
                            <ol className="breadcrumb">
                                <li className="breadcrumb-item">
                                    <a href={ApiRoutes.ALERTS.INDEX}>Alerts</a>
                                </li>
                                <li className="breadcrumb-item" aria-current="page">
                                    {alert.id}
                                </li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h2>
                            Alert <em>{alert.id}</em>

                            <small>
                                &nbsp;
                                {alert.is_active
                                  ? <span className="badge badge-danger pull-right">active/ongoing</span>
                                  : <span className="badge badge-info">inactive</span>}
                            </small>
                        </h2>

                        <blockquote className={'text-danger'} style={{ 'font-weight': 'bold' }}>{alert.message}</blockquote>

                        <hr />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6">
                        <dl>
                            <h3>Time</h3>

                            <dt>First seen:</dt>
                            <dd>{moment(alert.first_seen).format()}  ({moment(alert.first_seen).fromNow()})</dd>
                            <dt>Last seen:</dt>
                            <dd>{moment(alert.last_seen).format()} ({moment(alert.last_seen).fromNow()})</dd>
                        </dl>

                        <hr />

                        <p>
                            <h3>Meta Information</h3>

                            <dl>
                                {Object.keys(alert.fields).map(function (key) {
                                  return <AlertField key={key} fieldKey={key} value={alert.fields[key]} fields={alert.fields} />
                                })}
                            </dl>
                        </p>

                        <hr />

                        <p>
                            <dl>
                                <dt>Frames</dt>
                                <dd><FrameCount alert={alert} /></dd>
                                <dt>Subsystem</dt>
                                <dd>{alert.subsystem}</dd>
                                <dt>Alert Type ID</dt>
                                <dd>{alert.type}</dd>
                            </dl>
                        </p>
                    </div>

                    <div className="col-md-6">
                        <div className="alert alert-info">
                            <h3>Guidance</h3>
                            <p>
                                {alert.description}
                            </p>

                            <h4>Possible False Positives</h4>
                            <ul>
                                {Object.keys(alert.false_positives).map(function (key) {
                                  return (<li key={key}>{alert.false_positives[key]}</li>)
                                })}
                            </ul>

                            <p>
                                <a href={'https://go.nzyme.org/' + alert.documentation_link} className="btn btn-primary" target="_blank" rel="noreferrer">
                                    Learn More
                                </a>
                            </p>
                        </div>
                    </div>
                </div>

            </div>
    )
  }
}

export default AlertDetailsPage

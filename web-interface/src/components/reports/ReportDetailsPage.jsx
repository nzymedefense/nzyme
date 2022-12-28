import React, { useState, useEffect } from 'react'
import { useParams, Navigate } from 'react-router-dom'

import Routes from '../../util/ApiRoutes'
import LoadingSpinner from '../misc/LoadingSpinner'
import ReportName from './ReportName'
import ReportsService from '../../services/ReportsService'
import moment from 'moment'
import ReportFireTime from './ReportFireTime'
import EmailReceiversDetailsTable from './EmailReceiversDetailsTable'
import ReportExecutionLog from './ReportExecutionLog'
import DeleteReportButton from './DeleteReportButton'
import AddEmailReceiverForm from './AddEmailReceiverForm'

const reportsService = new ReportsService()

function ReportDetailsPage () {
  const { reportName } = useParams()

  const [report, setReport] = useState(null)
  const [reportDeleted, setReportDeleted] = useState(false)

  useEffect(() => {
    reportsService.findOne(reportName, setReport)
  }, [reportName])

  if (reportDeleted) {
    return <Navigate to={Routes.SYSTEM.REPORTS.INDEX} />
  }

  if (!report) {
    return <LoadingSpinner />
  }

  return (
          <div>
              <div className="row">
                  <div className="col-md-12">
                      <nav aria-label="breadcrumb">
                          <ol className="breadcrumb">
                              <li className="breadcrumb-item"><a href={Routes.SYSTEM.REPORTS.INDEX}>Reports</a></li>
                              <li className="breadcrumb-item active" aria-current="page">
                                  <ReportName name={report.name} />
                              </li>
                          </ol>
                      </nav>
                  </div>
              </div>

              <div className="row">
                  <div className="col-md-12">
                      <h1>Report Details</h1>
                  </div>
              </div>

              <div className="row">
                  <div className="col-md-3">
                      <dl>
                          <dt>Name:</dt>
                          <dd><ReportName name={report.name} /></dd>
                      </dl>
                  </div>
                  <div className="col-md-3">
                      <dl>
                          <dt>Created at:</dt>
                          <dd>{moment(report.created_at).format()}</dd>
                      </dl>
                  </div>

                  <div className="col-md-6">
                      <span className="float-right">
                          <a href={Routes.SYSTEM.REPORTS.INDEX} className="btn btn-dark">Back</a>&nbsp;
                          <DeleteReportButton
                            reportsService={reportsService}
                            setReportDeleted={setReportDeleted}
                            reportName={report.name} />
                      </span>
                  </div>
              </div>

              <div className="row">
                  <div className="col-md-3">
                      <dl>
                          <dt>Next Fire Time:</dt>
                          <dd><ReportFireTime time={report.next_fire_time} /></dd>
                      </dl>
                  </div>

                  <div className="col-md-3">
                      <dl>
                          <dt>Previous Fire Time:</dt>
                          <dd><ReportFireTime time={report.previous_fire_time} /></dd>
                      </dl>
                  </div>

                  <div className="col-md-3">
                      <dl>
                          <dt>Schedule:</dt>
                          <dd title={report.cron_expression}>{report.schedule_string}</dd>
                      </dl>
                  </div>

                  <div className="col-md-3">
                      <dl>
                          <dt>Trigger State:</dt>
                          <dd>{report.trigger_state}</dd>
                      </dl>
                  </div>
              </div>

              <hr />

              <div className="row">
                  <div className="col-md-12">
                      <h2>Receivers</h2>
                  </div>
              </div>

              <div className="row">
                  <div className="col-md-6">
                      <AddEmailReceiverForm report={report} reportsService={reportsService} setReport={setReport} />
                  </div>
              </div>

              <div className="row">
                  <div className="col-md-12">
                      <EmailReceiversDetailsTable report={report} reportsService={reportsService} setReport={setReport} />
                  </div>
              </div>

              <div className="row">
                  <div className="col-md-12">
                      <h2>Execution Log <small>(previous 14 executions)</small></h2>
                  </div>

                  <div className="col-md-12">
                      <ReportExecutionLog reportName={reportName} logs={report.recent_execution_log} />
                  </div>
              </div>

          </div>
  )
}

export default ReportDetailsPage

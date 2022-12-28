import RESTClient from '../util/RESTClient'

class ReportsService {
  findAll () {
    const self = this

    RESTClient.get('/reports', {}, function (response) {
      self.setState({ reports: response.data.reports })
    })
  }

  findOne (name, setReport) {
    RESTClient.get('/reports/show/' + name, {}, function (response) {
      setReport(response.data)
    })
  }

  schedule (type, hourOfDay, minuteOfHour, emailReceivers, successCallback, errorCallback) {
    const data = {
      report_type: type,
      hour_of_day: hourOfDay,
      minute_of_hour: minuteOfHour,
      email_receivers: emailReceivers
    }

    RESTClient.post('/reports/schedule', data, function () {
      successCallback()
    }, function () {
      errorCallback()
    })
  }

  deleteReport (name, setReportDeleted, errorCallback) {
    const self = this

    RESTClient.delete('/reports/show/' + name, {}, function () {
      setReportDeleted(true)
    }, function () {
      errorCallback()
    })
  }

  deleteEmailReceiver (reportName, email, successCallback, errorCallback) {
    const data = {
      email_address: email
    }

    RESTClient.post('/reports/show/' + reportName + '/receivers/email/delete', data, function () {
      successCallback()
    }, function () {
      errorCallback()
    })
  }

  addEmailReceiver (reportName, email, successCallback, errorCallback) {
    const data = {
      email_address: email
    }

    RESTClient.post('/reports/show/' + reportName + '/receivers/email', data, function () {
      successCallback()
    }, function () {
      errorCallback()
    })
  }

  findExecutionLog (reportName, executionId, setLog) {
    const self = this

    RESTClient.get('/reports/show/' + reportName + '/execution/' + executionId, {}, function (response) {
      setLog(response.data)
    })
  }
}

export default ReportsService

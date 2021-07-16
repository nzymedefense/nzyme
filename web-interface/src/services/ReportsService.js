import RESTClient from "../util/RESTClient";

class ReportsService {

  schedule(type, hourOfDay, minuteOfHour, emailReceivers, successCallback, errorCallback) {
    const data = {
      report_type: type,
      hour_of_day: hourOfDay,
      minute_of_hour: minuteOfHour,
      email_receivers: emailReceivers
    }

    RESTClient.post("/reports/schedule", data, function() {
      successCallback();
    }, function() {
      errorCallback();
    });
  }

}

export default ReportsService;
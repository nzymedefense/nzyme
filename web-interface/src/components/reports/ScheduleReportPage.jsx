import React from 'react';
import EmailReceivers from "./EmailReceivers";
import Routes from "../../util/Routes";
import ReportsService from "../../services/ReportsService";
import {notify} from "react-notify-toast";
import Redirect from "react-router-dom/Redirect";

class ScheduleReportPage extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      selectedReportType: undefined,
      emailReceivers: [],
      runAt: "20:00",
      addEmailFilled: false,
      submitting: false,
      submitted: false
    };

    this.reportsService = new ReportsService();

    this.selector = React.createRef();
    this.formDetails = React.createRef();
    this.addEmail = React.createRef();
    this.runAt = React.createRef();

    this._selectType = this._selectType.bind(this);
    this._reportDescription = this._reportDescription.bind(this);
    this._addEmailReceiver = this._addEmailReceiver.bind(this);
    this._updateTime = this._updateTime.bind(this);
    this._updateAddEmail = this._updateAddEmail.bind(this);
    this._onEmailReceiverDelete = this._onEmailReceiverDelete.bind(this);
    this._submitForm = this._submitForm.bind(this);
  }

  _selectType() {
    this.setState({selectedReportType: this.selector.current.value});
  }

  _reportDescription() {
    switch (this.state.selectedReportType) {
      case "TacticalSummary":
        return (
            <React.Fragment>
              <h5>Tactical Summary Report</h5>
              <p>
                The Tactical Summary Report provides a summary of all monitored networks, all detected networks of the day,
                all new networks of the day, all alerts of the day as well as the health of nzyme itself.
              </p>
            </React.Fragment>
        )
      default:
        return (
            <React.Fragment>
              <i className="fas fa-exclamation-triangle" /> Please select a report type.
            </React.Fragment>
        )
    }
  }

  _addEmailReceiver() {
    const receiver = this.addEmail.current.value;

    if (receiver && receiver.trim() !== "") {
      this.addEmail.current.value = "";
      this.setState(prevState => ({
        emailReceivers: [...prevState.emailReceivers, receiver],
        addEmailFilled: false // we have to set this because the reset about does not trigger onChange and button is never disabled
      }))
    }
  }

  _updateAddEmail() {
    this.setState({
      addEmailFilled: this.addEmail.current && this.addEmail.current.value.trim() !== ""
    })
  }

  _updateTime() {
    this.setState({runAt: this.runAt.current.value});
  }

  _onEmailReceiverDelete(e, deletedReceiver) {
    e.preventDefault();

    this.setState({emailReceivers: this.state.emailReceivers.filter(function(receiver) {
        return receiver !== deletedReceiver
      })});
  }

  _submitForm(e) {
    e.preventDefault();

    const self = this;
    const dateParts = this.state.runAt.split(":");

    this.reportsService.schedule(
        this.state.selectedReportType,
        parseInt(dateParts[0], 10),
        parseInt(dateParts[1], 10),
        this.state.emailReceivers,
        function () {
          self.setState({submitting: false, submitted: true});
          notify.show("Report Scheduled", "success");
        },
        function () {
          self.setState({submitting: false});
          notify.show("Could not schedule report. Please check nzyme log file.", "error");
        }
    )
  }

  render() {
    if (this.state.submitted) {
      return ( <Redirect to={Routes.REPORTS.INDEX} /> );
    }

    return (
        <div>
          <div className="row">
            <div className="col-md-12">
              <h1>Schedule Report</h1>
            </div>
          </div>

          <div className="row">
            <div className="col-md-12">
              <form onSubmit={this._submitForm}>
                <div className="form-group">
                  <label htmlFor="reportType">Report Type</label>
                  <select id="reportType" name="reportType" ref={this.selector} onChange={this._selectType}
                          className="form-control" required defaultValue="default-empty">
                    <option key="default-empty" />
                    <option key="TacticalSummary" value="TacticalSummary">Tactical Summary</option>
                  </select>
                </div>

                <div className="alert alert-info mt-lg-3">
                  {this._reportDescription()}
                </div>

                <div style={{display: this.state.selectedReportType ? "block" : "none"}}>
                  <div className="form-group" ref={this.formDetails}>
                    <label htmlFor="runAt">Run At</label>
                    <input id="runAt" name="runAt" ref={this.runAt} type="time" className="form-control" required value={this.state.runAt} onChange={this._updateTime} />
                    <small>The report will be generated and sent at that time every day. 12h or 24h format based on your browser locale.</small>
                  </div>

                  <div className="form-group" ref={this.formDetails}>
                    <label htmlFor="addEmail">Add Email Receiver</label>

                    <div className="input-group">
                      <input id="addEmail"
                             type="text"
                             className="form-control"
                             placeholder="john@example.org"
                             ref={this.addEmail}
                             onChange={this._updateAddEmail}
                             onKeyPress={(e) => { e.key === 'Enter' && e.preventDefault(); }} />
                      <div className="input-group-append">
                        <button className="btn btn-secondary" type="button" onClick={this._addEmailReceiver} disabled={!this.state.addEmailFilled}>
                          Add Email Receiver
                        </button>
                      </div>
                    </div>
                  </div>

                  <h5>Email Receivers <small>Optional</small></h5>
                  <EmailReceivers receivers={this.state.emailReceivers} onReceiverDelete={this._onEmailReceiverDelete} />

                  <button type="submit" disabled={this.state.submitting}className="btn btn-success">Schedule Report</button>&nbsp;
                  <a href={Routes.REPORTS.INDEX} className="btn btn-dark">Back</a>
                </div>

              </form>
            </div>
          </div>
        </div>
    )
  }

}

export default ScheduleReportPage;
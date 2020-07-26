import React from 'react';
import Reflux from 'reflux';
import AlertsTable from "./AlertsTable";
import AlertsStore from "../../stores/AlertsStore";
import AlertsActions from "../../actions/AlertsActions";
import LoadingSpinner from "../misc/LoadingSpinner";

class AlertsPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = AlertsStore;

        this.state = {
            alerts: undefined,
            total_alerts: 0,
            page: 0
        }

        this._loadData = this._loadData.bind(this);
        this._page = this._page.bind(this);
    }

    componentDidMount() {
        this._loadData();

        setInterval(this._loadData, 5000);
    }

    _loadData() {
        AlertsActions.findAll(this.state.page);
    }

    _page(direction) {
        const currentPage = this.state.page;
        if (direction === "next") {
            this.setState({alerts: undefined, page: currentPage+1}, () => {this._loadData()});
        } else if (direction === "previous") {
            this.setState({alerts: undefined, page: currentPage-1}, () => {this._loadData()});
        }
    }

    render() {
        const totalPages = this.state.total_alerts === 0 ? 1 : Math.ceil(this.state.total_alerts/25);

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Alerts</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <AlertsTable alerts={this.state.alerts} />
                    </div>
                </div>

                {this.state.alerts &&
                    <div className="row">
                        <div className="col-md-6">
                            <nav>
                                <ul className="pagination">
                                    <li className={"page-item " + (this.state.page === 0 ? "disabled" : "")}>
                                        <button className="page-link" disabled={this.state.page === 0} onClick={() => {this._page("previous")}}>Previous page</button>
                                    </li>

                                    <li className={"page-item " + (this.state.page+1 === totalPages ? "disabled" : "")}>
                                        <button className="page-link" disabled={this.state.page+1 === totalPages} onClick={() => {this._page("next")}}>Next page</button>
                                    </li>
                                </ul>
                            </nav>
                        </div>
                        <div className="col-md-6 text-right">
                            Page {this.state.page+1}/{totalPages}
                        </div>
                    </div>
                }
            </div>
        )
    }

}

export default AlertsPage;
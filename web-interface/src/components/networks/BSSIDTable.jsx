import React from 'react';
import Reflux from 'reflux';
import BSSIDTableRow from "./BSSIDTableRow";

class BSSIDTable extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            bssids: props.bssids
        }
    }

    componentWillReceiveProps(newProps) {
        this.setState({bssids: newProps.bssids});
    }

    render() {
        const self = this;
        return (
            <div className="row">
                <div className="col-md-12">
                    <table className="table table-sm table-hover table-striped">
                        <thead>
                        <tr>
                            <th>BSSID</th>
                            <th><i className="fas fa-signal" /></th>
                            <th>Advertised Networks</th>
                            <th>OUI</th>
                            <th>FP</th>
                            <th>WPS</th>
                            <th>Last Seen</th>
                        </tr>
                        </thead>
                        <tbody>
                        {Object.keys(this.state.bssids).map(function (key,i) {
                            return <BSSIDTableRow key={self.state.bssids[key].bssid} bssid={self.state.bssids[key]} />;
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        )
    }

}

export default BSSIDTable;
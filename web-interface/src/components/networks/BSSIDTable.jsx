import React from 'react';
import BSSIDTableRow from "./BSSIDTableRow";
import Routes from "../../util/Routes";

class BSSIDTable extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            bssids: props.bssids,
            selectedBSSID: undefined
        };

        this._onBSSIDClick = this._onBSSIDClick.bind(this);
    }

    componentWillReceiveProps(newProps) {
        this.setState({bssids: newProps.bssids});
    }

    _onBSSIDClick(e, bssid) {
        e.preventDefault();

        if (this.state.selectedBSSID === bssid) {
            // Click on a selected BSSID closes it.
            this.setState({selectedBSSID: undefined});
        } else {
            this.setState({selectedBSSID: bssid})
        }
    }

    render() {
        if (this.state.bssids.length === 0) {
            return (
                <div className="alert alert-warning">
                    <p>
                        No networks discovered yet. Check the <a href={Routes.SYSTEM_STATUS}>system page</a> page to make
                        sure that all probes are configured correctly and up.
                    </p>

                    <p>
                        You should also take a look at the <a href="https://go.nzyme.org/no-networks" target="_blank" rel="noopener noreferrer">
                        documentation page that explains common reasons for no discovered networks</a>.
                    </p>
                </div>
            )
        }

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
                            <th title="Security">SEC</th>
                            <th title="Fingerprints">FP</th>
                            <th>WPS</th>
                        </tr>
                        </thead>
                        <tbody>
                        {Object.keys(this.state.bssids).map(function (key,i) {
                            return <BSSIDTableRow
                                key={self.state.bssids[key].bssid}
                                bssid={self.state.bssids[key]}
                                displayDetails={self.state.selectedBSSID === self.state.bssids[key].bssid}
                                onBSSIDClick={self._onBSSIDClick} />;
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        )
    }

}

export default BSSIDTable;
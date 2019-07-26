import React from 'react';
import Reflux from 'reflux';

import SSIDTable from "./SSIDTable";
import BSSIDTableRowTop from "./BSSIDTableRowTop";

class BSSIDTableRow extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            displayDetails: false
        };

        this._bssidClick = this._bssidClick.bind(this);
    }

    _bssidClick(e) {
        e.preventDefault();

        const oldState = this.state.displayDetails;
        this.setState({displayDetails: !oldState})
    }

    render() {
        const self = this;

        if (!this.state.displayDetails) {
            return (
                <React.Fragment>
                    <BSSIDTableRowTop bssid={this.props.bssid} clickHandler={this._bssidClick} />
                </React.Fragment>
            )
        } else {
            return (
                <React.Fragment>
                    <BSSIDTableRowTop bssid={this.props.bssid} clickHandler={this._bssidClick} />

                    {Object.keys(this.props.bssid.ssids).map(function (key,i) {
                        return <SSIDTable key={i}  bssid={self.props.bssid.bssid} ssid={self.props.bssid.ssids[key]} />;
                    })}
                </React.Fragment>
            )
        }
    }

}

export default BSSIDTableRow;
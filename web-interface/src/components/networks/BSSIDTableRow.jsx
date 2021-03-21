import React from 'react';

import SSIDTable from "./SSIDTable";
import BSSIDTableRowTop from "./BSSIDTableRowTop";

class BSSIDTableRow extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            displayDetails: this.props.displayDetails
        };
    }

    componentWillReceiveProps(newProps) {
        this.setState({displayDetails: newProps.displayDetails});
    }

    render() {
        const bssid = this.props.bssid;

        if (!this.state.displayDetails) {
            return (
                <React.Fragment>
                    <BSSIDTableRowTop bssid={bssid} clickHandler={(e) => this.props.onBSSIDClick(e, bssid.bssid)} />
                </React.Fragment>
            )
        } else {
            return (
                <React.Fragment>
                    <BSSIDTableRowTop bssid={bssid} clickHandler={(e) => this.props.onBSSIDClick(e, bssid.bssid)} />

                    {Object.keys(bssid.ssids).map(function (key,i) {
                        return <SSIDTable
                            key={"ssidtable-" + bssid.bssid + "-" + bssid.ssids[key] + "-" + key}
                            bssid={bssid.bssid}
                            ssid={bssid.ssids[key]}
                        />;
                    })}
                </React.Fragment>
            )
        }
    }

}

export default BSSIDTableRow;
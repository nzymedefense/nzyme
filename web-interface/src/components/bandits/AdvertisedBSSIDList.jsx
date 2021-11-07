import React from 'react';

class AdvertisedBSSIDList extends React.Component {

    render() {
        const bssids = this.props.bssids;

        if (!bssids || bssids.length === 0) {
            return <div className="alert alert-info">No BSSIDS recorded.</div>;
        }

        return (
            <ul>
                {Object.keys(bssids).map(function (key,i) {
                    return <li key={"bssid-"+i}>{bssids[key]}</li>
                })}
            </ul>
        )
    }

}

export default AdvertisedBSSIDList;
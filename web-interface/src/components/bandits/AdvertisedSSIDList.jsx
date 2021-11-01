import React from 'react';

class AdvertisedSSIDList extends React.Component {

    render() {
        const ssids = this.props.ssids;

        if (!ssids || ssids.length === 0) {
            return <div className="alert alert-info">No SSIDS recorded.</div>;
        }

        return (
            <ul>
                {Object.keys(ssids).map(function (key,i) {
                    return <li key={"ssid-"+i}>{ssids[key]}</li>
                })}
            </ul>
        )
    }

}

export default AdvertisedSSIDList;
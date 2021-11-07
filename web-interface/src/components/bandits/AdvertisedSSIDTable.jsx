import React from 'react';

class AdvertisedSSIDTable extends React.Component {

    render() {
        const ssids = this.props.ssids;

        if (!ssids || ssids.length === 0) {
            return <div className="alert alert-info">No SSIDS recorded.</div>;
        }

        return (
            <table className="table table-sm table-hover table-striped">
                <thead>
                <tr>
                    <th>SSID</th>
                    <th>Frames</th>
                </tr>
                </thead>
                <tbody>
                {Object.keys(ssids).map(function (key,i) {
                    return (
                        <tr key={"bssid-"+i}>
                            <td>{ssids[key].value}</td>
                            <td>{ssids[key].frame_count}</td>
                        </tr>
                    )
                })}
                </tbody>
            </table>
        )
    }

}

export default AdvertisedSSIDTable;
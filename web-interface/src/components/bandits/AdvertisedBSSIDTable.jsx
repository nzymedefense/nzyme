import React from 'react';

class AdvertisedBSSIDTable extends React.Component {

    render() {
        const bssids = this.props.bssids;

        if (!bssids || bssids.length === 0) {
            return <div className="alert alert-info">No BSSIDS recorded.</div>;
        }

        return (
            <table className="table table-sm table-hover table-striped">
                <thead>
                    <tr>
                        <th>BSSID</th>
                        <th>Frames</th>
                    </tr>
                </thead>
                <tbody>
                    {Object.keys(bssids).map(function (key,i) {
                        return (
                            <tr key={"bssid-"+i}>
                                <td>{bssids[key].value}</td>
                                <td>{bssids[key].frame_count}</td>
                            </tr>
                        )
                    })}
                </tbody>
            </table>
        )
    }

}

export default AdvertisedBSSIDTable;
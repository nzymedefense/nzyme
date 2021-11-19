import React from 'react';

class AdvertisedBSSIDTable extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            selection: []
        }

        this._updateSelection = this._updateSelection.bind(this);
    }

    _updateSelection(x) {
        const current = this.state.selection;
        var selection;

        if (this.state.selection.includes(x)) {
            selection = current.filter(item => item !== x);
        } else {
            current.push(x);
            selection = current;
        }

        this.setState({selection: selection});
        this.props.onNewSelection(selection);
    }

    render() {
        const self = this;
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
                        <th>Chart</th>
                    </tr>
                </thead>
                <tbody>
                    {Object.keys(bssids).map(function (key,i) {
                        return (
                            <tr key={"bssid-"+i}>
                                <td>{bssids[key].value}</td>
                                <td>{bssids[key].frame_count}</td>
                                <td>
                                    <input type="checkbox" onClick={() => self._updateSelection(bssids[key].value)} />
                                </td>
                            </tr>
                        )
                    })}
                </tbody>
            </table>
        )
    }

}

export default AdvertisedBSSIDTable;
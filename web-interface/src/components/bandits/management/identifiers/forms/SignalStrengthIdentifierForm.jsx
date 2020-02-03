import React from 'react';
import Reflux from 'reflux';

class SignalStrengthIdentifierForm extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            from: 0,
            to: -100,
            errorMessage: "",
        };

        this._handleFromUpdate = this._handleFromUpdate.bind(this);
        this._handleToUpdate = this._handleToUpdate.bind(this);
        this._handleUpdate = this._handleUpdate.bind(this);
    }

    _handleFromUpdate(e) {
        const from = e.target.value;
        this.setState({from: from, errorMessage: ""});

        if (from > 0 || from < -100) {
            this.setState({errorMessage: "Invalid. The value must be between 0 and -100."});
            this.props.configurationUpdate({ready: false});
            return;
        }

        this._handleUpdate(from, this.state.to);
    }

    _handleToUpdate(e) {
        const to = e.target.value;
        this.setState({to: to, errorMessage: ""});

        if (to > 0 || to < -100) {
            this.setState({errorMessage: "Invalid. The to must be between 0 and -100."});
            this.props.configurationUpdate({ready: false});
            return;
        }

        this._handleUpdate(this.state.from, to);
    }

    _handleUpdate(from, to) {
        if (from <= to) {
            this.setState({errorMessage: "Invalid. The 'from' value must be larger than the 'to' value."});
            this.props.configurationUpdate({ready: false});
            return;
        }

        const explanation = "a frame with a signal strength between "+ from +" and " + to + " is recorded";

        this.props.configurationUpdate({
            configuration: {
                type: "SIGNAL_STRENGTH",
                from: from,
                to: to
            },
            explanation: explanation,
            ready: true
        });
    }

    render() {
        return (
            <form>
                <div className="form-group">
                    <label htmlFor="from">From</label>
                    <input type="number" className="form-control" id="from" value={this.state.from} onChange={this._handleFromUpdate} required />

                    <label htmlFor="to">To</label>
                    <input type="number" className="form-control" id="to" value={this.state.to} onChange={this._handleToUpdate} required />

                    <span className="text-danger">{this.state.errorMessage}</span>
                </div>
            </form>
        )
    }

}

export default SignalStrengthIdentifierForm;
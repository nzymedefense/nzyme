import React from 'react';

class TimerangeSwitcher extends React.Component {

    constructor(props) {
        super(props);

        this.menu = React.createRef();

        this.state = {
            ranges: props.ranges
        };

        this._changeRange = this._changeRange.bind(this);
    }

    _changeRange() {
        this.props._changeRange(this.menu.current.value);
    }

    render() {
        return (
            <div>
                {this.props.title}:&nbsp;

                <select className="timerange-switcher" defaultValue={this.props.currentRange} onChange={this._changeRange} ref={this.menu}>
                    {this.state.ranges.map(function (key,i) {
                        return <option key={"timerange-switch-" + key} value={key}>
                            {key} {key === 1 ? "hour" : "hours"}
                        </option>
                    })}
                </select>
            </div>
        );
    }

}

export default TimerangeSwitcher;
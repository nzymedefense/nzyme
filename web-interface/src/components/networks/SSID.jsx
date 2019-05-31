import React from 'react';

class SSID extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <span className={this.props.ssid.human_readable ? "" : "text-muted"}>
                {this.props.ssid.name.trim()}
            </span>
        );
    }

}

export default SSID;
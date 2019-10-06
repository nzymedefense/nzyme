import React from 'react';

class ChannelSwitcher extends React.Component {

    constructor(props) {
        super(props);

        this.menu = React.createRef();

        this.state = {
            channelNumbers: Object.keys(props.channels)
        };

        this._changeChannel = this._changeChannel.bind(this);
    }

    _changeChannel() {
        this.props.changeChannel(this.menu.current.value);
    }

    render() {
        return (
            <div>
                Select Channel:

                <select className="channel-switcher" defaultValue={this.props.currentChannel} onChange={this._changeChannel} ref={this.menu}>
                    {this.state.channelNumbers.map(function (key,i) {
                        return <option key={"channel-switch-" + key} value={key}>
                            {key}
                        </option>
                    })}
                </select>
            </div>
        );
    }

}

export default ChannelSwitcher;
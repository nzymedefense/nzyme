import React from 'react';
import Reflux from 'reflux';

import LoadingSpinner from "../misc/LoadingSpinner";

import SystemActions from "../../actions/SystemActions";
import SystemStore from "../../stores/SystemStore";
import VersionInfo from "./VersionInfo";

class Versioncheck extends Reflux.Component {

    constructor(props) {
        super(props);

        this.store = SystemStore;

        this.state = {
            versionInfo: undefined
        };
    }

    componentDidMount() {
        SystemActions.getVersionInfo();
    }

    render() {
        if (!this.state.versionInfo) {
            return <LoadingSpinner/>;
        } else {
            return <VersionInfo version={this.state.versionInfo} />;
        }
    }
}

export default Versioncheck;
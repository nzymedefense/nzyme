import React from 'react';
import Reflux from 'reflux';
import LoadingSpinner from "../../misc/LoadingSpinner";

class NetworkDashboardPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            network: undefined
        }
    }


    render() {
        if (!this.state.network) {
            return <LoadingSpinner />;
        }

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Network </h1>
                    </div>
                </div>


            </div>
        );
    }

}

export default NetworkDashboardPage;




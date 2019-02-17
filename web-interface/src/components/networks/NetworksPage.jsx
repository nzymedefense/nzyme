import React from 'react';
import Reflux from 'reflux';
import NetworksList from "./NetworksList";

class NetworksPage extends Reflux.Component {

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Networks</h1>
                    </div>
                </div>

                <NetworksList />
            </div>
        );
    }

}

export default NetworksPage;




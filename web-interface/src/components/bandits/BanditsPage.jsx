import React from 'react';
import Reflux from 'reflux';


class SystemPage extends Reflux.Component {

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Bandits</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-9">
                        <p>
                            A bandit is a description of behavior and characteristics of a device that advertises networks.
                            You can use bandit definitions to track and detect rogue actors who use mutating attributes in
                            frames (different MAC addresses, advertised SSIDs, etc.) by focusing on selected attributes that
                            do not change.
                        </p>
                    </div>

                    <div className="col-md-3">
                        <div className="float-right">
                            <a href="" className="btn btn-primary">Help</a>
                            &nbsp;
                            <a href="" className="btn btn-success">Define new Bandit</a>
                        </div>
                    </div>
                </div>


            </div>
        )
    }

}

export default SystemPage;
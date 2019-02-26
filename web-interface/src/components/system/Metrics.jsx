import React from 'react';
import Reflux from 'reflux';

class Metrics extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h3>Metrics</h3>
                    </div>
                </div>
            </div>
        )
    }

}

export default Metrics;
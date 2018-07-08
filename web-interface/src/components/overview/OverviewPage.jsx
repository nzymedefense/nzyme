import React from 'react';
import Reflux from 'reflux';

import GlobalStatistics from "./GlobalStatistics";

class OverviewPage extends Reflux.Component {

  render() {
    return (
      <div>
        <div className="row">
          <div className="col-md-12">
            <h1>System Overview</h1>
          </div>
        </div>


        <GlobalStatistics/>
      </div>
    );
  }

}

export default OverviewPage;




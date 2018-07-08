import React from 'react';
import {API_ROOT} from "../../util/API";

class NotConnectedPage extends React.Component {

  render() {
    return (
      <div>
        <div className="row">
          <div className="col-md-12">
            <h1>Not Connected to nzyme API!</h1>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <div className="alert alert-danger">
              <strong>Could not connect to nzyme REST API at `{API_ROOT}`.</strong>

              <br />
              Please ensure that nzyme is running, the URL points to the nzyme leader node and that your browser
              can connect to it.
            </div>
          </div>
        </div>
      </div>
    )
  }

}

export default NotConnectedPage;




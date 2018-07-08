import React, { Component } from 'react';

class App extends Component {
  render() {
    return (
      <div className="nzyme">
        <header>
          <div className="navbar box-shadow">
            <div className="container d-flex justify-content-between">
              <a href="/" className="navbar-brand d-flex align-items-center">
                <strong>nzyme - WiFi Defense System</strong>
              </a>
            </div>
          </div>
        </header>

        <div className="container">
          <div className="row">
            <div className="col-md-12">
              <h1>System Overview</h1>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default App;

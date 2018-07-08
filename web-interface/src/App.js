import React from 'react';
import Reflux from 'reflux';

import {
  BrowserRouter as Router,
  Switch,
  Route
} from 'react-router-dom';

import Notifications from 'react-notify-toast';

import PingStore from "./stores/PingStore";
import PingActions from "./actions/PingActions";

import NavigationBar from './components/layout/NavigationBar';
import OverviewPage from "./components/overview/OverviewPage";
import NotConnectedPage from "./components/misc/NotConnectedPage";

class App extends Reflux.Component {

  constructor(props) {
    super(props);

    this.state = {
      apiConnected: true
    };

    this.store = PingStore;
  }

  componentDidMount() {
    PingActions.ping();
  }

  render() {
    if(this.state.apiConnected) {
      return (
        <div className="nzyme">
          <NavigationBar/>

          <div className="container">
            <Notifications/>

            <OverviewPage/>
          </div>
        </div>
      );
    } else {
      return (
        <div className="nzyme">
          <NavigationBar/>

          <div className="container">
            <Notifications />
            <NotConnectedPage />
          </div>
        </div>
      )
    }
  }
}

export default App;

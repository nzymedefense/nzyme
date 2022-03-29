import React from 'react'
import Routes from '../../util/ApiRoutes'
import NavigationLink from './NavigationLink'
import AlertsButton from './AlertsButton'

class NavigationBar extends React.Component {
  render () {
    return (
          <nav className="navbar navbar-expand navbar-dark bg-dark">
            <div className="container-fluid">
              <a className="navbar-brand" href="#">nzyme</a>

              <div className="collapse navbar-collapse" id="navbarSupportedContent">
                <form className="d-flex">
                  <input className="form-control me-2" type="search" placeholder="Search" aria-label="Search" />
                    <button className="btn btn-primary" type="submit">Search</button>
                </form>
              </div>
            </div>
          </nav>
    )
  }
}

export default NavigationBar

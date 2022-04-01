import React from 'react'

class NavigationBar extends React.Component {
  render () {
    return (
        <nav className="navbar">
            <div className="container-fluid">
                <form  className="d-flex flex-row">
                    <div className="input-group">
                        <input className="form-control" type="search" placeholder="Search" aria-label="Search" />
                        <button className="btn btn-outline-primary" type="submit">
                            <i className="fa-solid fa-search" />
                        </button>
                    </div>
                </form>

                <div className="d-flex flex-row">
                    <button className="btn btn-outline-dark" title="Help">
                        <i className="fa-solid fa-question" />
                    </button> &nbsp;
                    <button className="btn btn-outline-primary" title="Sign out">
                        <i className="fa-solid fa-arrow-right-from-bracket" />
                    </button>
                </div>
            </div>
        </nav>
    )
  }
}

export default NavigationBar

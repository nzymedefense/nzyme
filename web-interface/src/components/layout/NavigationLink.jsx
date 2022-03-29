import React from 'react'
import NotificationCount from "./NotificationCount";

class NavigationLink extends React.Component {
  render () {
    let className = 'nav-link'

    if ((window.location.pathname === '/' && this.props.href === '/') || (this.props.href !== '/' && window.location.pathname.startsWith(this.props.href))) {
      className += ' nav-link-active'
    }

    return (
        <React.Fragment>
            <a href={this.props.href} className={className}>
                <span className="nav-icon">
                    <i className={this.props.icon + " fa-icon"} />
                </span>

                {this.props.title}

                <NotificationCount count={this.props.notificationCount} />
            </a>
        </React.Fragment>
    )
  }
}

export default NavigationLink

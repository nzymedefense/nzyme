import React from 'react'
import NotificationCount from './NotificationCount'

class NavigationLink extends React.Component {
  render () {
    let className = 'nav-link'
    let liClassName = ''
    if ((window.location.pathname === '/' && this.props.href === '/') || (this.props.href !== '/' && window.location.pathname.startsWith(this.props.href))) {
      className += ' nav-link-active'
      liClassName = 'nav-item-active'
    }

    return (
        <li className={'nav-item ' + liClassName}>
            <a href={this.props.href} className={className}>
                <span className="nav-icon">
                    {this.props.icon}
                </span>

                {this.props.title}

                <NotificationCount count={this.props.notificationCount} />
            </a>
        </li>
    )
  }
}

export default NavigationLink

import React from 'react'

class NavigationLink extends React.Component {
  render () {
    let className = 'btn btn-dark'

    if ((window.location.pathname === '/' && this.props.href === '/') || (this.props.href !== '/' && window.location.pathname.startsWith(this.props.href))) {
      className += ' active'
    }

    return (
            <a href={this.props.href} className={className}>{this.props.title}</a>
    )
  }
}

export default NavigationLink

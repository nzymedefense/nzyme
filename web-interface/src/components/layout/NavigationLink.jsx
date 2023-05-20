import React from 'react'

function NavigationLink(props) {

  const href = props.href;
  const icon = props.icon;
  const title = props.title;

  const show = props.show === undefined ? true : props.show;

  if (!show) {
    return null;
  }

  let className = 'nav-link'
  let liClassName = '';
  if ((window.location.pathname === '/' && href === '/')
      || (href !== '/' && window.location.pathname.startsWith(href))) {
    className += ' nav-link-active'
    liClassName = 'nav-item-active'
  }
  return (
    <li className={'nav-item ' + liClassName}>
      <a href={href} className={className}>
        <span className="nav-icon">{icon}</span>
        {title}
      </a>
    </li>
  )

}

export default NavigationLink
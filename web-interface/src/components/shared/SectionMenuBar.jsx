import React from 'react';

export default function SectionMenuBar(props) {

  const items = props.items;
  const activeRoute = props.activeRoute;

  return (
      <ul className="nav nav-tabs section-menu-bar">
        {items.map((item, i) => {
          return (
              <li key={i} className="nav-item">
                <a className={"nav-link " + (item.href === activeRoute ? "active" : null)} href={item.href}>
                  {item.name}
                </a>
              </li>
          )
        })}
      </ul>
  )

}
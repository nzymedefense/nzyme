import React from 'react'

function UserProfile(props) {

  const user = props.user;

  return (
    <div className="user-profile">
      <div className="name">
        {user.name}<br />
        <a href="/">
        <i className="fa-solid fa-chevron-right settings-icon"></i>
        Settings
        </a>
      </div>
    </div>
  )
}

export default UserProfile

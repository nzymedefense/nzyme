import React from 'react'
import ApiRoutes from "../../util/ApiRoutes";

function UserProfileBlock(props) {

  const user = props.user;

  return (
    <div className="user-profile">
      <div className="name">
        {user.name}<br />
        <a href={ApiRoutes.USERPROFILE.PROFILE}>
          <i className="fa-solid fa-chevron-right settings-icon"></i>
          Settings
        </a>
      </div>
    </div>
  )
}

export default UserProfileBlock

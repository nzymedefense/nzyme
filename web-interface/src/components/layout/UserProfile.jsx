import React from 'react'
import AssetImage from '../misc/AssetImage'

function UserProfile () {
  return (
        <div className="user-profile">
            <AssetImage filename="lennart_test.jpg"
                        className="avatar float-start"
                        alt="User Avatar" />

            <div className="name">
                Lennart Koopmann<br />
                <a href="/">
                    <i className="fa-solid fa-chevron-right settings-icon"></i>
                    Settings
                </a>
            </div>
        </div>
  )
}

export default UserProfile

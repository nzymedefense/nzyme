import React from 'react'

function UserProfile() {

    return (
        <div className="user-profile">
            <img className="avatar float-start" src="/static/lennart_test.jpg" />
            <div className="name">
                Lennart Koopmann<br />
                <a href="/">
                    <i className="fa-solid fa-chevron-right settings-icon"></i>
                    Settings
                </a>
            </div>
        </div>
    );

}

export default UserProfile;
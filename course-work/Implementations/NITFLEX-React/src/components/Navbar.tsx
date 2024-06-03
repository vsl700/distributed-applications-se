import "./Navbar.css"
import profileSettings from "../assets/profile_settings.svg"
import regNewUser from "../assets/reg_new_user.svg"
import manageUsers from "../assets/manage_users.svg"
import manageMovies from "../assets/manage_movies.svg"
import logout from "../assets/logout.svg"
import cross from '../assets/cross.svg'
import axios from "axios"
import { backendUrl } from "../globals"
import { useEffect, useState } from "react"
import { User, defaultUser } from "../models/User"

export const Navbar = (props: {closeable?: boolean, onClose?: Function}) => {
    const [user, setUser] = useState(defaultUser)
    const [privileges, setPrivileges] = useState([''])

    const hasAnyPrivilege = (priv: string | string[]) => {
        if(typeof priv === "string")
            return privileges.includes(priv)

        return priv.filter(p => privileges.includes(p)).length > 0
    }

    const onProfileSettings = () => {
        window.location.href = '/settings/profilesettings'
    }

    const onManageMovies = () => {
        window.location.href = '/settings/movies'
    }

    const onManageUsers = () => {
        window.location.href = '/settings/users'
    }

    const onRegisterNewUser = () => {
        window.location.href = '/settings/users/new'
    }

    const onLogOut = () => {
        axios.get(backendUrl + '/logout', { withCredentials: true })
            .then(() => window.location.href = '/login')
    }

    useEffect(() => {
        axios.get(backendUrl + '/currentUser', { withCredentials: true })
            .then(response => response.data)
            .then((obj: User) => {
                setUser(obj)
                axios.get(backendUrl + `/users/privileges`, { withCredentials : true })
                    .then(response => response.data)
                    .then((obj1: string[]) => setPrivileges(obj1))
            })
    }, [])

    return (
        <div className='Navbar'>
            <div className="Navbar-head">
                {user !== defaultUser && <NavHeading user={user} />}
                {props.closeable && 
                    <CloseButton onClick={() => {if(props.onClose !== undefined) props.onClose()}} />}
            </div>
            <hr />
            { hasAnyPrivilege('READ_USER_SETTINGS_PRIVILEGE') && 
                <NavOption imageSrc={profileSettings} onClick={onProfileSettings}>Profile settings</NavOption> }
            { hasAnyPrivilege(['REGISTER_USERS_PRIVILEGE', 'REGISTER_ADMINS_PRIVILEGE', 'REGISTER_OWNERS_PRIVILEGE']) && 
                <NavOption imageSrc={regNewUser} onClick={onRegisterNewUser}>Register new user</NavOption> }
            { hasAnyPrivilege(['MANAGE_USERS_PRIVILEGE', 'MANAGE_ADMINS_PRIVILEGE', 'MANAGE_OWNERS_PRIVILEGE']) && 
                <NavOption imageSrc={manageUsers} onClick={onManageUsers}>Manage users</NavOption> }
            { hasAnyPrivilege('MANAGE_MOVIES_PRIVILEGE') && 
                <NavOption imageSrc={manageMovies} onClick={onManageMovies}>Manage movies</NavOption> }
            <hr />
            <NavOption imageSrc={logout} onClick={onLogOut}>Log out</NavOption>
        </div>
    )
}

const NavHeading = (props: {user: User}) => {
    const getUserRole = (user: User): string => {
        switch(user.role){
            case 'ROLE_OWNER': return 'Owner'
            case 'ROLE_ADMIN': return 'Admin'
            case 'ROLE_USER': return 'User'
            default: return 'Invalid role!'
        }
    }

    return (
        <div className="Navbar-heading">
            <p className="Navbar-username">{props.user.username}</p>
            <p className="Navbar-role">{getUserRole(props.user)}</p>
        </div>
    )
}

const NavOption = (props: {imageSrc: string, onClick: Function, children: string}) => {
    return (
        <div className="Navbar-option" onClick={() => props.onClick()}>
            <img className="Navbar-option-image" src={props.imageSrc} alt="Option icon" />
            <p className="Navbar-option-text">{props.children}</p>
        </div>
    )
}

const CloseButton = (props: {onClick: Function}) => {
    return (
        <button className="Navbar-close-button" onClick={() => props.onClick()}><img src={cross} alt="Close navbar" /></button>
    )
}
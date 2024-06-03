import { useEffect, useState } from "react"
import { SettingsPageTemplate } from "../components/SettingsTemplates"
import deleteIcon from '../assets/delete.svg'
import editIcon from '../assets/edit.svg'
import { User, defaultUser } from "../models/User"
import './Settings.css'
import axios from "axios"
import { backendUrl } from "../globals"

export const ManageUsers = () => {
    const [users, setUsers] = useState([defaultUser])

    const getUserRole = (user: User): string => {
        switch(user.role){
            case 'ROLE_OWNER': return 'Owner'
            case 'ROLE_USER': return 'User'
            default: return 'Invalid role!'
        }
    }

    const onUserEdit = (user: User) => {
        window.location.href = `/settings/profilesettings/${user.id}`
    }

    const onUserDelete = (user: User) => {
        axios.delete(backendUrl + `/users/${user.id}`, { withCredentials: true })
            .then(() => fetchUsers())
    }

    const fetchUsers = () => {
        axios.get(backendUrl + '/users', { withCredentials: true })
        .then(response => response.data)
        .then((obj: User[]) => {
            setUsers(obj);
        })
    }

    useEffect(() => {
        fetchUsers()
    }, [])

    return (
        <SettingsPageTemplate title="Manage users">
            <table className="Settings-table">
                <thead>
                    <tr>
                        <th>User</th>
                        <th>Role</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {users.filter(u => u !== defaultUser).map(u => 
                        <tr key={u.username}>
                            <td>{u.username}</td>
                            <td>{getUserRole(u)}</td>
                            <td>
                                <button className="Edit-button nitflex-button" onClick={() => onUserEdit(u)}>
                                    <img src={editIcon} alt="" />
                                </button>
                                <button className="Delete-button nitflex-button" onClick={() => onUserDelete(u)}>
                                    <img src={deleteIcon} alt="" />
                                </button>
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </SettingsPageTemplate>
    )
}
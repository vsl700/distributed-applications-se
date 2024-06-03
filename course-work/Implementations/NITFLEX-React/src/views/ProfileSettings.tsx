import { HorizontalSetting, SettingSection, SettingsPageTemplate } from "../components/SettingsTemplates"
import deleteIcon from '../assets/delete.svg'
import './Settings.css'
import './ProfileSettings.css'
import { useEffect, useState } from "react"
import { UserSettings, defaultUserSettings } from "../models/UserSettings"
import axios from "axios"
import { backendUrl } from "../globals"
import { User, defaultUser } from "../models/User"

export const ProfileSettings = () => {
    const [user, setUser] = useState(defaultUser)
    const [userSettings, setUserSettings] = useState(defaultUserSettings)
    const [userPrivileges, setUserPrivileges] = useState([''])

    const getPathNameSegments = (): string[] => {
        let result = window.location.pathname.substring(1);
        if(result.endsWith('/'))
            result = result.substring(0, result.lastIndexOf('/'));

        return result.split('/');
    }

    const getUserId = (): string => {
        let pathNameSegments = getPathNameSegments()
        if(pathNameSegments[2] === undefined){
            return user.id
        }            

        return pathNameSegments[2]
    }

    const setUserStatus = (status: string) => {
        userSettings.status = status
        setUserSettings(userSettings)
    }

    const setUserRole = (role: string) => {
        userSettings.role = role
        setUserSettings(userSettings)
    }

    const setUserDeviceLimit = (limit: number) => {
        userSettings.deviceLimit = limit
        setUserSettings(userSettings)
    }

    const onSaveSettings = () => {
        axios.put(backendUrl + `/users/settings/${getUserId()}`, userSettings, { withCredentials: true })
            .then(() => window.location.href = '/settings/users')
    }

    const loadUserSettings = (user: User) => {
        axios.get(backendUrl + `/users/settings/${user.id}`, { withCredentials: true })
                .then(response => response.data)
                .then((obj: UserSettings) => setUserSettings(obj))
    }

    const loadUserPrivileges = (user: User) => {
        axios.get(backendUrl + `/users/privileges`, { withCredentials: true })
                .then(response => response.data)
                .then((obj: string[]) => setUserPrivileges(obj))
    }

    const hasPrivilege = (privilege: string) => {
        return userPrivileges.includes(privilege)
    }

    useEffect(() => {
        if(user !== defaultUser) return

        let userId = getUserId()
        if(userId === '')
            axios.get(backendUrl + `/currentUser`, { withCredentials: true })
                .then(response => response.data)
                .then((obj: User) => {
                    setUser(obj)
                    loadUserSettings(obj)
                    loadUserPrivileges(obj)
                })
        else
            axios.get(backendUrl + `/users/${userId}`, { withCredentials: true })
                .then(response => response.data)
                .then((obj: User) => {
                    setUser(obj)
                    loadUserSettings(obj)
                    loadUserPrivileges(obj)
                })
    }, [])

    return (
        <SettingsPageTemplate title="Profile Settings" additionalInfo={user !== defaultUser ? user.username : ''}>
            {/* <SettingSection title="Device sessions">
                <table className="Settings-table">
                    <thead>
                        <tr>
                            <th>Device</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Android 12</td>
                            <td>
                                <button className="Delete-button nitflex-button">
                                    <img src={deleteIcon} alt="" />
                                </button>
                            </td>
                        </tr>
                        <tr>
                            <td>Android 12</td>
                            <td>
                                <button className="Delete-button nitflex-button">
                                    <img src={deleteIcon} alt="" />
                                </button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </SettingSection> */}
            {userSettings !== defaultUserSettings ? <SettingSection title="Settings" separatorLine>
                <HorizontalSetting label="Profile state">
                    <input type="radio" name="state" id="state-active" value="ACTIVE" onChange={e => setUserStatus(e.target.value)} defaultChecked={userSettings.status === 'ACTIVE'} disabled={!hasPrivilege('WRITE_USER_SETTINGS_PRIVILEGE')} />
                    <label htmlFor="state-active">✔Active</label>
                    <input type="radio" name="state" id="state-banned" value="BANNED" onChange={e => setUserStatus(e.target.value)} defaultChecked={userSettings.status === 'BANNED'} disabled={!hasPrivilege('WRITE_USER_SETTINGS_PRIVILEGE')} />
                    <label htmlFor="state-banned">❌Banned</label>
                </HorizontalSetting>
                <HorizontalSetting label="User role">
                    <select name="role" id="role" onChange={e => setUserRole(e.target.value)} defaultValue={userSettings.role} disabled={!hasPrivilege('WRITE_USER_SETTINGS_PRIVILEGE')}>
                        <option value="ROLE_OWNER">Owner</option>
                        <option value="ROLE_USER">User</option>
                    </select>
                </HorizontalSetting>
                <HorizontalSetting label="Device limit">
                    <input type="number" name="devlimit" id="device-limit" onChange={e => setUserDeviceLimit(parseInt(e.target.value))} min={1} defaultValue={userSettings.deviceLimit} disabled={!hasPrivilege('WRITE_USER_SETTINGS_PRIVILEGE')} />
                </HorizontalSetting>
                { hasPrivilege('WRITE_USER_SETTINGS_PRIVILEGE') ? <button className="Settings-save-button nitflex-button" onClick={() => onSaveSettings()}>Save settings</button> : <></> }
            </SettingSection> : <></>}
        </SettingsPageTemplate>
    )
}
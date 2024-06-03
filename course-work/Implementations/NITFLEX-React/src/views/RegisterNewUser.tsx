import { useState } from "react";
import { SettingsPageTemplate } from "../components/SettingsTemplates"
import { backendUrl } from "../globals";
import axios from "axios";

export const RegisterNewUser = () => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [deviceLimit, setDeviceLimit] = useState(3);
    const [role, setRole] = useState("ROLE_USER");
    const [error, setError] = useState(false);

    const success = () => {
        setError(false);
        window.location.href = "/settings/users";
    }

    const failure = (error: any) => {
        console.log(error)
        // if(error.response && error.response.status === 302 && error.response.location === `${backendUrl}/`){
        //     success()
        //     return
        // }

        setError(true)
    }

    const onRegister = () => {
        axios.post(backendUrl + "/register", {
            username: username,
            password: password,
            role: role,
            deviceLimit: deviceLimit
        }, {
            withCredentials: true
        })
        .then(success, failure)
    }

    return (
        <SettingsPageTemplate title="Register new user">
            <div className="Settings-vertical-form">
                { error && <div className="Form-error"> <p>Registration failed!</p> </div> }
                <label htmlFor="username">Username:</label>
                <input type="text" name="username" onChange={(e) => setUsername(e.target.value)} />
                <label htmlFor="password">Password:</label>
                <input type="password" name="password" onChange={e => setPassword(e.target.value)} />
                <label htmlFor="role">User role:</label>
                <select name="role" id="role" defaultValue={role} onChange={e => setRole(e.target.value)}>
                    <option value="ROLE_OWNER">Owner</option>
                    <option value="ROLE_USER">User</option>
                </select>
                <label htmlFor="devlimit">Device limit:</label>
                <input type="number" name="devlimit" id="device-limit" min={1} defaultValue={deviceLimit} onChange={e => setDeviceLimit(parseInt(e.target.value))} />
                <button className="Settings-save-button nitflex-button" onClick={() => onRegister()}>Register</button>
            </div>
        </SettingsPageTemplate>
    )
}
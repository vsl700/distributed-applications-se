import { useState } from "react"
import axios from "axios"
import { NoLayout } from "../components/NoLayout"
import { backendUrl } from "../globals"

export const Login = () => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(false);

    const success = () => {
        setError(false);
        window.location.href = "/";
    }

    const failure = (error: any) => {
        console.log(error)
        // if(error.response && error.response.status === 302 && error.response.location === `${backendUrl}/`){
        //     success()
        //     return
        // }

        setError(true)
    }

    const onLogin = () => {
        axios.post(backendUrl + "/login", {
            username: username,
            password: password
        }, {
            withCredentials: true,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
        .then(success, failure)
    }

    return (
        <NoLayout>
            <div className="Nitflex-form">
                { error && <div className="Form-error"> <p>Login failed!</p> </div> }
                <label htmlFor="username">Username:</label>
                <input type="text" name="username" id="username" onChange={e => setUsername(e.target.value)} />
                <label htmlFor="password">Password:</label>
                <input type="password" name="password" id="password" onChange={e => setPassword(e.target.value)} />
                <button className="Submit-button nitflex-button" onClick={e => onLogin()}>Login</button>
            </div>
        </NoLayout>
    )
}
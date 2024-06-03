import { useEffect, useState } from "react"
import { SettingsPageTemplate } from "../components/SettingsTemplates"
import './Settings.css'
import { MovieSettings, defaultMovieSettings } from "../models/MovieSettings"
import { backendUrl } from "../globals"
import axios from "axios"

export const EditMovie = () => {
    const [movieSettings, setMovieSettings] = useState(defaultMovieSettings)

    const getPathNameSegments = (): string[] => {
        let result = window.location.pathname.substring(1);
        if(result.endsWith('/'))
            result = result.substring(0, result.lastIndexOf('/'));

        return result.split('/');
    }

    const getMovieId = (): string => {
        return getPathNameSegments()[2]
    }

    const setMovieName = (name: string) => {
        movieSettings.name = name
        setMovieSettings(movieSettings)
    }

    const onSaveSettings = () => {
        axios.put(backendUrl + `/movies/settings/${getMovieId()}`, movieSettings, { withCredentials: true })
            .then(() => window.location.href = '/settings/movies')
    }

    useEffect(() => {
        axios.get(backendUrl + `/movies/settings/${getMovieId()}`, { withCredentials: true })
            .then(response => response.data)
            .then((obj: MovieSettings) => setMovieSettings(obj))
    }, [])

    return (
        <SettingsPageTemplate title="Edit movie">
            {movieSettings !== defaultMovieSettings ? 
                <div className="Settings-vertical-form">
                    <label htmlFor="path">Movie path:</label>
                    <input type="text" name="path" defaultValue={movieSettings.path} disabled />
                    <label htmlFor="name">Movie name:</label>
                    <input type="text" name="name" defaultValue={movieSettings.name} onChange={e => setMovieName(e.target.value)} />
                    <button className="Settings-save-button nitflex-button" onClick={() => onSaveSettings()}>Save settings</button>
                </div> : <></>}
        </SettingsPageTemplate>
    )
}
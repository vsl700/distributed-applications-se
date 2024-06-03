import { useEffect, useState } from "react"
import { SettingsPageTemplate } from "../components/SettingsTemplates"
import deleteIcon from '../assets/delete.svg'
import editIcon from '../assets/edit.svg'
import './Settings.css'
import { Movie, defaultMovie } from "../models/Movie"
import axios from "axios"
import { backendUrl } from "../globals"

export const ManageMovies = () => {
    const [movies, setMovies] = useState([defaultMovie]);

    const onMovieEdit = (movie: Movie) => {
        window.location.href = `/settings/movies/${movie.id}`
    }

    const onMovieDelete = (movie: Movie) => {
        axios.delete(backendUrl + `/movies/${movie.id}`, { withCredentials: true })
            .then(() => fetchMovies())
    }

    const fetchMovies = () => {
        axios.get(backendUrl + '/movies', { withCredentials: true })
        .then(response => response.data)
        .then((obj: Movie[]) => {
            setMovies(obj);
        })
    }

    useEffect(() => {
        fetchMovies()
    }, [])

    return (
        <SettingsPageTemplate title="Manage movies">
            <table className="Settings-table">
                <thead>
                    <tr>
                        <th>Movie</th>
                        <th>Added by</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {movies.filter(m => m !== defaultMovie).map(m => 
                        <tr>
                            <td>{m.name}</td>
                            <td>{m.requester == null ? 'server' : m.requester}</td>
                            <td>
                                <button className="Edit-button nitflex-button" onClick={() => onMovieEdit(m)}>
                                    <img src={editIcon} alt="" />
                                </button>
                                <button className="Delete-button nitflex-button" onClick={() => onMovieDelete(m)}>
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
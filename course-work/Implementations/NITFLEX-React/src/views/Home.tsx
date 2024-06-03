import { useEffect, useState } from 'react';
import axios from 'axios';
import { Movie, defaultMovie } from '../models/Movie';
import './Home.css';
import './MovieCard.css'
import { Header } from '../components/Header';
import { backendUrl } from '../globals';
import { Modal } from '../components/Modal';
import { Player, SubtitleTrack } from '../components/Player';
import { Subtitle, defaultSubtitle } from '../models/Subtitle';

function Home() {
  const [movies, setMovies] = useState([defaultMovie]);
  const [trailerOpen, setTrailerOpen] = useState(false);
  const [timeoutID, setTimeoutID] = useState(setTimeout(() => {}, 0));
  const [timeoutRunning, setTimeoutRunning] = useState(false);

  // Gets the Movie search param, not 'window.location.search'
  const getSearchParam = (): string | null => {
    let search = window.location.search
    if(search === '')
      return null

    return search.substring(search.indexOf('?') + 1)
  }

  const fetchMovies = () => {
    const getPath = (): string => {
      let search = getSearchParam()
      if(search)
        return `/movies?${search}`

      return '/movies'
    }

    if(trailerOpen) return;

    axios.get(backendUrl + getPath(), { withCredentials: true })
      .then(response => response.data)
      .then((arr: Movie[]) => {
        setMovies(arr);
      });
  }

  const onTrailerOpen = () => {
    setTrailerOpen(true)
  }

  const onTrailerClose = () => {
    setTrailerOpen(false)
  }

  useEffect(() => {
    if(timeoutRunning) {
      if(trailerOpen){
        clearTimeout(timeoutID);
        setTimeoutRunning(false);
      }
      return;
    }

    if(trailerOpen) return;

    if(movies.includes(defaultMovie)){
      fetchMovies();
    }else{
      setTimeoutID(setTimeout(() => {setTimeoutRunning(false); fetchMovies();}, 20000));
      setTimeoutRunning(true);
    }
  });

  return (
    <div>
      <Header navbar={true} />
      <div className="Home">
        <form className='Search-form'>
          <input type="text" name='search' placeholder='Search' />
        </form>
        <div className='Home-movies'>
          {movies.filter(m => m !== defaultMovie).map(m => 
            <MovieCard key={m.id} movie={m} onTrailerOpen={() => onTrailerOpen()} onTrailerClose={() => onTrailerClose()} />
          )}
        </div>
      </div>
    </div>
  );
}

const MovieCard = (props: {movie: Movie, onTrailerOpen: Function, onTrailerClose: Function}) => {
  const [trailerModalOpen, setTrailerModalOpen] = useState(false);

  const watchMovie = () => {
    window.location.href = `/watch/${props.movie.id}`;
  }

  const openTrailerModal = () => {
    setTrailerModalOpen(true);
    props.onTrailerOpen();
  }

  const closeTrailerModal = () => {
    setTrailerModalOpen(false);
    props.onTrailerClose();
  }

  return (
    <div className='Movie-card'>
      <div className='Movie-card-data'>
        <h2 className='Movie-title'>{props.movie.name}</h2>
        <p className='Movie-added-by'>Added by {props.movie.requester == null ? 'server' : props.movie.requester}</p>
      </div>
      <div className='Movie-card-action'>
        <MovieCardAction onClick={watchMovie}>Watch</MovieCardAction>
        <MovieCardAction disabled={!props.movie.hasTrailer} onClick={openTrailerModal}>Watch trailer</MovieCardAction> {/* If no trailer, the action should be inactive */}
      </div>
      <Modal isOpen={trailerModalOpen} onClose={closeTrailerModal}>
        <TrailerPlayerContainer movie={props.movie}/>
      </Modal>
    </div>
  )
}

const MovieCardAction = (props: {disabled?: boolean, onClick: Function, children: string}) => {
  return (
    <div className={`Movie-card-option${props.disabled ? ' disabled' : ''}`} onClick={() => !props.disabled && props.onClick()}>
      <p className='Option-text'>{props.children}</p>
    </div>
  )
}

const TrailerPlayerContainer = (props: {movie: Movie}) => {
  const [subtitles, setSubtitles] = useState([defaultSubtitle]);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if(ready)
      return;

    axios.get(backendUrl + `/subtitles/${props.movie.id}/trailer`, { withCredentials: true })
      .then(response => response.data)
      .then((subsArr: Subtitle[]) => {
          setSubtitles(subsArr);
          setReady(true);
      })
  });

  return (
    <div className='Trailer-player-container'>
      {ready && <Player width={400} videoPath={`${backendUrl}/stream/trailer/${props.movie.id}`} subtitlesPaths={subtitles.filter(s => s !== defaultSubtitle).map((s): SubtitleTrack => {return {src: `${backendUrl}/stream/subs/${props.movie.id}/${s.id}`, label: s.name}})} />}
    </div>
  )
}

export default Home;

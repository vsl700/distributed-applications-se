import { useEffect } from "react";
import videojs from "video.js";
import "./Player.css";

export interface SubtitleTrack {
    src: string,
    label: string
}

export const Player = (props: {width: number, height?: number, videoPath: string, subtitlesPaths: SubtitleTrack[]}) => {
    const videoURL = props.videoPath;

    useEffect(() => {
        let options = {
            tracks: props.subtitlesPaths.map(p => {return {src: p.src, label: p.label, kind: 'subtitles', srclang: 'bul'}})
        };
        
        console.log(options);
        let player = videojs('videojs-player', options);
        player.width(props.width);
        player.height(props.height);
        player.src({ src: `${videoURL}/manifest.mpd`, type: 'application/dash+xml', withCredentials: true});

        return () => player.dispose();
    }, [props.height, props.width]);

    return (
        <video id='videojs-player'
        className="video-js"
        crossOrigin="use-credentials"
        controls
        preload="auto"></video>
    );
}

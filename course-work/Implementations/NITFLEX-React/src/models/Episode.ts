export interface Episode{
    id: string,
    seasonNumber: number,
    episodeNumber: number
}

export const defaultEpisode: Episode = {id: "", seasonNumber: 0, episodeNumber: 0};

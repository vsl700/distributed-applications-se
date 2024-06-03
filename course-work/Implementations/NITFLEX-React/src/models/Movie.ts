export interface Movie{
    id: string,
    name: string,
    type: string,
    hasTrailer: boolean,
    requester: string | null
}

export const defaultMovie: Movie = {id: "", name: "defaultMovie", type: "", hasTrailer: false, requester: null};

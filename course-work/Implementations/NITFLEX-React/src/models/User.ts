export interface User {
    id: string,
    username: string,
    role: string
}

export const defaultUser: User = {id: '', username: '', role: ''}
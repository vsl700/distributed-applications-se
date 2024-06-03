export interface UserSettings{
    status: string,
    role: string,
    deviceLimit: number
}

export const defaultUserSettings: UserSettings = {status: '', role: '', deviceLimit: 0}
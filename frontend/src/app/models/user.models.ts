export interface Credentials {
    email: string;
    password: string;
}

export interface User {
    userName: string;
    email: string;
    profilePicture: string;
}

export interface RegisterData{
    userName: string;
    email: string;
    birthdate: string;
    password: string;
    securityQuestion: string;
    answer: string;
    favoriteGenres: {id: number, genre: string}[];
}

export interface AnswerResponse {
    email: string;
    answer: string;
}


import { FilmListItem } from "./film.models";

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

export interface MyProfile {
  email: string;
  userName: string;
  profilePicture: string;
  bio: string;
  isPrivate: boolean;
  followers: number;
  following: number;
  watchedFilms: FilmListItem[];
  laterFilms: FilmListItem[];
}

export interface UserProfile {
  email: string;
  userName: string;
  profilePicture: string;
  bio: string;
  isPrivate: boolean;
  followers: number;
  following: number;
  watchedFilms: FilmListItem[] | null;
  laterFilms: FilmListItem[] | null;
}

import { Film } from './film.models';

export interface SubscriptionId {
  email: string;
  forumId: number;
}

export interface Forum {
  id: number;
  discourseTopicId: number;
  film: Film; 
}

export interface SubscriptionResponse {
  id: SubscriptionId;
  forum: Forum;
}
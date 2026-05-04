export interface NotifyResponse {
  senderEmail: string;
  senderName: string;
  senderProfilePicture: string;
  message: string;
  date: string;
  read: boolean;
  type: 'FOLLOWREQUEST' | string; //Añadir los otros tipos de notificaciones
}
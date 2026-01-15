import {Payload} from './payload';

export interface Apiresponse<T> {
  status: boolean;
  messages: string[];
  payload: Payload<T>;
}

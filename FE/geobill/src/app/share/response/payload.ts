import {SortData} from './sort-data';
import {Pageable} from './pageable';

export interface Payload<T> {
  content: T[];
  pageable: Pageable;
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: SortData;
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

import {SortData} from './sort-data';

export interface Pageable {
  pageNumber: number;
  pageSize: number;
  sort: SortData;
  offset: number;
  paged: boolean;
  unpaged: boolean;
}

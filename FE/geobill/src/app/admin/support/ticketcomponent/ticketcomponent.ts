/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Menutop } from '../../../share/menutop/menutop';
import { Topheader } from '../../../share/topheader/topheader';

export interface Tickettab {
  key: string;
  label: string;
  count: number;
}

export interface Ticketfilter {
  ticketNumber: string;
  summary: string;
  priority: string;
  department: string;
  assignedTo: string;
  lastReply: string;
}

export interface Ticketrow {
  id: number;
  number: string;
  summary: string;
  priority: string;
  department: string;
  assignedTo: string;
  lastReply: string;
  status: string;
}

@Component({
  selector: 'app-ticketcomponent',
  imports: [
    CommonModule,
    FormsModule,
    Menutop,
    Topheader
  ],
  templateUrl: './ticketcomponent.html',
  styleUrl: './ticketcomponent.css',
})
export class Ticketcomponent {

  tabs: Tickettab[] = [
    { key: 'awaiting_staff', label: 'Awaiting Staff', count: 0 },
    { key: 'awaiting_client', label: 'Awaiting Client', count: 0 },
    { key: 'in_progress', label: 'In Progress', count: 0 },
    { key: 'on_hold', label: 'On Hold', count: 0 },
    { key: 'closed', label: 'Closed', count: 0 },
    { key: 'trash', label: 'Trash', count: 0 }
  ];

  priorities: string[] = ['Any', 'Low', 'Medium', 'High'];
  departments: string[] = ['Any'];
  assignees: string[] = ['Any'];
  lastReplies: string[] = ['Any', 'Today', 'Last 7 days', 'Last 30 days'];

  activeTab = 'in_progress';
  expanded = false;
  tickets: Ticketrow[] = [];

  filter: Ticketfilter = this.emptyFilter();

  get visibleTickets(): Ticketrow[] {
    return this.tickets.filter(ticket => ticket.status === this.activeTab);
  }

  selectTab(key: string): void {
    this.activeTab = key;
  }

  toggleExpand(): void {
    this.expanded = !this.expanded;
  }

  applyFilters(): void {
    // TODO: panggil service ticket dengan nilai this.filter
  }

  clearFilters(): void {
    this.filter = this.emptyFilter();
  }

  private emptyFilter(): Ticketfilter {
    return {
      ticketNumber: '',
      summary: '',
      priority: 'Any',
      department: 'Any',
      assignedTo: 'Any',
      lastReply: 'Any'
    };
  }
}

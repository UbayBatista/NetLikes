import { Component } from '@angular/core';
import { ForumList } from '../../components/forum-list/forum-list';
import { ChatWindow } from '../../components/chat-window/chat-window';

@Component({
  selector: 'app-forum',
  standalone: true,
  imports: [ForumList, ChatWindow],
  templateUrl: './forum.html',
  styleUrl: './forum.css'
})

export class Forum { 
  currentTitle: string = '';
  forumId: number | null = null;
  selectedForum: boolean = false;

  seeChat(event: { title: string, forumId: number }) {
    this.currentTitle = event.title;
    this.selectedForum = true;
    this.forumId = event.forumId;
  }
  returnToList() {
    this.selectedForum = false;
  }
}
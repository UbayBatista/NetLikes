import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Forum } from './forum';
import { ForumList } from '../../components/forum-list/forum-list';
import { ChatWindow } from '../../components/chat-window/chat-window';
import { provideRouter } from '@angular/router';

describe('Forum', () => {
  let component: Forum;
  let fixture: ComponentFixture<Forum>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Forum, ForumList, ChatWindow],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(Forum);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the sidebar and chat main containers', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.sidebar-foros')).toBeTruthy();
    expect(compiled.querySelector('.chat-main')).toBeTruthy();
  });
});
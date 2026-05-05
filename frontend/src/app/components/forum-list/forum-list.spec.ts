import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ForumList } from './forum-list';
import { FormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';

describe('ForumList', () => {
  let component: ForumList;
  let fixture: ComponentFixture<ForumList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ForumList, FormsModule],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(ForumList );
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
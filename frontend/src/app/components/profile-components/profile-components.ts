import { Component, Input, ViewChild, ElementRef, ChangeDetectorRef, AfterViewInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-profile-components',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './profile-components.html',
    styleUrls: ['./profile-components.css']
})
export class ProfileBody implements AfterViewInit {
    @Input() title: string = '';
    @Input() isEditing: boolean = false;

    @ViewChild('scrollContainer') scrollContainer!: ElementRef<HTMLDivElement>;

    canScrollLeft = false;
    canScrollRight = false;

    constructor(private cdr: ChangeDetectorRef) {}

    @HostListener('window:resize')
    onResize() {
        this.updateScrollButtons();
    }

    ngAfterViewInit() {
        this.updateScrollButtons();
        setTimeout(() => this.updateScrollButtons(), 500);
        setTimeout(() => this.updateScrollButtons(), 1500);
    }

    updateScrollButtons() {
        const el = this.scrollContainer?.nativeElement;
        if (!el) return;

        this.canScrollLeft = el.scrollLeft > 5;
        this.canScrollRight = el.scrollWidth > (el.scrollLeft + el.clientWidth + 5);
        
        this.cdr.detectChanges();
    }

    scroll(direction: 'left' | 'right') {
        const el = this.scrollContainer.nativeElement;
        const scrollAmount = direction === 'left' ? -el.offsetWidth * 0.8 : el.offsetWidth * 0.8;
        
        el.scrollBy({ left: scrollAmount, behavior: 'smooth' });

        setTimeout(() => this.updateScrollButtons(), 400);
    }
}
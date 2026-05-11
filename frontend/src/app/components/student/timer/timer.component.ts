import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-timer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './timer.component.html',
  styleUrls: ['./timer.component.scss']
})
export class TimerComponent implements OnInit, OnChanges, OnDestroy {
  @Input() durationSeconds = 900;
  @Output() expired = new EventEmitter<void>();

  remainingSeconds = 0;
  displayTime = '00:00';
  private subscription?: Subscription;
  private timerStarted = false;

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.startTimer();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['durationSeconds'] && !changes['durationSeconds'].firstChange) {
      this.restartTimer();
    }
  }

  private restartTimer(): void {
    this.subscription?.unsubscribe();
    this.timerStarted = false;
    this.remainingSeconds = this.durationSeconds;
    this.startTimer();
  }

  private startTimer(): void {
    if (this.timerStarted || this.durationSeconds <= 0) {
      this.updateDisplay();
      return;
    }

    this.timerStarted = true;
    this.remainingSeconds = this.durationSeconds;
    this.updateDisplay();

    this.subscription = interval(1000).subscribe(() => {
      this.remainingSeconds = Math.max(0, this.remainingSeconds - 1);
      this.updateDisplay();
      this.cdr.detectChanges();

      if (this.remainingSeconds === 0) {
        this.expired.emit();
        this.subscription?.unsubscribe();
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  private updateDisplay(): void {
    const minutes = Math.floor(this.remainingSeconds / 60).toString().padStart(2, '0');
    const seconds = (this.remainingSeconds % 60).toString().padStart(2, '0');
    this.displayTime = `${minutes}:${seconds}`;
  }
}

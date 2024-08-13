import {Component} from "@angular/core";
import {APP_BASE_HREF, CommonModule} from "@angular/common";
import {RouterOutlet} from "@angular/router";

@Component({
  selector: 'left-panel2',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './top-panel.component.html',
  styleUrl: './left-panel2.component.css'
})
export class TopPanelComponent {
  title = 'webdav';
}
